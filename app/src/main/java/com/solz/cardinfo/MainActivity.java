package com.solz.cardinfo;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.microblink.MicroblinkSDK;
import com.solz.cardinfo.Data.AppDatabase;
import com.solz.cardinfo.Data.AppExecutors;
import com.solz.cardinfo.Data.Card;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Transaction;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import java.util.List;

import static android.widget.LinearLayout.VERTICAL;

public class MainActivity extends AppCompatActivity implements BottomSheet.BottomSheetListener {

    FloatingActionButton fab, fab1, fab2;
    LinearLayout fabLayout1, fabLayout2, empty;
    View fabBGLayout;
    boolean isFABOpen = false;
    RecyclerView recyclerView;
    CardAdapter adapter;
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MicroblinkSDK.setLicenseKey("sRwAAAARY29tLnNvbHouY2FyZGluZm9QYKmax3T43DkroYdHaikBfpn7I3ogGzwKlmUGI/C1Ud8Hctu5SzDp6XRSDptGmvDmKB1nvTPl6xl26/sSK2ClRgUAuWjaRsNnJ8ZGyiIlyNGQZywjIWjuFy4pR57FfD+AiCHLLacq8McNIUCwcwgMBsoFQyjD3ahC7ufIkCJNyw29jR98EGYd", this);


        findView();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CardAdapter(this);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        recyclerView.addItemDecoration(decoration);

        mDb = AppDatabase.getInstance(getApplicationContext());


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFABOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
            }
        });

       fab2.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
//               Intent intent = new Intent(MainActivity.this, AddCard.class);
//               startActivity(intent);
               BottomSheet bottomSheet = new BottomSheet();
               bottomSheet.show(getSupportFragmentManager(), "bottomSheet");
               closeFABMenu();
           }
       });

       fab1.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(MainActivity.this, CaptureCard.class);
               startActivity(intent);
           }
       });

    }

    private void findView() {
        fab = findViewById(R.id.fab);
        fab1 = findViewById(R.id.fab1);
        fab2 = findViewById(R.id.fab2);

        fabLayout1 = findViewById(R.id.fabLayout1);
        fabLayout2 = findViewById(R.id.fabLayout2);

        fabBGLayout = findViewById(R.id.fabBGLayout);

        recyclerView = findViewById(R.id.recycler);

        empty = findViewById(R.id.emptyView);
    }


    private void showFABMenu() {
        isFABOpen = true;
        fabLayout1.setVisibility(View.VISIBLE);
        fabLayout2.setVisibility(View.VISIBLE);
        fabBGLayout.setVisibility(View.VISIBLE);
        fab.animate().rotationBy(180);
        fabLayout1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fabLayout2.animate().translationY(-getResources().getDimension(R.dimen.standard_100));
    }

    private void closeFABMenu() {
        isFABOpen = false;
        fabBGLayout.setVisibility(View.GONE);
        fab.animate().rotation(0);
        fabLayout1.animate().translationY(0);
        fabLayout2.animate().translationY(0);
        fab.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

                if (!isFABOpen) {
                    fabLayout1.setVisibility(View.GONE);
                    fabLayout2.setVisibility(View.GONE);
                }

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isFABOpen) {
            closeFABMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<Card> trans = mDb.taskDao().loadAllTasks();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (trans.size() == 0){
                            recyclerView.setVisibility(View.GONE);
                            empty.setVisibility(View.VISIBLE);
                        }else{
                            recyclerView.setVisibility(View.VISIBLE);
                            empty.setVisibility(View.GONE);
                            adapter.setTasks(trans);
                        }

                    }
                });
            }
        });
    }

    @Override
    public void onButtonClickeed() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<Card> trans = mDb.taskDao().loadAllTasks();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //adapter.setTasks(trans);
                        if (trans.size() == 0){
                            recyclerView.setVisibility(View.GONE);
                            empty.setVisibility(View.VISIBLE);
                        }else{
                            recyclerView.setVisibility(View.VISIBLE);
                            empty.setVisibility(View.GONE);
                            adapter.setTasks(trans);
                        }
                    }
                });
            }
        });

    }


    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
