package com.example.postdaily;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;


public class NewPostActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef,RootRef;
    StorageReference UserProfileImageRef;

    private Toolbar newpostToolbar;
    private EditText title,description;
    private ImageView imageView;
    private Button addPostBtn;

    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_CAMERA_CODE=300;
    private static final int IMAGE_PICK_GALLERY_CODE=400;

    String[] cameraPermissions;
    String[] storagePermissions;

    Uri image_rui=null;

    String name,email,uid,dp;

    private ProgressDialog pd;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        newpostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newpostToolbar);
        getSupportActionBar().setTitle("Add New Post");

        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();
        checkUserStatus();
        RetrieveUserInfo();




//        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
//        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
//       Query query=userDbRef.child("uid").equalTo(uid);
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot ds:snapshot.getChildren()){
//
//                    name=""+ ds.child("name").getValue().toString();
//                  //  email=""+ ds.child("email").getValue().toString();
//                    dp=""+ ds.child("image").getValue().toString();
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        title=findViewById(R.id.titlET);
        description=findViewById(R.id.addDescriptionEt);
        imageView=findViewById(R.id.new_post_image);
        addPostBtn=findViewById(R.id.post_btn);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });
        
        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titles=title.getText().toString().trim();
                String descriptions=description.getText().toString().trim();
                if(TextUtils.isEmpty(titles)){
                    Toast.makeText(NewPostActivity.this,"Enter title",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(descriptions))
                {
                    Toast.makeText(NewPostActivity.this,"Enter description",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(image_rui==null)
                {
                    uploadData(titles,descriptions,"noImage");
                }
                else{
                    uploadData(titles,descriptions,String.valueOf(image_rui));

                }
            }
        });

    }

    private void uploadData(final String titles, final String descriptions, String uri) {

        pd.setMessage("Publishing post...");
        pd.show();

        final String timeStamp= String.valueOf(System.currentTimeMillis());
        String filePathAndName="Posts/"+ "post_"+timeStamp;

        if(!uri.equals("noImage")){
            StorageReference ref= FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putFile(Uri.parse(uri))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());
                            String downloadUri= uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){
                                HashMap<Object,String> hashMap=new HashMap<>();
                                hashMap.put("uid",uid);
                                hashMap.put("name",name);
                                hashMap.put("image",dp);
                                hashMap.put("email",email);
                                hashMap.put("pId",timeStamp);
                                hashMap.put("pTittle",titles);
                                hashMap.put("pDescr",descriptions);
                                hashMap.put("pImage",downloadUri);
                                hashMap.put("pTime",timeStamp);

                                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pd.dismiss();
                                                Toast.makeText(NewPostActivity.this,"Post Published", Toast.LENGTH_SHORT).show();
                                                title.setText("" );
                                                description.setText("" );
                                                imageView.setImageURI(null);
                                                image_rui=null;
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                Toast.makeText(NewPostActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();


                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(NewPostActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
        else {
            HashMap<Object,String> hashMap=new HashMap<>();
            hashMap.put("uid",uid);
            hashMap.put("name",name);
            hashMap.put("image",dp);
            hashMap.put("email",email);
            hashMap.put("pId",timeStamp);
            hashMap.put("pTittle",titles);
            hashMap.put("pDescr",descriptions);
            hashMap.put("pImage","noImage");
            hashMap.put("pTime",timeStamp);

            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            Toast.makeText(NewPostActivity.this,"Post Published", Toast.LENGTH_SHORT).show();
                       title.setText("" );
                       description.setText("" );
                       imageView.setImageURI(null);
                       image_rui=null;


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(NewPostActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();


                        }
                    });
        }

        }


    private void showImagePickDialog() {
        String[] options={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Choose Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which==0)
                {
                    if(!checkCameraPermisson())
                    {
                        requestCameraPermission();
                    }
                    else{
                        pickFromCamera();
                    }
                }
                if (which==1)
                {
                    if(!checkStoragePermisson())
                    {
                        requestStoragePermission();
                    }
                    else{
                        pickFromGallery();

                    }
                }
            }
        });
        builder.create().show();

    }

    private void pickFromGallery() {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }


    private void pickFromCamera() {

        ContentValues cv=new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Desor");
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_rui);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermisson()
    {
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission()
    {
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermisson()
    {
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission()
    {
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode){
        case CAMERA_REQUEST_CODE:{
            if(grantResults.length>0){
                boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                boolean storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                if(cameraAccepted && storageAccepted){
                    pickFromCamera();
                }
                else{
                    Toast.makeText(this, "Camera & Storage both permission are necessary",Toast.LENGTH_LONG).show();
                }
            }

        }
        break;
        case STORAGE_REQUEST_CODE:{
            if(grantResults.length>0){
                boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(storageAccepted){
                    pickFromGallery();
                }
                else{
                    Toast.makeText(this, "Storage permission  necessary",Toast.LENGTH_LONG).show();
                }
            }

        }
        break;

    }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_rui=data.getData();
                imageView.setImageURI(image_rui);

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                imageView.setImageURI(image_rui);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    private void checkUserStatus()
    {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null)
        {
            email=user.getEmail();
            uid=user.getUid();

        }
        else{
           startActivity(new Intent(this,MainActivity.class));
           finish();
        }
    }
    private void RetrieveUserInfo() {
        RootRef.child("Users").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))){
                            String retrieveProfileImage=dataSnapshot.child("image").getValue().toString();
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            name=retrieveUserName;
                            dp=retrieveProfileImage;

                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                    {
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();


                           name=retrieveUserName;

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}