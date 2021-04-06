package com.willsprogrammer.onlinefriendlynotepad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailView, password_view, reEnter_password_view;

    private String input_email, input_password, input_reEnter_password;

    private FirebaseAuth mAuth;

    private ProgressBar progressBar;
    private Button register_button;

    private SoundEffects play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        play = new SoundEffects(this);

        emailView = findViewById(R.id.register_email_view);
        password_view = findViewById(R.id.register_password_view);
        reEnter_password_view = findViewById(R.id.register_confirm_password_view);

        mAuth = FirebaseAuth.getInstance(); // initializing the instance

        progressBar = findViewById(R.id.progressBar);
        register_button = findViewById(R.id.register_button);

    }

    public void loginLinkClicked(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//
//        // if user logged in, go to sign-in screen
//        if (mAuth.getCurrentUser() != null) {
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        }
//    }


    public void RegisterButtonClicked(View view) {
        play.playSound(R.id.login_button);

        input_email = emailView.getText().toString().trim();
        input_password = password_view.getText().toString().trim();
        input_reEnter_password = reEnter_password_view.getText().toString().trim();


        if (!validRegistration(input_email, input_password, input_reEnter_password)) {

            return;
        }

        register_button.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        //create user
        // after the user registered, the user will continue to remain signed in even if the app restart
        mAuth.createUserWithEmailAndPassword(input_email, input_password) // create email and password
                // add addOnCompleteListener in order to get the results of the current user with email and password
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            register_button.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, "Registration failed.", Toast.LENGTH_LONG).show();
                        } else {

                            mAuth.signOut();
                            Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        }
                    }
                });
    }

    private boolean validRegistration(String input_email, String input_password, String input_reEnter_password) {

        if (input_email.isEmpty() || input_password.isEmpty() || input_reEnter_password.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        // To make sure the user enters the strong password
        if (input_password.length() < 6) {
//            Toast.makeText(getApplicationContext(), "Password too short, enter minimum of 6 characters!", Toast.LENGTH_LONG).show();
            password_view.setError(getString(R.string.minimum_password));
            return false;
        }

        // verifying for matching confirmation passwords
        if (!input_password.equals(input_reEnter_password)) {
            Toast.makeText(this, "Your passwords don't match", Toast.LENGTH_SHORT).show();
            reEnter_password_view.setError("Your passwords don't match");
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