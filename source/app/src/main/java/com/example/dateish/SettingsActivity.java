package com.example.dateish;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rizlee.rangeseekbar.RangeSeekBar;

import java.awt.font.NumericShaper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField;
    private TextView mShowAge, mShowDistance, mInvalidDate;
    private Button mBack, mConfirm;
    private ImageView mProfileImage;
    private RadioGroup mChooseSex;
    private View mMale, mFemale;
    private RangeSeekBar mAge;
    private SeekBar mDistance;
    private DatePicker mBirth;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private String userId, name, phone, profileImageUrl, userSex, showSex, distance, birthDate;
    private float minAge, maxAge;
    private Uri resultUri;
    private boolean firstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mProfileImage = (ImageView) findViewById(R.id.profileImage);

        mBirth = (DatePicker) findViewById(R.id.dateOfBirth);
        mInvalidDate = (TextView) findViewById(R.id.invalidDate);

        mChooseSex = (RadioGroup) findViewById(R.id.showMe);

        mAge = (RangeSeekBar) findViewById(R.id.ageSeekBar);
        mShowAge = (TextView) findViewById(R.id.showAge);

        mDistance = (SeekBar) findViewById(R.id.distanceSeekBar);
        mShowDistance = (TextView) findViewById(R.id.showDistance);

        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        if(getSharedPreferences("com.example.dateish", MODE_PRIVATE).getBoolean("firstTime", true)){
            firstTime = true;
            getSharedPreferences("com.example.dateish", MODE_PRIVATE).edit().putBoolean("firstTime", false).apply();
        }
        else
            firstTime = false;

        if (firstTime) {
            new AlertDialog.Builder(this)
                    .setTitle("Additional settings")
                    .setMessage("Hello, this is your first time in settings. Set your birth date and search criteria, so you can start swiping. But be careful, because you can only change your birth date ONCE.")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            mBack.setVisibility(View.INVISIBLE);
        }

        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmp_date = String.valueOf(mBirth.getYear());
                if(String.valueOf(mBirth.getMonth()).length() < 2)
                    tmp_date = tmp_date + "-0" + (mBirth.getMonth() + 1);
                else
                    tmp_date = tmp_date + "-" + (mBirth.getMonth() + 1);
                if(String.valueOf(mBirth.getDayOfMonth()).length() < 2)
                    tmp_date = tmp_date + "-0" + mBirth.getDayOfMonth();
                else
                    tmp_date = tmp_date + "-" + mBirth.getDayOfMonth();
                LocalDate date = LocalDate.parse(tmp_date);
                if(ChronoUnit.YEARS.between(date, LocalDate.now()) < 18){
                    mInvalidDate.setTextColor(Color.parseColor("#fc4103"));
                    mInvalidDate.setText("You must be at least 18 years old");
                }
                else {
                    saveUserInformation();
                    finish();
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }

    private void getUserInfo() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount() > 0){
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if(map.get("name") != null){
                        name = map.get("name").toString();
                        mNameField.setText(name);
                    }
                    if(map.get("phone") != null){
                        phone = map.get("phone").toString();
                        mPhoneField.setText(phone);
                    }
                    if(map.get("dateOfBirth") != null){
                        birthDate = map.get("dateOfBirth").toString();
                        if(!firstTime){
                            mBirth.setEnabled(false);
                        }
                        mBirth.updateDate(Integer.parseInt(birthDate.split("-")[0]), Integer.parseInt(birthDate.split("-")[1]) - 1, Integer.parseInt(birthDate.split("-")[2]));
                    }
                    if(map.get("sex") != null){
                        userSex = map.get("sex").toString();
                    }
                    if(map.get("profileImageUrl") != null){
                        profileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                    }
                    if(map.get("showSex") != null){
                        showSex = map.get("showSex").toString();
                        if(showSex.equals("Male")){
                            mMale = mChooseSex.findViewById(R.id.men);
                            mMale.performClick();
                        }
                        else{
                            mFemale = mChooseSex.findViewById(R.id.women);
                            mFemale.performClick();
                        }
                    }
                    if(map.get("minAge") != null && map.get("maxAge") != null){
                        minAge = Float.parseFloat(map.get("minAge").toString());
                        maxAge = Float.parseFloat(map.get("maxAge").toString());
                        String age = (int) minAge + " - " + (int) maxAge;
                        mShowAge.setText(age);
                        mAge.setCurrentValues(minAge, maxAge);
                        mAge.setListenerRealTime(new RangeSeekBar.OnRangeSeekBarRealTimeListener() {
                            @Override
                            public void onValuesChanging(float v, float v1) {

                            }

                            @Override
                            public void onValuesChanging(int i, int i1) {
                                String age = i + " - " + i1;
                                mShowAge.setText(age);
                            }
                        });
                    }
                    if(map.get("distance") != null){
                        distance = map.get("distance").toString();
                        String dist = distance + " km";
                        mShowDistance.setText(dist);
                        mDistance.setProgress(Integer.parseInt(distance));
                        mDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                String dist = progress + " km";
                                mShowDistance.setText(dist);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveUserInformation() {
        name = mNameField.getText().toString();
        phone = mPhoneField.getText().toString();
        int selectId = mChooseSex.getCheckedRadioButtonId();
        final RadioButton radioButton = (RadioButton) findViewById(selectId);
        minAge = mAge.getCurrentValues().component1();
        maxAge = mAge.getCurrentValues().component2();
        distance = String.valueOf(mDistance.getProgress());
        birthDate = String.valueOf(mBirth.getYear());
        if(String.valueOf(mBirth.getMonth()).length() < 2)
            birthDate = birthDate + "-0" + (mBirth.getMonth() + 1);
        else
            birthDate = birthDate + "-" + (mBirth.getMonth() + 1);
        if(String.valueOf(mBirth.getDayOfMonth()).length() < 2)
            birthDate = birthDate + "-0" + mBirth.getDayOfMonth();
        else
            birthDate = birthDate + "-" + mBirth.getDayOfMonth();
        Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("phone", phone);
        userInfo.put("dateOfBirth", birthDate);
        if(radioButton.getText().toString().equals("Men"))
            userInfo.put("showSex", "Male");
        else
            userInfo.put("showSex", "Female");
        userInfo.put("minAge", (int) minAge);
        userInfo.put("maxAge", (int) maxAge);
        userInfo.put("distance", Integer.parseInt(distance));
        mUserDatabase.updateChildren(userInfo);
        if(resultUri != null){
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map newImage = new HashMap();
                            newImage.put("profileImageUrl", uri.toString());
                            mUserDatabase.updateChildren(newImage);
                            return;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            finish();
                            return;
                        }
                    });
                }
            });
        }
        else{
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
        }
    }
}