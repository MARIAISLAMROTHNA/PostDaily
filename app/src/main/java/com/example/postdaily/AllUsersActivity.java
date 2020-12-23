package com.example.postdaily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView allUsersList;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        mToolbar = (Toolbar) findViewById(R.id.allUsersToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("All Users");
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");


        allUsersList = findViewById(R.id.all_users_list);
        allUsersList.setHasFixedSize(true);
        allUsersList.setLayoutManager(new LinearLayoutManager(this));
        allUsersDisplay();
    }

    private void allUsersDisplay()
    {
        Toast.makeText(this,"Searching",Toast.LENGTH_SHORT).show();
        FirebaseRecyclerOptions<FindFriends> options=new FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(UsersRef,FindFriends.class)
                .build();

        FirebaseRecyclerAdapter<FindFriends, AllUsersActivity.AllUserDisplayViewHolder> adapter=
                new FirebaseRecyclerAdapter<FindFriends, AllUsersActivity.AllUserDisplayViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull AllUsersActivity.AllUserDisplayViewHolder holder, final int position, @NonNull FindFriends model) {
                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visitUserId=getRef(position).getKey();
                                Intent profileIntent=new Intent(AllUsersActivity.this,PersonProfileActivity.class);
                                profileIntent.putExtra("visitUserId",visitUserId);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public AllUsersActivity.AllUserDisplayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_display,parent,false);
                        AllUsersActivity.AllUserDisplayViewHolder viewHolder= new AllUsersActivity.AllUserDisplayViewHolder(view);
                        return viewHolder;

                    }
                };

        allUsersList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class AllUserDisplayViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView profileImage;

        public AllUserDisplayViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.all_user_name);
            userStatus=itemView.findViewById(R.id.all_user_status);
            profileImage=itemView.findViewById(R.id.all_user_profile_img);
        }
    }
}
