package com.willsprogrammer.onlinefriendlynotepad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextView reset_password_link;

    private EditText emailView, password_view;

    private FirebaseAuth mAuth;

    private ProgressBar progressBar;
    private Button login_button;

    private SoundEffects play;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        play = new SoundEffects(this);

        emailView = findViewById(R.id.email_view);
        password_view = findViewById(R.id.password_view);
        login_button = findViewById(R.id.login_button);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        reset_password_link = findViewById(R.id.forgot_password_link);

        progressBar = findViewById(R.id.progressBar2);

        setLinkOnclickListeners();

    }
    public void loginLinkClicked(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
        finish();
    }

    private void setLinkOnclickListeners(){
        reset_password_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // resetting functionality
                final EditText reset_email = new EditText(view.getContext());
                AlertDialog.Builder password_reset_dialog = new AlertDialog.Builder(view.getContext());
                password_reset_dialog.setTitle("Reset Password");
                password_reset_dialog.setMessage("Enter a valid Email to receive reset password link");
                password_reset_dialog.setView(reset_email);


                password_reset_dialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // extract the email and send reset link
                        String input_email = reset_email.getText().toString();
                        if(TextUtils.isEmpty(input_email)){
                            Toast.makeText(LoginActivity.this, "Enter your email", Toast.LENGTH_SHORT).show();

                        }else {

                            progressBar.setVisibility(View.VISIBLE);
                            login_button.setVisibility(View.INVISIBLE);

                            mAuth.sendPasswordResetEmail(input_email).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressBar.setVisibility(View.GONE);
                                    login_button.setVisibility(View.VISIBLE);
                                    Toast.makeText(LoginActivity.this, "Reset link sent to your Email", Toast.LENGTH_LONG).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    login_button.setVisibility(View.VISIBLE);
                                    Toast.makeText(LoginActivity.this, "Error! Reset link is not sent", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
                password_reset_dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // close the dialog
                    }
                });
                password_reset_dialog.create().show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // if user logged in, go to sign-in screen
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    public void loginButtonClicked(View view) {
        play.playSound(R.id.login_button);

        String input_email = emailView.getText().toString().trim();
        String input_password = password_view.getText().toString().trim();


        if(!validLogin(input_email, input_password)){

            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        login_button.setVisibility(View.INVISIBLE);

        //authenticate user
        mAuth.signInWithEmailAndPassword(input_email, input_password) // sign in with email and password
                // add addOnCompleteListener in order to get the results of the current user with email and password
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // there was an error
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();


                        } else {

                            progressBar.setVisibility(View.GONE);
                            login_button.setVisibility(View.VISIBLE);
                            Toast.makeText(LoginActivity.this, "Failed to login", Toast.LENGTH_SHORT).show();


                        }
                    }
                });
    }

    private boolean validLogin(String input_email, String input_password){

        if(input_email.isEmpty() || input_password.isEmpty()){
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        // To make sure the user enters the strong password
        if (input_password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum of 6 characters!", Toast.LENGTH_LONG).show();
            password_view.setError(getString(R.string.minimum_password));
            return false;
        }

        // return true if everything is fine
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        play.releaseSoundPool();
    }
}