package com.example.postdaily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class RequestActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DatabaseReference profileUserRef, UsersRef, FollowReqRef, FollowRef;
    private Button sendFollowRequestBtn, declineFollowRequestBtn;
    private FirebaseAuth mAuth;
    private RecyclerView myReqList;
    String currentUserID, currentState;
    private String saveCurrentDate;
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        mToolbar = findViewById(R.id.request_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Request Details");

        mAuth = FirebaseAuth.getInstance();
        myReqList = (RecyclerView) findViewById(R.id.req_list);
        myReqList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myReqList.setLayoutManager(linearLayoutManager);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FollowReqRef = FirebaseDatabase.getInstance().getReference().child("FollowRequests");
        FollowRef = FirebaseDatabase.getInstance().getReference().child("Friends");

    }

    @Override
    protected void onStart() {
        super.onStart();
        displayAllRequest();
    }

    private void displayAllRequest() {
        currentUserID = mAuth.getCurrentUser().getUid();
        FirebaseRecyclerOptions<FindFriends> options=
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(FollowReqRef.child(currentUserID),FindFriends.class)
                        .build();

        FirebaseRecyclerAdapter<FindFriends,RequestViewHolder> adapter=
                new FirebaseRecyclerAdapter<FindFriends, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull FindFriends model) {
                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                        final String list_user_id=getRef(position).getKey();

                        DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    String type =dataSnapshot.getValue().toString();

                                    if(type.equals("received")){
                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("image")){
                                                    final String requestProfileImage=dataSnapshot.child("image").getValue().toString();

                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);
                                                }

                                                final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus=dataSnapshot.child("status").getValue().toString();
                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("Wants to connect with you...");

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[]=new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"
                                                                };

                                                        AlertDialog.Builder builder=new AlertDialog.Builder(RequestActivity.this);
                                                        builder.setTitle(requestUserName + "  Friend Request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                if(which == 0){
                                                                    Calendar calForDate=Calendar.getInstance();
                                                                    SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMM-yyyy");
                                                                    saveCurrentDate=currentDate.format(calForDate.getTime());
                                                                    FollowRef.child(currentUserID).child(list_user_id).child("date").setValue(saveCurrentDate)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        FollowRef.child(list_user_id).child(currentUserID).child("date").setValue(saveCurrentDate)
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            FollowReqRef.child(currentUserID).child(list_user_id)
                                                                                                                    .removeValue()
                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                            if(task.isSuccessful()){
                                                                                                                                FollowReqRef.child(list_user_id).child(currentUserID)
                                                                                                                                        .removeValue()
                                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                            @Override
                                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                                if(task.isSuccessful()){
                                                                                                                                                    currentState="friends";
                                                                                                                                                    Toast.makeText(RequestActivity.this, "New Friend Added", Toast.LENGTH_SHORT).show();
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
                                                                if(which == 1){
                                                                    FollowReqRef.child(currentUserID).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        FollowReqRef.child(list_user_id).child(currentUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful()){
                                                                                                            currentState="not_friends";
                                                                                                            Toast.makeText(RequestActivity.this, "Request Declined", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                                        builder.show();

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }

                                    else if(type.equals("sent")){
                                        Button request_send_btn= holder.itemView.findViewById(R.id.request_accept_btn);
                                        request_send_btn.setText("Req. send");

                                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);

                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("image")){
                                                    final String requestProfileImage=dataSnapshot.child("image").getValue().toString();

                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);
                                                }

                                                final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus=dataSnapshot.child("status").getValue().toString();
                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("You have send a request to "+ requestUserName);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[]=new CharSequence[]
                                                                {

                                                                        "Cancel Friend Request"
                                                                };

                                                        AlertDialog.Builder builder=new AlertDialog.Builder(RequestActivity.this);
                                                        builder.setTitle("Already send request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                currentState="request_sent";
                                                                if(which == 1)
                                                                {
                                                                    FollowReqRef.child(currentUserID).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        FollowReqRef.child(list_user_id).child(currentUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful()){
                                                                                                            Toast.makeText(RequestActivity.this, "You have cancel the chat request...", Toast.LENGTH_SHORT).show();
                                                                                                            currentState="not_friends";
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                                        builder.show();

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_frnd_req, parent ,false);
                        RequestViewHolder holder= new RequestViewHolder(view);
                        return holder;
                    }
                };

        myReqList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptButton, cancelButton;


        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            acceptButton=itemView.findViewById(R.id.request_accept_btn);
            cancelButton=itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}


