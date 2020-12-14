package com.example.dateish;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private Button mRegister;
    private EditText mEmail, mPassword, mRPassword, mName, mLastName;
    private CheckBox mShowPassword;

    private RadioGroup mRadioGroup;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(RegistrationActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mRegister = (Button) findViewById(R.id.register);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mRPassword = (EditText) findViewById(R.id.rpassword);
        mName = (EditText) findViewById(R.id.name);
        mLastName = (EditText) findViewById(R.id.lastname);
        mShowPassword = (CheckBox) findViewById(R.id.showPass);

        mShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mRPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else {
                    mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mRPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }

            }
        });

        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences("com.example.dateish", MODE_PRIVATE).edit().putBoolean("firstTime", true).apply();
                int selectId = mRadioGroup.getCheckedRadioButtonId();

                final RadioButton radioButton = (RadioButton) findViewById(selectId);

                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String rpassword = mRPassword.getText().toString();
                final String name = mName.getText().toString();
                final String lastName = mLastName.getText().toString();
                final String url = "https://firebasestorage.googleapis.com/v0/b/dateish-5d381.appspot.com/o/profileImages%2Fno-profile-picture-300x216.jpg?alt=media&token=97567e68-2b5e-4921-aabd-8d02175035c2";
                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(rpassword) || TextUtils.isEmpty(name) || TextUtils.isEmpty(lastName) || !radioButton.isChecked()) {
                    Toast.makeText(RegistrationActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(radioButton.getText() == null){
                        return;
                    }
                    while(!password.equals(rpassword))
                        Toast.makeText(RegistrationActivity.this, "Passwords must match", Toast.LENGTH_SHORT).show();
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                task.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegistrationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                String userId = mAuth.getCurrentUser().getUid();
                                DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                                Map userInfo = new HashMap<>();
                                userInfo.put("name", name + " " + lastName);
                                userInfo.put("sex", radioButton.getText().toString());
                                userInfo.put("profileImageUrl", url);
                                if (radioButton.getText().toString().equals("Male"))
                                    userInfo.put("showSex", "Female");
                                else
                                    userInfo.put("showSex", "Male");
                                userInfo.put("minAge", 18);
                                userInfo.put("maxAge", 100);
                                userInfo.put("distance", 142);
                                currentUserDb.updateChildren(userInfo);
                            }
                        }
                    });

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }
}