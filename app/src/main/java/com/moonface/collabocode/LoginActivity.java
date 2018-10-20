package com.moonface.collabocode;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    ProgressBar progressBar;
    SignInFragment signInFragment;
    SignUpFragment signUpFragment;
    Button signInButton, createAccountButton;
    View.OnClickListener signInButtonClickListener, signUpButtonClickListener, createAccountButtonClickListener, alreadyHaveAccountButtonClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        signInButton = findViewById(R.id.sign_in_button);
        createAccountButton = findViewById(R.id.create_account_button);
        signInFragment = new SignInFragment();
        signUpFragment = new SignUpFragment();

        signInButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        };
        signUpButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        };
        createAccountButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragments(signUpFragment);
            }
        };
        alreadyHaveAccountButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragments(signInFragment);
            }
        };

        setSupportActionBar(toolbar);
        progressBar.setVisibility(View.INVISIBLE);
        changeFragments(signInFragment);
    }

    private void changeFragments(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.placeholder, fragment);
        transaction.commit();
        if(fragment == signInFragment){
            signInButton.setText(R.string.sign_in_button);
            signInButton.setOnClickListener(signInButtonClickListener);
            createAccountButton.setText(R.string.create_account_button);
            createAccountButton.setOnClickListener(createAccountButtonClickListener);
        }
        if(fragment == signUpFragment){
            signInButton.setText(R.string.sign_up_button);
            signInButton.setOnClickListener(signUpButtonClickListener);
            createAccountButton.setText(R.string.already_have_account_button);
            createAccountButton.setOnClickListener(alreadyHaveAccountButtonClickListener);
        }
    }

    private void signIn() {
        if(signInFragment.getEmail().length() == 0){
            Toast.makeText(this, R.string.email_empty_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isEmailValid(signInFragment.getEmail())){
            Toast.makeText(this, R.string.email_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        if(signInFragment.getPassword().length() == 0){
            Toast.makeText(this, R.string.password_empty_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(signInFragment.getEmail(), signInFragment.getPassword()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent startApp = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(startApp);
                } else {
                    Toast.makeText(getApplicationContext(), task.getException() != null ? task.getException().getMessage() : getString(R.string.error_occurred_message), Toast.LENGTH_SHORT).show();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void signUp() {
        if(signUpFragment.getEmail().length() == 0){
            Toast.makeText(this, R.string.email_empty_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isEmailValid(signUpFragment.getEmail())){
            Toast.makeText(this, R.string.email_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        if(signUpFragment.getName().length() == 0){
            Toast.makeText(this, R.string.name_empty_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        if(signUpFragment.getPassword().length() == 0){
            Toast.makeText(this, R.string.password_empty_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(signUpFragment.getEmail(), signUpFragment.getPassword()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent startApp = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(startApp);
                } else {
                    Toast.makeText(getApplicationContext(), task.getException() != null ? task.getException().getMessage() : getString(R.string.error_occurred_message), Toast.LENGTH_SHORT).show();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    private boolean isEmailValid(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
