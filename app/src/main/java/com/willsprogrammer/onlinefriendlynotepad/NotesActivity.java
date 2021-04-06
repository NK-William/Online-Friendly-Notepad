package com.willsprogrammer.onlinefriendlynotepad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NotesActivity extends AppCompatActivity {

    private static final String TAG = "NotesActivity";
    EditText titleView, notesView;


    private String oldTitle;
    private String oldNotes;
    private String date_time;

    private int notesId;

    private String newTitle;
    private String newNotes;

    private SoundEffects play;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // // for ad banner
        MobileAds.initialize(this);
        mAdView = findViewById(R.id.notes_adView);
        final AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        play = new SoundEffects(this);

        titleView = findViewById(R.id.title_view);
        notesView = findViewById(R.id.notes_view);

        Intent intent = getIntent();
        notesId = intent.getIntExtra("ID_EXTRA", -1);

        //notesId = (Integer) getIntent().getExtras().get(EXTRA_NOTES_ID); // keep note: *************************

        // if notesId != 0, then we are editing the existing notes else we are adding new notes
        if (notesId == -1) {
            Toast.makeText(this, "An error occurred!!", Toast.LENGTH_SHORT).show();
            finish();
        } else {  // then we are editing

            // in case the screen is rotated
            if (savedInstanceState != null) {
                Log.d(TAG, "onCreate: retrieving restored data");
                String savedTitle = savedInstanceState.getString("savedTitle");
                String savedNotes = savedInstanceState.getString("savedNotes");

                oldTitle = savedInstanceState.getString("oldTitle");
                oldNotes = savedInstanceState.getString("oldNotes");
                date_time = savedInstanceState.getString("dateTime");


                titleView.setText(savedTitle);
                notesView.setText(savedNotes);

            } else { // extracting database data if NotesActivity has newly started. since (notesId !=0).
                oldTitle = intent.getStringExtra("TITLE_EXTRA");
                oldNotes = intent.getStringExtra("NOTES_EXTRA");
                date_time = intent.getStringExtra("DATE_TIME_EXTRA");

                titleView.setText(oldTitle);
                notesView.setText(oldNotes);
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save_notes) {
            // perform saving here
            Log.d(TAG, "onOptionsItemSelected: saving");
            save();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void save() {
        play.playSound(R.id.login_button);

        Log.d(TAG, "save: button pressed");

        newTitle = titleView.getText().toString().trim();
        newNotes = notesView.getText().toString().trim();


        // the if block will execute only when a user is creating  new notes
        if (notesId == 0) {
            Log.d(TAG, "save: saving new notes");
            // saving without adding any data.
            if ((newTitle.isEmpty()) && (newNotes.isEmpty())) {
                Log.d(TAG, "save: no data entered");
                Toast.makeText(this, "You can't save empty notes", Toast.LENGTH_SHORT).show();

            }
            // adding only the title without adding to notes body.
            else if (!(newTitle.isEmpty()) && (newNotes.isEmpty())) {
                Log.d(TAG, "save: only title entered");
                Toast.makeText(this, "You can't save only a title, enter notes", Toast.LENGTH_SHORT).show();
            }
            // the following else block will execute only when the notes body is not empty.
            else {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String user_id = user.getUid();

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
                String formatted_date_time = df.format(c.getTime());

                final ProgressDialog progressDialog;

                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Please wait...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(user_id).child("Notes").child(formatted_date_time);
                // saving notes without saving a title
                if (newTitle.isEmpty()) {
                    Log.d(TAG, "save: saving notes without title, that's okay");
                    Notes notes_object = new Notes("", newNotes, formatted_date_time, "0");
                    mRef.setValue(notes_object).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                Toast.makeText(NotesActivity.this, "save successful", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(NotesActivity.this, "Failed to save!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                // user here added title and notes.
                else {
                    Log.d(TAG, "save: saving notes and title");
                    Notes notes_object = new Notes(newTitle, newNotes, formatted_date_time, "0");
                    mRef.setValue(notes_object).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                Toast.makeText(NotesActivity.this, "saved successful", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(NotesActivity.this, "Failed to save!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }
        // The else block will execute only when a user is editing existing notes
        else {

            //***************************************************

            Log.d(TAG, "save: saving new notes");
            // saving without adding any data.
            if ((newTitle.isEmpty()) && (newNotes.isEmpty())) {
                Log.d(TAG, "save: no data entered");
                Toast.makeText(this, "You can't save empty notes", Toast.LENGTH_SHORT).show();

            }
            // adding only the title without adding to notes body.
            else if (!(newTitle.isEmpty()) && (newNotes.isEmpty())) {
                Log.d(TAG, "save: only title entered");
                Toast.makeText(this, "You can't save only a title, enter notes", Toast.LENGTH_SHORT).show();
            }
            // the following else block will execute only when the notes body is not empty.
            else {


                if (oldTitle.equals(newTitle) && oldNotes.equals(newNotes)) {
                    Log.d(TAG, "save: not updating");
                    finish();
                } else {

                    final ProgressDialog progressDialog2;

                    progressDialog2 = new ProgressDialog(this);
                    progressDialog2.setMessage("Please wait...");
                    progressDialog2.setCanceledOnTouchOutside(false);
                    progressDialog2.show();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String user_id = user.getUid();
                    final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(user_id).child("Notes").child(date_time);

                    // update only notes
                    if (oldTitle.equals(newTitle) && !(oldNotes.equals(newNotes))) {
                        Log.d(TAG, "save: updating only notes");
                        mRef.child("notes").setValue(newNotes).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressDialog2.dismiss();
                                    Toast.makeText(NotesActivity.this, "Update successful", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    progressDialog2.dismiss();
                                    Toast.makeText(NotesActivity.this, "Failed to update!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    // update only title
                    else if (!(oldTitle.equals(newTitle)) && oldNotes.equals(newNotes)) {
                        Log.d(TAG, "save: updating only title");
                        mRef.child("title").setValue(newTitle).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressDialog2.dismiss();
                                    Toast.makeText(NotesActivity.this, "Update successful", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    progressDialog2.dismiss();
                                    Toast.makeText(NotesActivity.this, "Failed to update!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                    // update notes and title
                    else {
                        Log.d(TAG, "save: updating all");
                        mRef.child("title").setValue(newTitle).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mRef.child("notes").setValue(newNotes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressDialog2.dismiss();
                                                Toast.makeText(NotesActivity.this, "Update successful", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                progressDialog2.dismiss();
                                                Toast.makeText(NotesActivity.this, "Failed to update!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    progressDialog2.dismiss();
                                    Toast.makeText(NotesActivity.this, "Failed to update!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }

            //***************************************************

        }
    }

    @Override
    public void onBackPressed() {

        newTitle = titleView.getText().toString();
        newNotes = notesView.getText().toString();


        // the if block will execute only when a user is creating  new notes
        if (notesId == 0) {

            // exiting without adding any data.
            if ((newTitle.isEmpty()) && (newNotes.isEmpty())) {
                Log.d(TAG, "onBackPressed: exiting on empty title and notes(no dialog)");
                Log.d(TAG, "save: no data entered");
                super.onBackPressed();

            }
            // adding only the title without adding to notes body.
            else if (!(newTitle.isEmpty()) && (newNotes.isEmpty())) {
                Log.d(TAG, "onBackPressed: only title entered(no dialog)");
                Toast.makeText(this, "Not saved, only title entered", Toast.LENGTH_SHORT).show(); //************************
                super.onBackPressed();
            }
            // the following else block will execute only when the notes body is not empty.
            else {

                onAddNewAlertDialog();

                // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@1

            }
        }
        // The else block will execute only when a user is editing existing notes
        else {

            // there are not changes
            if (oldTitle.equals(newTitle) && oldNotes.equals(newNotes)) {
                Log.d(TAG, "onBackPressed: no changes(no dialog)");
                super.onBackPressed();
            } else {

                // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@2

                OnUpdateAlertDialog();
            }

        }

    }

    private void onAddNewAlertDialog() {
        Log.d(TAG, "initialisingDialogOnAddNew: called");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You have some data entered");
        builder.setCancelable(true);

        builder.setPositiveButton("Stay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do nothing
            }
        });
        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    private void OnUpdateAlertDialog() {
        Log.d(TAG, "initialisingDialogOnUpdate: method called to trigger the dialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You have made changes");
        builder.setCancelable(true);

        builder.setPositiveButton("Stay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do nothing
            }
        });
        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

        Log.d(TAG, "initialisingDialogOnAddNew: method exit");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString("savedTitle", titleView.getText().toString());
        outState.putString("savedNotes", notesView.getText().toString());
        outState.putString("dateTime", date_time);

        outState.putString("oldTitle", oldTitle);
        outState.putString("oldNotes", oldNotes);


        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        play.releaseSoundPool();
        mAdView.destroy();
    }

    //    public void demoSave(){
//        String title = title_view.getText().toString().trim();
//        String notes = notes_view.getText().toString().trim();
//
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        String user_id = user.getUid();
//
//        Calendar c = Calendar.getInstance();
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
//        String formatted_date_time = df.format(c.getTime());
//
//        Notes notes_object = new Notes(title, notes, formatted_date_time, "0");
//
//        FirebaseDatabase.getInstance().getReference(user_id).child("Notes").child(formatted_date_time).setValue(notes_object)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if(task.isSuccessful()){
//                            Toast.makeText(NotesActivity.this, "Saved successfully", Toast.LENGTH_SHORT).show();
//                            finish();
//                        }else{
//                            Toast.makeText(NotesActivity.this, "Failed to save!", Toast.LENGTH_SHORT).show();
//                            finish();
//                        }
//                    }
//                });
//    }


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