package com.moonface.collabocode;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.ahmadaghazadeh.editor.widget.CodeEditor;

import java.util.Objects;

public class CodeViewerActivity extends AppCompatActivity {

    private static final String CODE = "code";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_viewer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CodeEditor codeEditor = findViewById(R.id.code_editor);
        codeEditor.setLanguage(new JavaLanguage());
        codeEditor.setReadOnly(true);
        codeEditor.setShowExtendedKeyboard(false);
        codeEditor.setEnabled(false);

        codeEditor.setText(getIntent().getStringExtra(CODE), 1);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}
