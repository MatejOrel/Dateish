package com.example.dateish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class ShowProfile extends AppCompatActivity {

    private TextView mName, mDistance, mBio;
    private ImageView mProfileImage;
    private DatabaseReference mUserDatabase;
    private String name, profileImageUrl, distance, bio, userId;
    private Uri resultUri;
    private double latitude, longtitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);

        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        mName = (TextView) findViewById(R.id.name);
        mDistance = (TextView) findViewById(R.id.distance);
        mBio = (TextView) findViewById(R.id.bio);

        userId = getIntent().getStringExtra("userId");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        getUserInfo();
    }

    private void getUserInfo() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount() > 0){
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if(map.get("name") != null){
                        name = map.get("name").toString() + ", " + ChronoUnit.YEARS.between(LocalDate.parse((CharSequence) map.get("dateOfBirth")), LocalDate.now());
                        mName.setText(name);
                    }
                    if(map.get("bio") != null){
                        bio = map.get("bio").toString();
                        mBio.setText(bio);
                    }
                    if(map.get("profileImageUrl") != null){
                        profileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                    }
                    if(map.get("latitude") != null && map.get("longtitude") != null){
                        latitude = Double.parseDouble(map.get("latitude").toString());
                        longtitude = Double.parseDouble(map.get("longtitude").toString());
                        mBio.setText(bio);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}