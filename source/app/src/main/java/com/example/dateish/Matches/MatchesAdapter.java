package com.example.dateish.Matches;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dateish.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesViewHolders> {

    private List<MatchesObject> matchesList;
    private Context context;
    private DatabaseReference mUserDatabase;
    private String currentUid, chatId;
    private FirebaseAuth mAuth;

    public MatchesAdapter(List<MatchesObject> matchesList, Context context){
        this.matchesList = matchesList;
        this.context = context;
    }
    @NonNull
    @Override
    public MatchesViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matches, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        MatchesViewHolders rcv = new MatchesViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull MatchesViewHolders holder, int position) {
        holder.mUnmatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Unmatch")
                        .setMessage("Are you sure you want to unmatch with this person?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mAuth = FirebaseAuth.getInstance();
                                currentUid = mAuth.getCurrentUser().getUid();
                                mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid).child("connections").child("yeps").child(matchesList.get(position).getUserId());
                                mUserDatabase.removeValue();
                                mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(matchesList.get(position).getUserId()).child("connections").child("yeps").child(currentUid);
                                mUserDatabase.removeValue();
                                mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(matchesList.get(position).getUserId()).child("connections").child("matches").child(currentUid);
                                chatId = mUserDatabase.child("chatId").toString();
                                mUserDatabase.removeValue();
                                mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid).child("connections").child("matches").child(matchesList.get(position).getUserId());
                                mUserDatabase.removeValue();
                                mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(chatId);
                                mUserDatabase.removeValue();
                                // Continue with delete operation
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });
        holder.mMatchId.setText(matchesList.get(position).getUserId());
        holder.mMatchId.setVisibility(View.GONE);
        holder.mMatchName.setText(matchesList.get(position).getName());
        Glide.with(context).load(matchesList.get(position).getProfileImageUrl()).into(holder.mMatchImage);
    }

    @Override
    public int getItemCount() {
        return this.matchesList.size();
    }
}
