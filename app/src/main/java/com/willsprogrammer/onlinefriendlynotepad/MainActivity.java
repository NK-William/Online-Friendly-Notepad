package com.willsprogrammer.onlinefriendlynotepad;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecyclerAdapter.ViewClickListener {


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;
    private ArrayList<Notes> list;
    private DatabaseReference mDatabase;
    private ArrayList<Notes> inverse_list;
    private SoundEffects play;
    private static final String TAG = "MainActivity";
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        play = new SoundEffects(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play.playSound(R.id.card_view);

                Intent addNewNotesIntent = new Intent(MainActivity.this, NotesActivity.class);
                addNewNotesIntent.putExtra("ID_EXTRA", 0);
                startActivity(addNewNotesIntent);
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        inverse_list = new ArrayList<>();
        list = new ArrayList<>();
        adapter = new RecyclerAdapter(this, list, this);
        recyclerView.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String user_id = user.getUid();

        final ProgressDialog progressDialog;

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Make sure your have internet connection");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        // for ad banner
        MobileAds.initialize(this);
        mAdView = findViewById(R.id.adView);
        final AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mDatabase = FirebaseDatabase.getInstance().getReference(user_id).child("Notes");

        // this will be executed once in onCreate() in thia activity, so I display progressBar here.
//        if (inverse_list.size() == 0){
//            Toast.makeText(this, "employ progressBar", Toast.LENGTH_SHORT).show();
//        }

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();
                inverse_list.clear();
                Log.d(TAG, "onDataChange: called");
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Notes notes = dataSnapshot.getValue(Notes.class);

                    inverse_list.add(notes);

                }

                for (int i = (inverse_list.size() - 1); i >= 0; i--) {
                    list.add(inverse_list.get(i));
                }
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onDelete(final Notes notes) {
        play.playSound(R.id.card_view);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Proceed with deletion");
        builder.setCancelable(true);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String user_id = user.getUid();

                FirebaseDatabase.getInstance().getReference().child(user_id).child("Notes").child(notes.date_time)
                        .removeValue().addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // do nothing
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public void onEdit(Notes notes) {
        play.playSound(R.id.card_view);

        Intent editNotesIntent = new Intent(MainActivity.this, NotesActivity.class);
        editNotesIntent.putExtra("ID_EXTRA", 1);
        editNotesIntent.putExtra("TITLE_EXTRA", notes.title);
        editNotesIntent.putExtra("NOTES_EXTRA", notes.notes);
        editNotesIntent.putExtra("DATE_TIME_EXTRA", notes.date_time);
        startActivity(editNotesIntent);

    }

    @Override
    public void onFavourite(Notes notes) {
        play.playSound(R.id.item_favourite_image);

        String favourite_value = notes.favourite;
        if (favourite_value.equals("1")) {
            modifyFavourite(notes, "0");
        } else {
            modifyFavourite(notes, "1");

        }
    }

    private void modifyFavourite(final Notes notes, final String value) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String user_id = user.getUid();

        final String formatted_date_time = notes.date_time;

//        final Favourites favourites = new Favourites(notes.title, notes.notes, notes.date_time);

        // reference to "favourite" directory
//        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(user_id).child("Favourites")
//                .child(formatted_date_time);

        // updating the favourite value in "Notes" directory
        FirebaseDatabase.getInstance().getReference(user_id).child("Notes").child(formatted_date_time).child("favourite")
                .setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // If favourite is updated in "notes" then the following code will update to "favourite" directory
                if (task.isSuccessful()) {
                    // this code segment remove from favourite
                    if (value.equals("0")) {
                        Toast.makeText(MainActivity.this, "Removed from favourite", Toast.LENGTH_SHORT).show();

//                        databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if (task.isSuccessful()) {
//                                    Toast.makeText(MainActivity.this, "Removed from favourite", Toast.LENGTH_SHORT)
//                                            .show();
//                                } else {
//                                    Toast.makeText(MainActivity.this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
                    }
                    // this code segment add to favourite
                    else {

                        Toast.makeText(MainActivity.this, "Added to favourite", Toast.LENGTH_SHORT).show();

//                        databaseReference.setValue(favourites)
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            Toast.makeText(MainActivity.this, "Added to favourite", Toast.LENGTH_SHORT).show();
//                                        } else {
//                                            Toast.makeText(MainActivity.this, "Failed to add to favorites", Toast.LENGTH_SHORT).show();
//                                        }
//                                    }
//                                });
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to update favourite", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            Log.d(TAG, "onOptionsItemSelected: returning true ");
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        play.releaseSoundPool();
        mAdView.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdView.resume();
    }
}