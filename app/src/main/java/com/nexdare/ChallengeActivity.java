package com.nexdare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nexdare.models.Challenge;
import com.nexdare.models.ChatMessage;
import com.nexdare.models.Chatroom;
import com.nexdare.utility.ChallengeListAdapter;
import com.nexdare.utility.ChatroomListAdapter;
import com.nexdare.utility.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ChallengeActivity extends AppCompatActivity {

    private static final String TAG = "ChallengeActivity";

    private FirebaseAuth.AuthStateListener mAuthListener;
    //widgets
    private ListView mListView;
    private FloatingActionButton mFob;


    //vars
    private ArrayList<Challenge> mChallenges;
    private ChallengeListAdapter mAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signedin);

        init();
        setupFirebaseAuth();
        initImageLoader();
    }

    /**
     * init universal image loader
     */
    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(ChallengeActivity.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    public void init(){
        createChallenge();
        getChallenges();
    }

    private void setupChatroomList(){
        Log.d(TAG, "setupChatroomList: setting up chatroom listview");
        mAdapter = new ChallengeListAdapter(ChallengeActivity.this, R.layout.layout_chatroom_listitem, mChallenges);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: selected chatroom: " + mChallenges.get(i).toString());
                Intent intent = new Intent(ChallengeActivity.this, ChatroomActivity.class);
                intent.putExtra(getString(R.string.intent_challenge), mChallenges.get(i));
                startActivity(intent);
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
        challenge.setChallenge_id("Id");
        challenge.setName("10 PushUps");
        challenge.setStatus("ACCEPTED");
        challenge.setSentTo("Nikhil");
        challenge.setUserId(user.getEmail());
        challenge.setRules("Post Video as proof");
        challenge.setTimeFrame(100);

        //insert the new message into the chatroom
        reference
                .child(newMessageId)
                .setValue(challenge);
    }


    private void getChallenges(){
        Log.d(TAG, "getChallenges: retrieving challenges from firebase database.");
        mChallenges = new ArrayList<>();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.dbnode_challenges));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot:  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found challenge: "
                            + singleSnapshot.getValue());

                    Challenge challenge = new Challenge();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                    if(objectMap.get(getString(R.string.field_challenge_userId)) != null) {
                        String userId = objectMap.get(getString(R.string.field_challenge_userId)).toString();
                        if(userId.equals(user.getEmail())) {
                            challenge.setChallenge_id(objectMap.get(getString(R.string.field_challenge_id)).toString());
                            challenge.setName(objectMap.get(getString(R.string.field_challenge_name)).toString());
                            challenge.setDescription(objectMap.get(getString(R.string.field_challenge_desc)).toString());
                            challenge.setSentTo(objectMap.get(getString(R.string.field_challenge_sentto)).toString());
                            challenge.setStatus(objectMap.get(getString(R.string.field_challenge_status)).toString());
                            challenge.setTimeFrame(Integer.parseInt(objectMap.get(getString
                                    (R.string.field_challenge_timeframe)).toString()));
                            mChallenges.add(challenge);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                intent = new Intent(ChallengeActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.optionChat:
                intent = new Intent(ChallengeActivity.this, ChatActivity.class);
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
                    Intent intent = new Intent(ChallengeActivity.this, LoginActivity.class);
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

            Intent intent = new Intent(ChallengeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
    }


}












