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


public class FindFriendsActivity extends AppCompatActivity {
private Toolbar mToolbar;
private ImageButton SearchButton;
private EditText SearchInputText;
private RecyclerView SearchResultList;
private DatabaseReference UsersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        mToolbar = (Toolbar)findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friends");

        SearchResultList=findViewById(R.id.search_friend_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton=(ImageButton) findViewById(R.id.search_people_button);
        SearchInputText=(EditText)findViewById(R.id.search_box_input);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String SearchBoxInput= SearchInputText.getText().toString();
                SearchFriends(SearchBoxInput);
            }
        });

    }

    private void SearchFriends(String searchBoxInput)
    {
        Toast.makeText(this,"Searching",Toast.LENGTH_SHORT).show();
        Query SearchFriendsQuery=UsersRef.orderByChild("name").startAt(searchBoxInput).endAt(searchBoxInput+"\uf8ff");
        FirebaseRecyclerOptions<FindFriends> options=new FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(SearchFriendsQuery,FindFriends.class)
                .build();

        FirebaseRecyclerAdapter<FindFriends,FindFriendViewHolder> adapter=
                new FirebaseRecyclerAdapter<FindFriends,FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull FindFriends model) {
                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visitUserId=getRef(position).getKey();
                        Intent profileIntent=new Intent(FindFriendsActivity.this,PersonProfileActivity.class);
                        profileIntent.putExtra("visitUserId",visitUserId);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_display,parent,false);
                FindFriendViewHolder viewHolder= new FindFriendViewHolder(view);
                return viewHolder;

            }
        };

        SearchResultList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView profileImage;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.all_user_name);
            userStatus=itemView.findViewById(R.id.all_user_status);
            profileImage=itemView.findViewById(R.id.all_user_profile_img);
        }
    }
}


