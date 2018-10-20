package com.moonface.collabocode;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SignUpFragment extends Fragment {

    private TextView emailInput, nameInput, passwordInput;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){

        emailInput = view.findViewById(R.id.input_email);
        nameInput = view.findViewById(R.id.input_name);
        passwordInput = view.findViewById(R.id.input_password);
    }

    public String getEmail(){
        return emailInput.getText().toString();
    }

    public String getName() {
        return nameInput.getText().toString();
    }

    public String getPassword(){
        return passwordInput.getText().toString();
    }
}
