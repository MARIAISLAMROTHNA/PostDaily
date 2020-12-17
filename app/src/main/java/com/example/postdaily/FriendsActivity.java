package com.example.postdaily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.UserRecoverableException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {
    private RecyclerView myFriendList;
    private DatabaseReference FriendsRef,UserRef;
    private FirebaseAuth mAuth;
    private String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth=FirebaseAuth.getInstance();
        uid=mAuth.getCurrentUser().getUid();
        FriendsRef= FirebaseDatabase.getInstance().getReference().child("Friends").child(uid);
        UserRef=FirebaseDatabase.getInstance().getReference().child("Users");


        myFriendList=(RecyclerView)findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);
        
        DisplayAllFriends();
    }

    private void DisplayAllFriends() {
        FirebaseRecyclerOptions<Friends> options=new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(FriendsRef,Friends.class)
                .build();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder>adapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, final int position, @NonNull Friends model) {
                holder.FriendDate.setText("Friends Since:"+model.getDate());


                final String userIds=getRef(position).getKey();
               UserRef.child(userIds).addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       if(snapshot.exists()){
                           final String retrieveProfileImage=snapshot.child("image").getValue().toString();
                           final String retrieveUserName=snapshot.child("name").getValue().toString();

                           holder.userName.setText(retrieveUserName);
                          Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {

                   }
               });

            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_display,parent,false);
                FriendsViewHolder viewHolder= new FriendsViewHolder(view);
                return viewHolder;
            }
        };
        myFriendList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,FriendDate;
        CircleImageView profileImage;
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.all_user_name);
            FriendDate=itemView.findViewById(R.id.all_user_status);
            profileImage=itemView.findViewById(R.id.all_user_profile_img);

        }
    }
}