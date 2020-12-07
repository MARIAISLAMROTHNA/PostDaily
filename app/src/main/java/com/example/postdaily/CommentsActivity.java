package com.example.postdaily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {
    private RecyclerView CommentsList;
    private ImageButton PostCommentsBtn;
    private EditText CommentInputText;
    private DatabaseReference UserRef,PostRef;
    private String Post_Key,currentUserId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        Post_Key=getIntent().getExtras().get("PostKey").toString();

        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef=FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");

        CommentsList= (RecyclerView)findViewById(R.id.comment_list);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        PostCommentsBtn=(ImageButton)findViewById(R.id.post_comment_btn);
        CommentInputText=(EditText)findViewById(R.id.comment_input);

        PostCommentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String Username=snapshot.child("name").getValue().toString();
                            ValidateComment(Username);
                            CommentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }
    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView myUserName,myComment,myTime,myDate;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
            myUserName=(TextView)itemView.findViewById(R.id.comment_user_name);
            myComment=(TextView)itemView.findViewById(R.id.comment_text);
            myTime=(TextView)itemView.findViewById(R.id.comment_time);
            myDate=(TextView)itemView.findViewById(R.id.comment_date);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Comments> options=new FirebaseRecyclerOptions.Builder<Comments>()
                .setQuery(PostRef,Comments.class)
                .build();

        FirebaseRecyclerAdapter<Comments,CommentsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Comments,CommentsViewHolder>(options) {
                    @NonNull
                    @Override
                    protected void onBindViewHolder(@NonNull CommentsViewHolder holder, final int position, @NonNull Comments model) {
                        holder.myUserName.setText(model.getUsername());
                        holder.myComment.setText(model.getComment());
                        holder.myTime.setText(model.getTime());
                        holder.myDate.setText(model.getDate());
                     //   Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);
                    }

                    @NonNull
                    @Override
                    public CommentsActivity.CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout,parent,false);
                        CommentsActivity.CommentsViewHolder viewHolder= new CommentsViewHolder(view);
                        return viewHolder;

                    }
                };
        CommentsList.setAdapter(adapter);
        adapter.startListening();


    }

    private void ValidateComment(String username)
    {
        String commentText=CommentInputText.getText().toString();
        if(TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this,"Please Write text to Comment...", Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar calForDate=Calendar.getInstance();
            SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMM-yyyy");
           final String saveCurrentDate=currentDate.format(calForDate.getTime());

            Calendar calForTime=Calendar.getInstance();
            SimpleDateFormat currentTime=new SimpleDateFormat("hhh:mm");
            final String saveCurrentTime=currentTime.format(calForTime.getTime());
            final String RandomKey=currentUserId + saveCurrentDate +saveCurrentTime;

            HashMap commentsMap=new HashMap();
            commentsMap.put("uid",currentUserId);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("username",username);

            PostRef.child(RandomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(CommentsActivity.this,"You have commented successfully....", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(CommentsActivity.this,"Error occured try again....", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

}