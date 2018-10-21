package com.nexdare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nexdare.utility.FilePaths;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UploadImageActivity extends AppCompatActivity implements ChangePhotoDialog.OnPhotoReceivedListener {
    private StorageReference videoRef;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Uri mSelectedImageUri;
    private Bitmap mSelectedImageBitmap;
    private ImageView mProfileImage;
    private ProgressBar mProgressBar;
    private byte[] mBytes;
    private double progress;
    private static final String DOMAIN_NAME = "nexient.com";
    private static final int REQUEST_CODE = 1234;
    private static final double MB_THRESHHOLD = 5.0;
    private static final double MB = 1000000.0;
    private boolean mStoragePermissions;
    private static final String TAG = "UploadImageActivity";


    @Override
    public void getImagePath(Uri imagePath) {
        if (!imagePath.toString().equals("")) {
            mSelectedImageBitmap = null;
            mSelectedImageUri = imagePath;
            Log.d(TAG, "getImagePath: got the image uri: " + mSelectedImageUri);

            ImageLoader.getInstance().displayImage(imagePath.toString(), mProfileImage);
        }

    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            mSelectedImageUri = null;
            mSelectedImageBitmap = bitmap;
            Log.d(TAG, "getImageBitmap: got the image bitmap: " + mSelectedImageBitmap);

            mProfileImage.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_challenge);

        verifyStoragePermissions();
        setupFirebaseAuth();
    }

    /*
     * ----------------------------- Firebase setup
     * ---------------------------------
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    // toastMessage("Successfully signed in with: " + user.getEmail());

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Toast.makeText(UploadImageActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UploadImageActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                // ...
            }
        };

    }

    public void uploadNewPhoto(Uri imageUri) {
        /*
         * upload a new profile photo to firebase storage
         */
        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");

        // Only accept image sizes that are compressed to under 5MB. If thats not
        // possible
        // then do not allow image to be uploaded
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imageUri);
    }

    /**
     * Generalized method for asking permission. Can pass any array of permissions
     */
    public void verifyStoragePermissions() {
        Log.d(TAG, "verifyPermissions: asking user for permissions.");
        String[] permissions = { android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA };
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions(UploadImageActivity.this, permissions, REQUEST_CODE);
        }
    }

    /**
     * 1) doinBackground takes an imageUri and returns the byte array after
     * compression 2) onPostExecute will print the % compression to the log once
     * finished
     */
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {

        Bitmap mBitmap;

        public BackgroundImageResize(Bitmap bm) {
            if (bm != null) {
                mBitmap = bm;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog();
            Toast.makeText(UploadImageActivity.this, "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params) {
            Log.d(TAG, "doInBackground: started.");

            if (mBitmap == null) {

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(UploadImageActivity.this.getContentResolver(), params[0]);
                    Log.d(TAG, "doInBackground: bitmap size: megabytes: " + mBitmap.getByteCount() / MB + " MB");
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: IOException: ", e.getCause());
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++) {
                if (i == 10) {
                    Toast.makeText(UploadImageActivity.this, "That image is too large.", Toast.LENGTH_SHORT).show();
                    break;
                }
                bytes = getBytesFromBitmap(mBitmap, 100 / i);
                Log.d(TAG, "doInBackground: megabytes: (" + (11 - i) + "0%) " + bytes.length / MB + " MB");
                if (bytes.length / MB < MB_THRESHHOLD) {
                    return bytes;
                }
            }
            return bytes;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            hideDialog();
            mBytes = bytes;
            // execute the upload
            executeUploadTask();
        }
    }

    // convert from bitmap to byte array
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void executeUploadTask() {
        showDialog();
        FilePaths filePaths = new FilePaths();
        // specify where the photo will be stored
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid()
                        + "/profile_image"); // just replace the old image with the new one

        if (mBytes.length / MB < MB_THRESHHOLD) {

            // Create file metadata including the content type
            StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpg")
                    .setContentLanguage("en") // see nodes below
                    /*
                     * Make sure to use proper language code ("English" will cause a crash) I
                     * actually submitted this as a bug to the Firebase github page so it might be
                     * fixed by the time you watch this video. You can check it out at
                     * https://github.com/firebase/quickstart-unity/issues/116
                     */
                    .setCustomMetadata("Mitch's special meta data", "JK nothing special here")
                    .setCustomMetadata("location", "Iceland").build();
            // if the image size is valid then we can submit to database
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(mBytes, metadata);
            // uploadTask = storageReference.putBytes(mBytes); //without metadata

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Now insert the download url into the firebase database
                    Uri firebaseURL = taskSnapshot.getDownloadUrl();
                    Toast.makeText(UploadImageActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: firebase download url : " + firebaseURL.toString());
                    FirebaseDatabase.getInstance().getReference().child(getString(com.nexdare.R.string.dbnode_users))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(getString(com.nexdare.R.string.field_profile_image))
                            .setValue(firebaseURL.toString());

                    hideDialog();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(UploadImageActivity.this, "could not upload photo", Toast.LENGTH_SHORT).show();

                    hideDialog();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double currentProgress = (100 * taskSnapshot.getBytesTransferred())
                            / taskSnapshot.getTotalByteCount();
                    if (currentProgress > (progress + 15)) {
                        progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: Upload is " + progress + "% done");
                        Toast.makeText(UploadImageActivity.this, progress + "%", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        } else {
            Toast.makeText(this, "Image is too Large", Toast.LENGTH_SHORT).show();
        }

    }
}
