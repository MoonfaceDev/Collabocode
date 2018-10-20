package com.moonface.collabocode;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class SignInFragment extends Fragment {

    private TextView emailInput, passwordInput;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        TextView forgotPasswordButton = view.findViewById(R.id.forgot_password_button);
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });

        emailInput = view.findViewById(R.id.input_email);
        passwordInput = view.findViewById(R.id.input_password);
    }

    public String getEmail(){
        return emailInput.getText().toString();
    }

    public String getPassword(){
        return passwordInput.getText().toString();
    }

    private void forgotPassword(){
        ForgotPasswordDialog forgotPasswordDialog = new ForgotPasswordDialog(getActivity(), new ForgotPasswordDialog.OnSentPressedListener() {
            @Override
            public void onButtonClick(String email) {
                sendForgotPasswordEmail(email);
            }
        });
        forgotPasswordDialog.show();
    }

    private void sendForgotPasswordEmail(String email){
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.email_sent_message, Toast.LENGTH_SHORT).show();
                } else {
                    final String errorMessage = task.getException() != null ? task.getException().getMessage() : "";
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
