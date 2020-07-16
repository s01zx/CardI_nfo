package com.solz.cardinfo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.microblink.directApi.DirectApiErrorListener;
import com.microblink.directApi.RecognizerRunner;
import com.microblink.entities.recognizers.Recognizer;
import com.microblink.entities.recognizers.RecognizerBundle;
import com.microblink.entities.recognizers.blinkcard.BlinkCardRecognizer;
import com.microblink.hardware.orientation.Orientation;
import com.microblink.recognition.RecognitionSuccessType;
import com.microblink.view.recognition.ScanResultListener;
import com.solz.cardinfo.Data.AppDatabase;
import com.solz.cardinfo.Data.AppExecutors;
import com.solz.cardinfo.Data.Card;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

public class CaptureCard extends AppCompatActivity {

    Button button, processCard, addCard;
    ImageView imageTaken;
    EditText editText;
    TextView rCardNum, rCardType, rCardExpiry;
    CardView cardView;

    String currentPhotoPath;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int CAMERA_PERM_CODE = 101;

    private RecognizerRunner mRecognizerRunner;
    private BlinkCardRecognizer mRecognizer;
    private RecognizerBundle mRecognizerBundle;

    String filePath;

    Card card;
    String cardNumber;
    String validThru;
    String issuer;
    int imtRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_card);

        findID();
        final AppDatabase mDb = AppDatabase.getInstance(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askCameraPermissions();
            }
        });

        processCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                mRecognizerRunner.recognizeBitmap(bitmap, Orientation.ORIENTATION_LANDSCAPE_RIGHT, mScanResultListener);
            }
        });

        addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String val = editText.getText().toString();

                if (val.isEmpty()){
                    Snackbar snackbar = Snackbar
                            .make(view, " Kindly Input your card name", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    return;

                }

                String trimed = cardNumber.replace(" ", "");

                String[] splitArray=validThru.split("/");
                String mm = splitArray[0];
                String yy = splitArray[1];

                card = new Card(trimed, val, issuer, mm, yy, cardType(issuer));

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.taskDao().insertTask(card);
                        finish();

                    }
                });
            }
        });

        mRecognizer = new BlinkCardRecognizer();
        mRecognizerBundle = new RecognizerBundle(mRecognizer);
        mRecognizerRunner = RecognizerRunner.getSingletonInstance();


        mRecognizerRunner.initialize(this, mRecognizerBundle, new DirectApiErrorListener() {
            @Override
            public void onRecognizerError(Throwable t) {
                Toast.makeText(CaptureCard.this, "There was an error in initialization of Recognizer: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });


    }

    private void findID() {
        imageTaken = findViewById(R.id.picImageView);
        button = findViewById(R.id.takePic);
        processCard = findViewById(R.id.decode_card);
        rCardNum = findViewById(R.id.resultCardNumber);
        rCardType = findViewById(R.id.resultCardType);
        rCardExpiry = findViewById(R.id.resultCardExpiry);
        editText = findViewById(R.id.resultCardName);
        addCard = findViewById(R.id.addCard);
        cardView = findViewById(R.id.cardView);
    }

    int cardType(String val){

        switch (val){
            case "Verve":
                imtRes = R.drawable.verve;
                break;
            case "MasterCard":
                imtRes = R.drawable.master;
                break;
            case "Visa":
            default:
                imtRes = R.drawable.visa;
                break;
        }

        return imtRes;
    }

    private final ScanResultListener mScanResultListener = new ScanResultListener() {
        @Override
        public void onScanningDone(@NonNull RecognitionSuccessType recognitionSuccessType) {
            // this method is from ScanResultListener and will be called
            // when scanning completes
            // you can obtain scanning result by calling getResult on each
            // recognizer that you bundled into RecognizerBundle.
            // for example:

            BlinkCardRecognizer.Result result = mRecognizer.getResult();
            if (result.getResultState() == Recognizer.Result.State.Valid) {
                cardNumber = result.getCardNumber();
                final String val = String.valueOf(result.getValidThru());
                //validThru = String.valueOf(result.getValidThru());
                issuer = String.valueOf(result.getIssuer());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rCardNum.setText(cardNumber);
                        rCardType.setText(issuer);
                        String[] splitArray=val.split(" ");
                        validThru = splitArray[4];
                        rCardExpiry.setText(validThru);
                        processCard.setVisibility(View.GONE);
                        cardView.setVisibility(View.VISIBLE);


                    }
                });
            }else {
                Toast.makeText(getApplicationContext(), "Error Reading Image.Kindly retake the picture", Toast.LENGTH_SHORT ).show();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button.setText("Recapture Image");
                        processCard.setVisibility(View.GONE);
                        button.setVisibility(View.VISIBLE);
                    }
                });

            }



        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            File f = new File(currentPhotoPath);
            filePath = f.getPath();
            imageTaken.setImageURI(Uri.fromFile(f));
            processCard.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);



        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.solz.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecognizerRunner.terminate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }else {
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else {
            dispatchTakePictureIntent();
        }

    }


}

