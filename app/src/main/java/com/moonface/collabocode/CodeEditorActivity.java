package com.moonface.collabocode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.ahmadaghazadeh.editor.widget.CodeEditor;

import java.util.Objects;

public class CodeEditorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CodeEditor codeEditor = findViewById(R.id.code_editor);
        codeEditor.setLanguage(new JavaLanguage());
        codeEditor.setText(getIntent().getStringExtra("code") == null || getIntent().getStringExtra("code").isEmpty() ? "public class MyClass{\n\n    public static void main(String[] args){\n\n    }\n\n}" : getIntent().getStringExtra("code"),1);
        codeEditor.getTextProcessor().setCodeCompletion(false);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        findViewById(R.id.fab).setOnClickListener(v -> {
            CodeTitleDialog codeTitleDialog = new CodeTitleDialog(this, getIntent().getStringExtra("title"), title -> {
                if (!arrayContains(getIntent().getStringArrayExtra("titlesList"), title)) {
                    Intent result = new Intent();
                    result.putExtra("RESULT_TITLE", title);
                    result.putExtra("RESULT_CODE", codeEditor.getText());
                    setResult(Activity.RESULT_OK, result);
                    finish();
                } else {
                    OverwriteCodeDialog overwriteCodeDialog = new OverwriteCodeDialog(this, title, () -> {
                        Intent result = new Intent();
                        result.putExtra("RESULT_TITLE", title);
                        result.putExtra("RESULT_CODE", codeEditor.getText());
                        setResult(Activity.RESULT_OK, result);
                        finish();
                    });
                    overwriteCodeDialog.show();
                }
            });
            codeTitleDialog.show();
        });
    }

    private boolean arrayContains(String[] array, String title){
        for (String t : array){
            if (t.equals(title)){
                return true;
            }
        }
        return false;
    }
}
