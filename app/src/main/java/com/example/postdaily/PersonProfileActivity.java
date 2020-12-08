package com.example.postdaily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {
    private TextView userName, userStatus,userCountry,userGender;
    private CircleImageView userProfileImage;
    private Button sendFollowReq,declineFollowRequest;
    private DatabaseReference profileUserRef,UserRef,FollowReqRef;
    private FirebaseAuth mAuth;
    private String senderUserId,receiverUserId,currentState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth=FirebaseAuth.getInstance();
        senderUserId=mAuth.getCurrentUser().getUid();

        receiverUserId=getIntent().getExtras().get("visitUserId").toString();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        FollowReqRef=FirebaseDatabase.getInstance().getReference().child("FollowRequests");


        InitializeField();
        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))){
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

                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus=dataSnapshot.child("status").getValue().toString();
                            String retriveCountry=dataSnapshot.child("country").getValue().toString();
                            String retriveGender=dataSnapshot.child("gender").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                            userCountry.setText(retriveCountry);
                            userGender.setText(retriveGender);
                            MaintainFriendRequest();
                        }
                        else {
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            userName.setText(retrieveUserName);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        declineFollowRequest.setVisibility(View.INVISIBLE);
        declineFollowRequest.setEnabled(false);

        if(!senderUserId.equals(receiverUserId))
        {

            sendFollowReq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFollowReq.setEnabled(false);
                    if(currentState.equals("not_friends"))
                    {
                        sendFollowReqToPerson();
                    }
                }
            });
        }
        else{
            sendFollowReq.setVisibility(View.INVISIBLE);
            declineFollowRequest.setVisibility(View.INVISIBLE);
        }
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
                                sendFollowReq.setText("Cancel friend request");
                                declineFollowRequest.setVisibility(View.INVISIBLE);
                                declineFollowRequest.setEnabled(false);
                            }

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
                                        sendFollowReq.setEnabled(true);
                                        currentState="request_sent";
                                        sendFollowReq.setText("Cancel Friend Request");
                                        declineFollowRequest.setVisibility(View.INVISIBLE);
                                        declineFollowRequest.setEnabled(false);
                                    }
                                }
                            });
                }
            }
        });

    }

    private void InitializeField()
    {
        sendFollowReq=findViewById(R.id.person_send_follow_req);
        declineFollowRequest=findViewById(R.id.person_decline_follow_req);
        userName=findViewById(R.id.person_full_name);
        userStatus=findViewById(R.id.person_status);
        userProfileImage=findViewById(R.id.person_profile_pic);
        userCountry=findViewById(R.id.person_country);
        userGender=findViewById(R.id.person_gender);
        currentState="not_friends";

    }

}