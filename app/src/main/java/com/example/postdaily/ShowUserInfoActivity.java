package com.example.postdaily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowUserInfoActivity extends AppCompatActivity {
    private Button myPostBtn,myFriendsBtn;
    private TextView userName, userStatus,userCountry,userGender;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,FriendsRef,PostRef;
    private Toolbar mToolbar;
    private int countFriends=0,countPosts=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user_info);

        mToolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Profile");


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendsRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        PostRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        InitializeFields();
        RetrieveUserInfo();
        myFriendsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMyFriendsActivity();
            }
        });

        FriendsRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    countFriends=(int)snapshot.getChildrenCount();
                    myFriendsBtn.setText(Integer.toString(countFriends)+ " Friends");
                }
                else
                {
                    myFriendsBtn.setText("0 Friends");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        myPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMyPostActivity();
            }
        });

        PostRef.orderByChild("uid")
                .startAt(currentUserID).endAt(currentUserID + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            countPosts=(int)snapshot.getChildrenCount();
                            myPostBtn.setText(Integer.toString(countPosts) + " Posts");

                        }
                        else
                        {
                            myPostBtn.setText("0 Posts");

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
    private void InitializeFields() {
        myFriendsBtn=findViewById(R.id.myFriends);
        myPostBtn=findViewById(R.id.myPost);
        userName=findViewById(R.id.my_full_name);
        userStatus=findViewById(R.id.my_status);
        userProfileImage=findViewById(R.id.my_profile_pic);
        userCountry=findViewById(R.id.my_country);
        userGender=findViewById(R.id.my_gender);

    }
        private void RetrieveUserInfo() {
            UserRef.child(currentUserID)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists())
                            {
                                String retrieveProfileImage=snapshot.child("image").getValue().toString();
                                String retrieveUserName=snapshot.child("name").getValue().toString();
                                String retrieveStatus=snapshot.child("status").getValue().toString();
                                String retriveCountry=snapshot.child("country").getValue().toString();
                                String retriveGender=snapshot.child("gender").getValue().toString();
                                userName.setText(retrieveUserName);
                                userStatus.setText(retrieveStatus);
                                userCountry.setText(retriveCountry);
                                userGender.setText(retriveGender);
                                Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    private void SendUserToMyFriendsActivity() {
        Intent friendsIntent = new Intent(ShowUserInfoActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }
    private void SendUserToMyPostActivity() {
        Intent myPostIntent = new Intent(ShowUserInfoActivity.this, MyPostsActivity.class);
        startActivity(myPostIntent);
    }

        private void SendUserToMainActivity() {
            Intent mainIntent =new Intent(ShowUserInfoActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            finish();
        }

    }
