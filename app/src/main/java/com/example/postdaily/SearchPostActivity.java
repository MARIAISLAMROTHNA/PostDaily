package com.example.postdaily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchPostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private ImageButton SearchButton;
    private EditText SearchInputText;
    private RecyclerView SearchResultList;
    private DatabaseReference PostsRef,Likesref;
    Boolean LikeChecker=false;
    private String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_post);

        mAuth = FirebaseAuth.getInstance();
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        Likesref=FirebaseDatabase.getInstance().getReference().child("Likes");
        mToolbar = (Toolbar) findViewById(R.id.searchPostToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Search Posts");

        SearchResultList = findViewById(R.id.search_post_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton = (ImageButton) findViewById(R.id.search_post_button);
        SearchInputText = (EditText) findViewById(R.id.search_post_input);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String SearchBoxInput = SearchInputText.getText().toString();
                SearchPosts(SearchBoxInput);
            }
        });

    }

    private void SearchPosts(String searchBoxInput) {
        Toast.makeText(this, "Searching", Toast.LENGTH_SHORT).show();
        Query SearchPostsQuery = PostsRef.orderByChild("pDescr").startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");
        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(SearchPostsQuery, Posts.class)
                .build();

        FirebaseRecyclerAdapter<Posts, SearchPostActivity.SearchPostsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Posts, SearchPostsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull SearchPostActivity.SearchPostsViewHolder holder, final int position, @NonNull Posts model) {

                        final String PostKey = getRef(position).getKey();
                        String pTimestamp = model.getpTime();
                        Calendar calendar = Calendar.getInstance(Locale.getDefault());
                        calendar.setTimeInMillis(Long.parseLong(pTimestamp));
                        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                        holder.name.setText(model.getName());
                        holder.postTime.setText(pTime);
                        holder.postDescr.setText(model.getpDescr());
                        Picasso.get().load(model.getpImage()).into(holder.postImg);
                        Picasso.get().load(model.getimage()).placeholder(R.drawable.profile_image).into(holder.profImg);
                        holder.setLikeButtonStatus(PostKey);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(SearchPostActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey", PostKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        holder.LikeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                uid=mAuth.getCurrentUser().getUid();

                                LikeChecker = true;
                                Likesref.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (LikeChecker.equals(true)) {
                                            if (snapshot.child(PostKey).hasChild(uid)) {
                                                Likesref.child(PostKey).child(uid).removeValue();
                                                LikeChecker = false;
                                            } else {
                                                Likesref.child(PostKey).child(uid).setValue(true);
                                                LikeChecker = false;

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        });
                        holder.CommentButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent CommentsIntent = new Intent(SearchPostActivity.this, CommentsActivity.class);
                                CommentsIntent.putExtra("PostKey", PostKey);
                                startActivity(CommentsIntent);

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public SearchPostActivity.SearchPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_posts, parent, false);
                        SearchPostActivity.SearchPostsViewHolder viewHolder = new SearchPostActivity.SearchPostsViewHolder(view);
                        return viewHolder;

                    }
                };

        SearchResultList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class SearchPostsViewHolder extends RecyclerView.ViewHolder {

        ImageButton LikeButton, CommentButton;
        TextView DisplayNoOfLikes;

        TextView postDescr, postId, postDate, postTime, postTitle, name;
        CircleImageView profImg;
        ImageView postImg;
        int countLikes;
        String uid;
        DatabaseReference LikesRef;

        View mView;

        public SearchPostsViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.post_user_name);
            postDescr = itemView.findViewById(R.id.post_description);
            postImg = itemView.findViewById(R.id.post_image);
            postTime = itemView.findViewById(R.id.post_time);
            profImg = itemView.findViewById(R.id.Post_Profile_image);
            LikeButton = itemView.findViewById(R.id.like_button);
            CommentButton = itemView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = itemView.findViewById(R.id.display_no_of_likes);
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }

        public void setLikeButtonStatus(final String PostKey) {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(PostKey).hasChild(uid)) {
                        countLikes = (int) snapshot.child(PostKey).getChildrenCount();
                        LikeButton.setImageResource(R.drawable.like);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes) + (" Likes")));
                    } else {
                        countLikes = (int) snapshot.child(PostKey).getChildrenCount();
                        LikeButton.setImageResource(R.drawable.dislike);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes) + (" Likes")));

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
