package com.solz.cardinfo;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.solz.cardinfo.Data.AppDatabase;
import com.solz.cardinfo.Data.AppExecutors;
import com.solz.cardinfo.Data.Card;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BottomSheet extends BottomSheetDialogFragment implements AdapterView.OnItemClickListener {

    String[] cardTypes = {"Visa", "MasterCard", "Verve"};
    String value;
    int imtRes;

    Card card;
    private BottomSheetListener mListner;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_add_card,container,false);

        Button button = v.findViewById(R.id.submit);
        final EditText name = v.findViewById(R.id.cardName);
        final EditText number = v.findViewById(R.id.cardNumber);
        final EditText expM = v.findViewById(R.id.mm);
        final EditText expY = v.findViewById(R.id.yy);

        final AppDatabase mDb = AppDatabase.getInstance(getContext());

        // region editText Optimization
        number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                Object[] paddingSpans = editable.getSpans(0, editable.length(), DashSpan.class);
                for (Object span : paddingSpans) {
                    editable.removeSpan(span);
                }

                addSpans(editable);
            }

            private static final int GROUP_SIZE = 4;

            private void addSpans(Editable editable) {

                final int length = editable.length();
                for (int i = 1; i * (GROUP_SIZE) < length; i++) {
                    int index = i * GROUP_SIZE;
                    editable.setSpan(new DashSpan(), index - 1, index,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        });
        // endregion

        //region Spinner
        Spinner spinner = v.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object item = adapterView.getItemAtPosition(i);
                if (item != null){
                    value = item.toString();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter ad = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, cardTypes);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(ad);
        //endregion

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String c_name = name.getText().toString();
                String c_number = number.getText().toString();
                String c_expM = expM.getText().toString();
                String c_expY = expY.getText().toString();

                if (c_name.isEmpty()){
                    Toast.makeText(getContext(), "Kindly Input your card name", Toast.LENGTH_SHORT ).show();
                    return;
                }

                if (c_number.isEmpty()){
                    Toast.makeText(getContext(), "Kindly Input your card number", Toast.LENGTH_SHORT ).show();
                    return;
                }

                if (c_expM.isEmpty() || c_expY.isEmpty()){
                    Toast.makeText(getContext(), "Kindly Input the card expiry date", Toast.LENGTH_SHORT ).show();
                    return;
                }




                card = new Card(c_number, c_name, value, c_expM, c_expY, cardType(value));

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.taskDao().insertTask(card);
                        dismiss();

                    }
                });

                mListner.onButtonClickeed();

            }
        });





        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    public interface BottomSheetListener{
        void onButtonClickeed();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListner = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement Listener");

        }
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
}
