package com.example.dateish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class ShowProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView mName, mDistance, mBio;
    private ImageView mProfileImage;
    private DatabaseReference mUserDatabase, mCurrentUserDatabase, usersDb;
    private String name, profileImageUrl, distance, bio, userId;
    private double latitude, longtitude, latitude_u, longtitude_u;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();

        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        mName = (TextView) findViewById(R.id.name);
        mDistance = (TextView) findViewById(R.id.distance);
        mBio = (TextView) findViewById(R.id.bio);

        getLocation();
        userId = getIntent().getStringExtra("userId");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        getUserInfo();
    }

    private void getLocation(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child("latitude").getValue() != null && snapshot.child("longtitude").getValue() != null){
                        latitude = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                        longtitude = Double.parseDouble(snapshot.child("longtitude").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

   /* private void getLocation(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = usersDb.child(user.getUid());
        mCurrentUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        mCurrentUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if(map.get("latitude") != null && map.get("longtitude") != null){
                        latitude = Double.parseDouble(map.get("latitude").toString());
                        longtitude = Double.parseDouble(map.get("longtitude").toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

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
                        latitude_u = Double.parseDouble(map.get("latitude").toString());
                        longtitude_u = Double.parseDouble(map.get("longtitude").toString());
                        distance = String.valueOf((int) distance(latitude, longtitude, latitude_u, longtitude_u)) + " km";
                        mDistance.setText(distance);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = (dist * 60 * 1.1515) / 0.62137;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}