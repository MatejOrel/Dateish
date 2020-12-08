package com.example.dateish;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dateish.Cards.arrayAdapter;
import com.example.dateish.Cards.cards;
import com.example.dateish.Matches.MatchesActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private com.example.dateish.Cards.arrayAdapter arrayAdapter;

    private FirebaseAuth mAuth;
    private String currentUid;
    private DatabaseReference usersDb;

    List<cards> rowItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();

        checkUserSex();
        rowItems = new ArrayList<>();

        arrayAdapter = new arrayAdapter(this, R.layout.item, rowItems);

        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                cards obj = (cards) dataObject;
                String userId = obj.getUserId();
                usersDb.child(userId).child("connections").child("nope").child(currentUid).setValue(LocalDateTime.now().toString());
                usersDb.child(userId).child("connections").child("yeps").child(currentUid).removeValue();
                Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT).show();
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onRightCardExit(Object dataObject) {
                cards obj = (cards) dataObject;
                String userId = obj.getUserId();
                usersDb.child(userId).child("connections").child("yeps").child(currentUid).setValue(LocalDateTime.now().toString());
                usersDb.child(userId).child("connections").child("nope").child(currentUid).removeValue();
                isConnectionMatch(userId);
                Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {

            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Toast.makeText(MainActivity.this, "click", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void isConnectionMatch(String userId){
        DatabaseReference currentUserConnectionsDb = usersDb.child(currentUid).child("connections").child("yeps").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Toast.makeText(MainActivity.this, "new Connection", Toast.LENGTH_LONG).show();

                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    usersDb.child(snapshot.getKey()).child("connections").child("matches").child(currentUid).child("chatId").setValue(key);
                    usersDb.child(currentUid).child("connections").child("matches").child(snapshot.getKey()).child("chatId").setValue(key);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String oppositeUserSex;
    public void checkUserSex(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child("sex").getValue() != null){

                        oppositeUserSex = snapshot.child("showSex").getValue().toString();
                        getSearches();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getSearches(){
        usersDb.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.child("sex").getValue() != null) {
                    if (snapshot.exists() &&
                            (!snapshot.child("connections").child("nope").hasChild(currentUid) || ChronoUnit.DAYS.between(LocalDateTime.parse((CharSequence) snapshot.child("connections").child("nope").child(currentUid).getValue()), LocalDateTime.now()) > 7) &&
                            (!snapshot.child("connections").child("yeps").hasChild(currentUid) || ChronoUnit.DAYS.between(LocalDateTime.parse((CharSequence) snapshot.child("connections").child("yeps").child(currentUid).getValue()), LocalDateTime.now()) > 7) &&
                            snapshot.child("sex").getValue().toString().equals(oppositeUserSex) &&
                            !snapshot.getKey().equals(currentUid.toString())) {
                        cards item = new cards(snapshot.getKey(), snapshot.child("name").getValue().toString(), snapshot.child("profileImageUrl").getValue().toString());
                        rowItems.add(item);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }

            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void logoutUser(View view) {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(intent);
        finish();
        return;
    }

    public void goToSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        return;
    }

    public void goToMatches(View view) {
        Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
        startActivity(intent);
        return;
    }
}