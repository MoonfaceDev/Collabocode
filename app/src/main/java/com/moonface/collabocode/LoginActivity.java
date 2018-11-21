package com.moonface.collabocode;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {
    private static final String BUTTON_TRANSITION = "main_button";
    public static final long MOVE_DEFAULT_TIME = 100;
    private static final int RC_SIGN_IN = 0;
    ProgressBar progressBar;
    SignInFragment signInFragment;
    SignUpFragment signUpFragment;
    Button createAccountButton;
    View.OnClickListener createAccountButtonClickListener, alreadyHaveAccountButtonClickListener;
    SignInFragment.OnSignInButtonClickListener onSignInButtonClickListener;
    SignUpFragment.OnSignUpButtonClickListener onSignUpButtonClickListener;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        createAccountButton = findViewById(R.id.create_account_button);
        signInFragment = new SignInFragment();
        signUpFragment = new SignUpFragment();
        SignInButton googleSignIn = findViewById(R.id.google_sign_in);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().requestProfile().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, connectionResult -> Toast.makeText(getApplicationContext(), connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show()).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        googleSignIn.setOnClickListener(v -> signIn(mGoogleApiClient));

        mCallbackManager = CallbackManager.Factory.create();
        LoginButton facebookSignIn = findViewById(R.id.facebook_sign_in);
        facebookSignIn.setReadPermissions("email", "public_profile");
        facebookSignIn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), R.string.sign_in_failed_message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), R.string.sign_in_failed_message, Toast.LENGTH_SHORT).show();
            }
        });

        onSignInButtonClickListener = this::signIn;
        onSignUpButtonClickListener = this::signUp;
        createAccountButtonClickListener = v -> changeFragments(signUpFragment);
        alreadyHaveAccountButtonClickListener = v -> changeFragments(signInFragment);

        setSupportActionBar(toolbar);
        progressBar.setVisibility(View.INVISIBLE);
        signUpFragment.setOnSignUpButtonClickListener(onSignUpButtonClickListener);
        signInFragment.setOnSignUpButtonClickListener(onSignInButtonClickListener);
        changeFragments(signInFragment);
    }

    private void changeFragments(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        TransitionSet enterTransitionSet = new TransitionSet();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enterTransitionSet.addTransition(new ChangeBounds()).addTransition(new ChangeTransform()).addTransition(new ChangeImageTransform());
        }
        enterTransitionSet.setDuration(MOVE_DEFAULT_TIME);
        fragment.setSharedElementEnterTransition(enterTransitionSet);
        if(fragment == signInFragment){
            createAccountButton.setText(R.string.create_account_button);
            createAccountButton.setOnClickListener(createAccountButtonClickListener);
            if(signUpFragment.getButton() != null) {
                transaction.addSharedElement(signUpFragment.getButton(), BUTTON_TRANSITION);
            }
        }
        if(fragment == signUpFragment){
            createAccountButton.setText(R.string.already_have_account_button);
            createAccountButton.setOnClickListener(alreadyHaveAccountButtonClickListener);
            if(signInFragment.getButton() != null) {
                transaction.addSharedElement(signInFragment.getButton(), BUTTON_TRANSITION);
            }
        }
        transaction.replace(R.id.placeholder, fragment);
        transaction.commit();
    }

    private void signIn(String email, String password) {
        if(email.length() == 0){
            Toast.makeText(this, R.string.email_empty_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isEmailValid(email)){
            Toast.makeText(this, R.string.email_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length() == 0){
            Toast.makeText(this, R.string.password_empty_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Intent startApp = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(startApp);
            } else {
                Toast.makeText(getApplicationContext(), task.getException() != null ? task.getException().getMessage() : getString(R.string.error_occurred_message), Toast.LENGTH_SHORT).show();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void signUp(String email, String name, String password) {
        if(email.length() == 0){
            Toast.makeText(this, R.string.email_empty_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isEmailValid(email)){
            Toast.makeText(this, R.string.email_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        if(name.length() == 0){
            Toast.makeText(this, R.string.name_empty_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length() == 0){
            Toast.makeText(this, R.string.password_empty_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).setPhotoUri(Uri.parse("gs://collabocode-moonface.appspot.com/profile-2398782_1280.png")).build();
                Objects.requireNonNull(task.getResult()).getUser().updateProfile(profileUpdates);
                User user = new User();
                user.setName(name);
                user.setProfilePicUrl("gs://collabocode-moonface.appspot.com/ic_profile_colored_24dp.png");
                FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(task.getResult()).getUser().getUid()).set(user).addOnCompleteListener(task1 -> {
                    Intent startApp = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(startApp);
                });
            } else {
                Toast.makeText(getApplicationContext(), task.getException() != null ? task.getException().getMessage() : getString(R.string.error_occurred_message), Toast.LENGTH_SHORT).show();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void signIn(GoogleApiClient mGoogleApiClient) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isEmailValid(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } else {
                Toast.makeText(this, R.string.sign_in_failed_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        User user = new User();
                        user.setName(account.getDisplayName());
                        user.setProfilePicUrl(Objects.requireNonNull(account.getPhotoUrl()).toString());

                        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(task.getResult()).getUser().getUid()).set(user).addOnCompleteListener(task1 -> {
                            Intent startApp = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(startApp);
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException() != null ? task.getException().getMessage() : getString(R.string.error_occurred_message), Toast.LENGTH_SHORT).show();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.VISIBLE);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        User user = new User();
                        user.setName(Objects.requireNonNull(task.getResult()).getUser().getDisplayName());
                        user.setProfilePicUrl(Objects.requireNonNull(task.getResult().getUser().getPhotoUrl()).toString());

                        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(task.getResult()).getUser().getUid()).set(user).addOnCompleteListener(task1 -> {
                            Intent startApp = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(startApp);
                        });
                    } else {
                        LoginManager.getInstance().logOut();
                        Toast.makeText(getApplicationContext(), task.getException() != null ? task.getException().getMessage() : getString(R.string.error_occurred_message), Toast.LENGTH_SHORT).show();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }
}
