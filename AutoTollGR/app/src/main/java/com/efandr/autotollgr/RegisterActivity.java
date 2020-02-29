package com.efandr.autotollgr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    public String LPlate;

    private LinearLayout register, userinfo, cardinfo;

    private EditText remail,rpassword,rrpassword,rusername,rlicenceplate,bcholder,bcnumber,bcvv,bcexpire;
    private Spinner rvehicletype;

    private Button registerbtn, enrollbtn, bankbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        register = (LinearLayout)findViewById(R.id.register_register_linearlayout);
        userinfo = (LinearLayout)findViewById(R.id.register_userinfo_linearlayout);
        cardinfo = (LinearLayout)findViewById(R.id.register_carinfo__linearlayout);

        remail = (EditText)findViewById(R.id.register_e_mail);
        rpassword = (EditText)findViewById(R.id.register_password);
        rrpassword = (EditText)findViewById(R.id.register_re_type_password);
        rusername = (EditText)findViewById(R.id.register_username);
        rlicenceplate = (EditText)findViewById(R.id.register_licenceplate);
        rvehicletype = (Spinner)findViewById(R.id.register_vehicle_type);

        bcholder = (EditText)findViewById(R.id.register_bank_holder);
        bcnumber = (EditText)findViewById(R.id.register_bank_cardnumber);
        bcvv = (EditText)findViewById(R.id.register_bank_cvv);
        bcexpire = (EditText)findViewById(R.id.register_bank_expire_day);

        registerbtn = (Button)findViewById(R.id.register_register_button);
        enrollbtn = (Button)findViewById(R.id.register_userinfo_button);
        bankbtn = (Button)findViewById(R.id.register_bank_button);

        registerbtn.setOnClickListener(this);
        enrollbtn.setOnClickListener(this);
        bankbtn.setOnClickListener(this);
    }

    private void signUp() {

        //showProgressDialog();

        String email = remail.getText().toString();
        String password = rpassword.getText().toString();
        String repassword = rrpassword.getText().toString();

        if(!validSingUp(email,password,repassword)){
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //hideProgressDialog();

                        if (task.isSuccessful()) {
                            onAuthSuccess();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Sign Up Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validCardInfo(String cardholder, String cardnumber, String cardcvv, String cardexpday){
        boolean valid = true;

        if (TextUtils.isEmpty(cardholder)) {
            bcholder.setError("Required.");
            valid = false;
        } else {
            remail.setError(null);
        }

        if (TextUtils.isEmpty(cardnumber)) {
            bcnumber.setError("Required.");
            valid = false;
        } else if(cardnumber.length()<16){
            bcnumber.setError("At least 16 numbers");
        }else {
            bcnumber.setError(null);
        }

        if (TextUtils.isEmpty(cardcvv)){
            bcvv.setError("Required");
            valid = false;
        } else if(cardcvv.length()<3){
            bcvv.setError("At least 3 numbers");
            valid = false;
        } else {
            bcvv.setError(null);
        }

        if (TextUtils.isEmpty(cardexpday)){
            bcexpire.setError("Required");
            valid = false;
        } else if(!(cardexpday.length()==4)){
            bcexpire.setError("Should be 4 numbers");
            valid = false;
        } else if(cardexpday.contains("/")){
            bcexpire.setError("Must be in form MMYY");
            valid = false;
        }

        return valid;
    }

    private boolean validSingUp(String email, String password, String repassword){
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            remail.setError("Required.");
            valid = false;
        } else {
            remail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            rpassword.setError("Required.");
            valid = false;
        } else {
            rpassword.setError(null);
        }

        if (TextUtils.isEmpty(repassword)){
            rrpassword.setError("Required");
            valid = false;
        } else if(!password.equals(repassword)){
            rrpassword.setError("Password doesn't match");
            valid = false;
        } else {
            rrpassword.setError(null);
        }
        return valid;
    }

    private void onAuthSuccess() {
        register.setVisibility(View.GONE);
        userinfo.setVisibility(View.VISIBLE);
    }

    private void onCarInfoSuccess(){
        userinfo.setVisibility(View.GONE);
        cardinfo.setVisibility(View.VISIBLE);
    }

    // [START basic_write]
    private void writeNewUser(String userId, String email, String name, String licenceplate, String vehicletype, String userid, String password) {
        User user = new User(email, name, licenceplate, vehicletype,userid, password);

        mDatabase.child("Users").child(licenceplate).setValue(user);
    }

    private void writeNewCard(String holder, String number, String cvv, String expireday, String userid){
        CreditCard card = new CreditCard(holder,number,cvv,expireday,userid);

        mDatabase.child("Cards").child(userid).setValue(card);
    }

    private void card(FirebaseUser user){
        String cholder,cnumber,cvv,cexpireday,cuserid;

        cholder = bcholder.getText().toString();
        cnumber = bcnumber.getText().toString();
        cvv = bcvv.getText().toString();
        cexpireday = bcexpire.getText().toString();
        String userid = user.getUid();

        if(!validCardInfo(cholder,cnumber,cvv,cexpireday)){
            return;
        }

        writeNewCard(cholder,cnumber,cvv,cexpireday,userid);

        // Go to MainActivity
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }

    private boolean validCarInfo(String username, String licenceplate){
        boolean valid = true;

        if (TextUtils.isEmpty(username)) {
            rusername.setError("Required.");
            valid = false;
        } else {
            rusername.setError(null);
        }

        if (TextUtils.isEmpty(licenceplate)) {
            rlicenceplate.setError("Required.");
            valid = false;
        } else {
            rlicenceplate.setError(null);
        }
        return valid;
    }

    private void enroll(FirebaseUser user){
        String email, username, licencepl, vehiclety, password;

        LPlate = rlicenceplate.getText().toString();

        email = remail.getText().toString();
        username = rusername.getText().toString();
        licencepl = rlicenceplate.getText().toString();
        vehiclety = rvehicletype.getSelectedItem().toString();
        password = rpassword.getText().toString();
        String userid = user.getUid();

        if(!validCarInfo(username,licencepl)){
            return;
        }

        // Write new user
        writeNewUser(user.getUid(), email, username, licencepl, vehiclety,userid, password);

        onCarInfoSuccess();
    }

    public void onClick(View v) {

        switch(v.getId()){
            case R.id.register_bank_button:
                card(mAuth.getCurrentUser());
                break;
            case R.id.register_userinfo_button:
                enroll(mAuth.getCurrentUser());
                break;
            case R.id.register_register_button:
                signUp();
                break;
            case R.id.register_login_textview:
                Intent logintent = new Intent(this,LoginActivity.class);
                this.startActivity(logintent);
                finish();
                break;
        }
    }
}
