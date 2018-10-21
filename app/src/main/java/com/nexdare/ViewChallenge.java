package com.nexdare;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nexdare.models.Challenge;
import com.nexdare.utility.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.joda.time.Hours;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ViewChallenge extends AppCompatActivity {

    private static final String TAG = "NewChallenge";

    private static final String STATE_LOCAL_DATE_TIME = "state.local.date.time";

    private FirebaseAuth.AuthStateListener mAuthListener;

    TextView tv, tv1, tv2, tv3, tv4, tv5;
    private LocalDateTime mLocalDateTime = new LocalDateTime();
    private Button btnChoose, btnUpload, btnSubmit;
    private ImageView imageView;

    private Uri filePath;

    private final int PICK_IMAGE_REQUEST = 71;
    FirebaseStorage storage;
    StorageReference storageReference;
    ImageView image;
    String challengeId = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_challenge);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        tv = (TextView) findViewById(R.id.challenge);
        tv1 = (TextView) findViewById(R.id.status);
        tv2 = (TextView) findViewById(R.id.from);
        tv3 = (TextView) findViewById(R.id.time);
        tv4 = (TextView) findViewById(R.id.rules);
        tv5 = (TextView) findViewById(R.id.submitionNotes);

        tv.setText("Challenge: " + getIntent().getStringExtra("challengeName"));
        tv.setEnabled(false);
        tv1.setText("Status: " + getIntent().getStringExtra("status"));
        tv1.setEnabled(false);
        tv2.setText("Received From: "+ getIntent().getStringExtra("from"));
        tv2.setEnabled(false);
        tv3.setText("Time Remaining: " + getIntent().getStringExtra("timeRemaining"));
        tv3.setEnabled(false);
        tv4.setText("Rules: " + getIntent().getStringExtra("rules"));
        tv4.setEnabled(false);
        if(getIntent().getStringExtra("submitionNotes") != null) {
            tv5.setText(getIntent().getStringExtra("submitionNotes"));
        }
        if(getIntent().getStringExtra("imageUri") != null) {
            File localFile = new File("gs://zettai-tools.appspot.com/images/17b54a4b-3953-41ec-90da-148aec9fcb3d");
            downloadFile(localFile);
            image = (ImageView) findViewById(R.id.imgView);
            image.setImageURI(Uri.fromFile(localFile));
        }
        challengeId = getIntent().getStringExtra("challengeId");

        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        imageView = (ImageView) findViewById(R.id.imgView);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateChallenge();
                Intent intent = new Intent(ViewChallenge.this, ListChallenges.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });


        setupFirebaseAuth();


    }

    private void downloadFile(final File localFile) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        if(localFile != null) {
            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.e("firebase ", ";local tem file created  created " + localFile.getPath());
                    //  updateDb(timestamp,localFile.toString(),position);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("firebase ", ";local tem file not created  created " + exception.toString());
                }
            });
        }
    }

    private void updateChallenge() {

        String filePathStr = "";
        if(filePath != null) {
            filePathStr = filePath.getPath();
        }
        String submittionNotes = String.valueOf(tv5.getText());
        //get a database reference
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_challenges));

        //insert the new message into the chatroom
        reference.child(challengeId)
                .child("submitionNotes").setValue(submittionNotes);

        reference.child(challengeId)
                .child("imageUri").setValue(filePathStr);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(ViewChallenge.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ViewChallenge.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_LOCAL_DATE_TIME, mLocalDateTime);
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mLocalDateTime = (LocalDateTime)savedInstanceState.getSerializable(STATE_LOCAL_DATE_TIME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()){
            case R.id.optionSignOut:
                signOut();
                return true;
            case R.id.optionAccountSettings:
                intent = new Intent(ViewChallenge.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.optionNewChallenge:
                intent = new Intent(ViewChallenge.this, ViewChallenge.class);
                startActivity(intent);
                return true;
            case R.id.optionChat:
                intent = new Intent(ViewChallenge.this, ChatActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Sign out the current user
     */
    private void signOut(){
        Log.d(TAG, "signOut: signing out");
        FirebaseAuth.getInstance().signOut();
    }

    /*
           ----------------------------- Firebase setup ---------------------------------
        */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(ViewChallenge.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }

    private void checkAuthenticationState(){
        Log.d(TAG, "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            Log.d(TAG, "checkAuthenticationState: user is null, navigating back to login screen.");

            Intent intent = new Intent(ViewChallenge.this, ViewChallenge.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
    }


}













