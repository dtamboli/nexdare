package com.nexdare;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nexdare.models.Challenge;
import com.nexdare.utility.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class NewChallenge extends AppCompatActivity {

    private static final String TAG = "NewChallenge";

    private static final String STATE_LOCAL_DATE_TIME = "state.local.date.time";

    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText challengeName;
    private EditText to;
    private EditText time;
    private EditText rules;
    private CheckBox free;
    private CheckBox premium;
    private Button send;
    private LocalDateTime mLocalDateTime = new LocalDateTime();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_challenge);

        challengeName = (EditText) findViewById(R.id.challenge);

        to = (EditText) findViewById(R.id.to);

        time = (EditText) findViewById(R.id.time);

        rules = (EditText) findViewById(R.id.rules);

        free = (CheckBox) findViewById(R.id.checkbox_free);

        premium = (CheckBox) findViewById(R.id.checkbox_premium);

        send = (Button) findViewById(R.id.Send);

        free.setChecked(true);
        if (savedInstanceState!=null) {
            mLocalDateTime = (LocalDateTime) savedInstanceState.getSerializable(STATE_LOCAL_DATE_TIME);
        }

        init();
        setupFirebaseAuth();
        initImageLoader();

        // Create the Date/Time text view
        createTextViewDateTime();

        // Create the choose date/time button
        createButtonChooseDateTime();

        createChallengeButton();

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


    public void onCheckboxClicked(View view) {

        if (free.isChecked()) {
            free.toggle();
        }

        if(premium.isChecked()){
            premium.toggle();
        }
    }

    /**
     * init universal image loader
     */
    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(NewChallenge.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    public void init(){
        createChallenge();
    }

    private void createTextViewDateTime() {
        TextView textView = (TextView)safeFindViewById(R.id.time);
        textView.setText(formatDateString(mLocalDateTime));
    }

    private void updateDateTimeTextView() {
        TextView textView = (TextView) safeFindViewById(R.id.time);
        textView.setText(formatDateString(mLocalDateTime));
    }


    private String formatDateString(LocalDateTime localDateTime) {
        return localDateTime.toString("MM/dd/yyyy hh:mm a");
    }

    private void createChallengeButton() {
        Button button = (Button)safeFindViewById(R.id.Send);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
              createChallenge();
                Intent intent = new Intent(NewChallenge.this, ListChallenges.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }


    private View safeFindViewById(int viewId) {
        final String METHOD = "createButtonChooseDateTime(" + viewId + ")";
        View ret = findViewById(viewId);
        if (ret == null) {
            Log.e(TAG, METHOD + "Something has gone horribly wrong.");
        }
        return ret;
    }


    private void createButtonChooseDateTime() {
        Button button = (Button)safeFindViewById(R.id.button_choosedatetime);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                // If there is already a Date displayed, use that.
                Date dateToUse = (mLocalDateTime == null) ? new LocalDateTime().toDate() : mLocalDateTime.toDate();
                DateTimePickerFragment datePickerFragment =
                        FragmentFactory.createDatePickerFragment(dateToUse, "The", DateTimePickerFragment.BOTH,
                                new DateTimePickerFragment.ResultHandler() {
                                    @Override
                                    public void setDate(Date result) {
                                        mLocalDateTime = new LocalDateTime(result.getTime());
                                        updateDateTimeTextView();
                                    }
                                });
                datePickerFragment.show(fragmentManager, DateTimePickerFragment.DIALOG_TAG);
            }
        });
    }

    private void createChallenge() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //get a database reference
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_challenges));

        //create the new messages id
        String newMessageId = reference.push().getKey();

        Challenge challenge = new Challenge();
        challenge.setChallenge_id(newMessageId);
        challenge.setName(challengeName.getText().toString());

     //   challenge.setName();
        challenge.setStatus("CREATED");
        challenge.setSentTo(to.getText().toString());
        challenge.setUserId(user.getEmail());
        challenge.setRules(rules.getText().toString());

        LocalDateTime currentTime = new LocalDateTime();

        String timeLimit =time.getText().toString();
/*
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm a");
        DateTime dt = formatter.parseDateTime(timeLimit);*/
        int hours  = Hours.hoursBetween(currentTime.toLocalDate(), mLocalDateTime.toLocalDate()).getHours();

        challenge.setTimeFrame(hours);

        //insert the new message into the chatroom
        reference
                .child(newMessageId)
                .setValue(challenge);
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
                intent = new Intent(NewChallenge.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.optionNewChallenge:
                intent = new Intent(NewChallenge.this, NewChallenge.class);
                startActivity(intent);
                return true;
            case R.id.optionChat:
                intent = new Intent(NewChallenge.this, ChatActivity.class);
                startActivity(intent);
                return true;
            case R.id.optionAdmin:
                    intent = new Intent(NewChallenge.this, AdminActivity.class);
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
                    Intent intent = new Intent(NewChallenge.this, LoginActivity.class);
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

            Intent intent = new Intent(NewChallenge.this, NewChallenge.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
    }


}













