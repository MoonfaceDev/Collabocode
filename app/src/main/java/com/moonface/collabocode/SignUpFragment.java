package com.moonface.collabocode;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Objects;

public class SignUpFragment extends Fragment {

    private Button signUpButton;
    private TextView emailInput;
    private TextView nameInput;
    private TextView passwordInput;
    private OnSignUpButtonClickListener onSignUpButtonClickListener;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){

        emailInput = view.findViewById(R.id.input_email);
        nameInput = view.findViewById(R.id.input_name);
        passwordInput = view.findViewById(R.id.input_password);
        TextView privacyPolicyLink = view.findViewById(R.id.privacy_policy_link);
        signUpButton = view.findViewById(R.id.sign_up_button);

        privacyPolicyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_link)));
                startActivity(intent);
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onSignUpButtonClickListener != null) {
                    onSignUpButtonClickListener.onClick(emailInput.getText().toString(), nameInput.getText().toString(), passwordInput.getText().toString());
                }
            }
        });
    }

    public View getButton(){
        return signUpButton;
    }

    public void setOnSignUpButtonClickListener(OnSignUpButtonClickListener onSignUpButtonClickListener){
        this.onSignUpButtonClickListener = onSignUpButtonClickListener;
    }

    public interface OnSignUpButtonClickListener{
        void onClick(String email, String name, String password);
    }
}
