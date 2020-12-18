package com.example.postdaily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class ClickPostActivity extends AppCompatActivity {
private ImageView PostImg;
private TextView PostDes;
private Button EditPostButton,DeletePostButton;
private ImageButton ListenPostButton;
private String PostKey,CurrentUserid,databaseUserid,description,image;
private FirebaseAuth mAuth;
private DatabaseReference ClickPostRef;
TextToSpeech textToSpeech;
private Boolean speak=true;
private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);
        mToolbar=(Toolbar)findViewById(R.id.click_post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Post Details");

        mAuth=FirebaseAuth.getInstance();
        CurrentUserid=mAuth.getCurrentUser().getUid();

        PostKey= getIntent().getExtras().get("PostKey").toString();
        ClickPostRef= FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);


        PostImg=(ImageView)findViewById(R.id.click_post_image);
        PostDes=(TextView)findViewById(R.id.click_post_description);
        EditPostButton=(Button)findViewById(R.id.edit_post_button);
        ListenPostButton=(ImageButton)findViewById(R.id.listen_post_button);

        DeletePostButton=(Button)findViewById(R.id.delete_post_button);

        DeletePostButton.setVisibility(View.INVISIBLE);
        EditPostButton.setVisibility(View.INVISIBLE);
        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=textToSpeech.ERROR){
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if(snapshot.exists())
                 {
                     description= snapshot.child("pDescr").getValue().toString();
                     image=snapshot.child("pImage").getValue().toString();
                     databaseUserid=snapshot.child("uid").getValue().toString();
                     PostDes.setText(description);
                     Picasso.get().load(image).into(PostImg);
                     if(CurrentUserid.equals(databaseUserid))
                     {
                         DeletePostButton.setVisibility(View.VISIBLE);
                         EditPostButton.setVisibility(View.VISIBLE);

                     }
                     EditPostButton.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             EditCurrentPost(description);
                         }
                     });
                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteCurrentPost();
            }
        });
        ListenPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (speak == true) {
                    ListenPostButton.setImageResource(R.drawable.listen);
                    textToSpeech.speak(description, TextToSpeech.QUEUE_FLUSH, null);
                    speak=false;
                }
                else if(speak==false)
                {
                    if(textToSpeech!=null){
                        ListenPostButton.setImageResource(R.drawable.speakeroff);
                        textToSpeech.stop();
                    }
                    speak=true;
                }

            }
        });


    }
    @Override
    protected void onDestroy() {
        if(textToSpeech!=null) {
            ListenPostButton.setImageResource(R.drawable.speakeroff);
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void EditCurrentPost(String description) {
        AlertDialog.Builder builder=new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post:");
        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClickPostRef.child("pDescr").setValue((inputField).getText().toString());
                Toast.makeText(ClickPostActivity.this,"Post Updated Successfully",Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog=builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_purple);

    }

    private void DeleteCurrentPost()
    {
        ClickPostRef.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this,"Post has been deleted",Toast.LENGTH_SHORT).show();
    }
    private void SendUserToMainActivity() {
        Intent mainIntent =new Intent(ClickPostActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}