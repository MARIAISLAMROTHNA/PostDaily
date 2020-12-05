package com.example.postdaily;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef,UserRef,Postsref;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private Button addPostBtn;
    private TextView userName;
    private CircleImageView userImg;
    private String uid;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        drawerLayout=(DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle=new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close );
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        uid=mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserRef=FirebaseDatabase.getInstance().getReference().child("Users");
        Postsref=FirebaseDatabase.getInstance().getReference().child("Posts");


        navigationView=(NavigationView)findViewById(R.id.navigation_view);

        View navview=navigationView.inflateHeaderView(R.layout.navigation_header);
        userName=(TextView)navview.findViewById(R.id.nav_user_full_name);
        userImg=(CircleImageView)navview.findViewById(R.id.nav_user_pro);

        UserRef.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))){
                            String retrieveProfileImage=dataSnapshot.child("image").getValue().toString();
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            userName.setText(retrieveUserName);
                           Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.profile_image).into(userImg);

                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();

                            userName.setText(retrieveUserName);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                UserMenuSelector(item);
                return false;
            }
        });


        postList=(RecyclerView)findViewById(R.id.all_user_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        displayAllUsersPost();

    }

    private void displayAllUsersPost() {
        FirebaseRecyclerOptions<Posts> options=new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(Postsref,Posts.class)
                .build();

        FirebaseRecyclerAdapter<Posts,PostsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {

                        String pTimestamp=model.getpTime();
                        Calendar calendar=Calendar.getInstance(Locale.getDefault());
                        calendar.setTimeInMillis(Long.parseLong(pTimestamp));
                        String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

                        holder.name.setText(model.getName());
                        holder.postTime.setText(pTime);
                        holder.postDescr.setText(model.getpDescr());
                        Picasso.get().load(model.getpImage()).into(holder.postImg);
                        Picasso.get().load(model.getimage()).placeholder(R.drawable.profile_image).into(holder.profImg);

                    }

                    @NonNull
                    @Override
                    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_posts,parent,false);
                      PostsViewHolder viewHolder=new PostsViewHolder(view);

                        return viewHolder;
                    }
                };
        postList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        TextView postDescr,postId,postDate,postTime,postTitle,name;
        CircleImageView profImg;
        ImageView postImg;

        View mView;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            name=itemView.findViewById(R.id.post_user_name);
            postDescr=itemView.findViewById(R.id.post_description);
            postImg=itemView.findViewById(R.id.post_image);
            postTime=itemView.findViewById(R.id.post_time);
            //postDate=itemView.findViewById(R.id.post_date);
            profImg=itemView.findViewById(R.id.Post_Profile_image);



        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       if(actionBarDrawerToggle.onOptionsItemSelected(item))
       {
           return true;
       }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {
   switch (item.getItemId())
   {
       case R.id.nav_home:
           Toast.makeText(MainActivity.this,"HomeChecked",Toast.LENGTH_LONG).show();
           break;
       case R.id.nav_profile:
           Intent newPostIntent=new Intent(MainActivity.this, UserProfileActivity.class);
           startActivity(newPostIntent);
           break;
       case R.id.nav_post:
           SendUserToPostActivity();
           break;
       case R.id.nav_friends:
           Toast.makeText(MainActivity.this,"FriendsChecked",Toast.LENGTH_LONG).show();
           break;
       case R.id.nav_find_friendst:
           Toast.makeText(MainActivity.this,"Find Friends",Toast.LENGTH_LONG).show();
           break;
       case R.id.nav_logout:
           mAuth.signOut();
           SendUserToLoginActivity();
           break;


   }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            SendUserToLoginActivity();
        } else {
            VerifyUserExistance();
        }

    }


    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

    }
    private void VerifyUserExistance() {
        String currentUserID=mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists()){

                }
                else{
                    SendUserToUserProfileActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void SendUserToUserProfileActivity() {
        Intent UserSettingIntent =new Intent(MainActivity.this,UserProfileActivity.class);
        startActivity(UserSettingIntent);

    }
    private void SendUserToPostActivity() {
        Intent profileIntent=new Intent(MainActivity.this, NewPostActivity.class);
        startActivity(profileIntent);
    }
    private void AddHeaderNameImage()
    {
        uid=mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))){
                            String retrieveProfileImage=dataSnapshot.child("image").getValue().toString();
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            userName.setText(retrieveUserName);
                            Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.profile_image).into(userImg);

                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();

                            userName.setText(retrieveUserName);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    }

