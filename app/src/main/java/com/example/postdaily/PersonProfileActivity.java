package com.example.postdaily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {
    private TextView userName, userStatus,userCountry,userGender;
    private CircleImageView userProfileImage;
    private Button sendFollowRequestBtn,declineFollowRequestBtn;
    private Toolbar mToolbar;
    private DatabaseReference profileUserRef,UserRef,FollowReqRef,FollowRef;
    private FirebaseAuth mAuth;
    private String senderUserId,receiverUserId,currentState;
    private String saveCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mToolbar = findViewById(R.id.Person_profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Person Details");

        mAuth=FirebaseAuth.getInstance();
        senderUserId=mAuth.getCurrentUser().getUid();

        receiverUserId=getIntent().getExtras().get("visitUserId").toString();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        FollowReqRef=FirebaseDatabase.getInstance().getReference().child("FollowRequests");
        FollowRef=FirebaseDatabase.getInstance().getReference().child("Friends");




    InitializeField();
        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))&& (dataSnapshot.hasChild("status"))&& (dataSnapshot.hasChild("country"))&& (dataSnapshot.hasChild("gender"))){
                            String retrieveProfileImage=dataSnapshot.child("image").getValue().toString();
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus=dataSnapshot.child("status").getValue().toString();
                            String retriveCountry=dataSnapshot.child("country").getValue().toString();
                            String retriveGender=dataSnapshot.child("gender").getValue().toString();
                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                            userCountry.setText(retriveCountry);
                            userGender.setText(retriveGender);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                            MaintainFriendRequest();

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        declineFollowRequestBtn.setVisibility(View.INVISIBLE);
        declineFollowRequestBtn.setEnabled(false);

        if(!senderUserId.equals(receiverUserId))
        {

            sendFollowRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFollowRequestBtn.setEnabled(false);
                    if(currentState.equals("not_friends"))
                    {
                        sendFollowReqToPerson();
                    }
                    if(currentState.equals("request_sent"))
                    {
                        cancelFriendRequest();
                    }
                    if(currentState.equals("request_received"))
                    {
                        acceptFriendReqest();
                    }
                    if (currentState.equals("friends"))
                    {
                        UnfollowAnExistingFriend();
                    }
                }

            });
        }
        else{
            sendFollowRequestBtn.setVisibility(View.INVISIBLE);
            declineFollowRequestBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void UnfollowAnExistingFriend()
    {
        FollowRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FollowRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendFollowRequestBtn.setEnabled(true);
                                                currentState="not_friends";
                                                sendFollowRequestBtn.setText("Send Friend Request");

                                                declineFollowRequestBtn.setVisibility(View.INVISIBLE);
                                                declineFollowRequestBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendReqest()
    {
        Calendar calForDate=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMM-yyyy");
        saveCurrentDate=currentDate.format(calForDate.getTime());
        FollowRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            FollowRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                FollowReqRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    FollowReqRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        sendFollowRequestBtn.setEnabled(true);
                                                                                        currentState="friends";
                                                                                        sendFollowRequestBtn.setText("Unfriend");
                                                                                        declineFollowRequestBtn.setVisibility(View.INVISIBLE);
                                                                                        declineFollowRequestBtn.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });

                        }
                    }
                });

    }

    private void cancelFriendRequest() {
        FollowReqRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FollowReqRef.child(receiverUserId).child(senderUserId)
                                   .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendFollowRequestBtn.setEnabled(true);
                                                currentState="not_friends";
                                                sendFollowRequestBtn.setText("Send Friend Request");
                                                declineFollowRequestBtn.setVisibility(View.INVISIBLE);
                                                declineFollowRequestBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void MaintainFriendRequest()
    {
        FollowReqRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild(receiverUserId))
                        {
                            String request_type=snapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if(request_type.equals("sent"))
                            {
                                currentState="request_sent";
                                sendFollowRequestBtn.setText("Cancel Friend Request");
                                declineFollowRequestBtn.setVisibility(View.INVISIBLE);
                                declineFollowRequestBtn.setEnabled(false);
                            }
                            else if(request_type.equals("received"))
                            {
                                currentState="request_received";
                                sendFollowRequestBtn.setText("Accept Friend Request");
                                declineFollowRequestBtn.setVisibility(View.VISIBLE);
                                declineFollowRequestBtn.setEnabled(true);
                                declineFollowRequestBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelFriendRequest();
                                    }
                                });
                            }

                        }
                        else
                        {
                            FollowRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.hasChild(receiverUserId))
                                            {
                                                currentState="friends";
                                                sendFollowRequestBtn.setText("Unfriend");

                                                declineFollowRequestBtn.setVisibility(View.INVISIBLE);
                                                declineFollowRequestBtn.setEnabled(false);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendFollowReqToPerson()
    {
        FollowReqRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FollowReqRef.child(receiverUserId).child(senderUserId)
                            .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        sendFollowRequestBtn.setEnabled(true);
                                        currentState="request_sent";
                                        sendFollowRequestBtn.setText("Cancel Friend Request");
                                        declineFollowRequestBtn.setVisibility(View.INVISIBLE);
                                        declineFollowRequestBtn.setEnabled(false);
                                    }
                                }
                            });
                }
            }
        });

    }

    private void InitializeField()
    {
        sendFollowRequestBtn=findViewById(R.id.person_send_follow_req);
        declineFollowRequestBtn=findViewById(R.id.person_decline_follow_req);
        userName=findViewById(R.id.person_full_name);
        userStatus=findViewById(R.id.person_status);
        userProfileImage=findViewById(R.id.person_profile_pic);
        userCountry=findViewById(R.id.person_country);
        userGender=findViewById(R.id.person_gender);
        currentState="not_friends";

    }

}