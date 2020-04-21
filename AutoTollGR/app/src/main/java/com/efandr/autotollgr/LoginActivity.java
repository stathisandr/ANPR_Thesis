package com.efandr.autotollgr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    public FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private EditText lemail,lpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        lemail = (EditText)findViewById(R.id.login_e_mail);
        lpassword =(EditText)findViewById(R.id.login_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
         currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            LoginActivity.this.startActivity(new Intent(LoginActivity.this, MainActivity.class));
            LoginActivity.this.finish();
        }
    }
    // [END on_start_check_user]

    private void signIn(String email, String password) {

        if (!validateForm()) {
            return;
        }

        //showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //FirebaseUser user = mAuth.getCurrentUser();
                            LoginActivity.this.startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            LoginActivity.this.finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = lemail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            lemail.setError("Required.");
            valid = false;
        } else {
            lemail.setError(null);
        }

        String password = lpassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            lpassword.setError("Required.");
            valid = false;
        } else {
            lpassword.setError(null);
        }

        return valid;
    }

    public void onClick(View v) {

        switch(v.getId()){

            case R.id.login_login_button:
                signIn(lemail.getText().toString(), lpassword.getText().toString());
                break;
            case R.id.login_register_textview:
                Intent regintent = new Intent(this,RegisterActivity.class);
                this.startActivity(regintent);
                finish();
                break;
        }
    }

}
