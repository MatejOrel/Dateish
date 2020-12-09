package com.example.dateish;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField;
    private TextView mShowAge, mShowDistance;
    private Button mBack, mConfirm;
    private ImageView mProfileImage;
    private RadioGroup mChooseSex;
    private View mMale, mFemale;
    private RangeSeekBar mAge;
    private SeekBar mDistance;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private String userId, name, phone, profileImageUrl, userSex, showSex, distance;
    private float minAge, maxAge;
    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mProfileImage = (ImageView) findViewById(R.id.profileImage);

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
                saveUserInformation();
                finish();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
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
                        minAge = (Long) map.get("minAge");
                        maxAge = (Long) map.get("maxAge");
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

        Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("phone", phone);
        if(radioButton.getText().toString().equals("Men"))
            userInfo.put("showSex", "Male");
        else
            userInfo.put("showSex", "Female");
        userInfo.put("minAge", minAge);
        userInfo.put("maxAge", maxAge);
        userInfo.put("distance", distance);
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