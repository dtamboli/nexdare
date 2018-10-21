package com.nexdare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nexdare.fragments.LisReceivedChallenges;
import com.nexdare.fragments.ListSentChallenges;
import com.nexdare.utility.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ListChallenges extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private TabLayout tab;
    private ViewPager vp;

    private static final String TAG = "ListChallenges";
    public static boolean isActivityRunning;

    // Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

       @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_challenges);
        setupFirebaseAuth();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //setSupportActionBar(toolbar);

        //VIEWPAGER AND TABS
        vp = (ViewPager) findViewById(R.id.viewpager);
        addPages();

        tab = (TabLayout) findViewById(R.id.tabs);
        tab.setTabGravity(TabLayout.GRAVITY_FILL);
        tab.setupWithViewPager(vp);
        tab.addOnTabSelectedListener(this);
    }
    private void addPages()
    {
        MyPagerAdapter myPagerAdapter=new MyPagerAdapter(getSupportFragmentManager());
        myPagerAdapter.addPage(ListSentChallenges.newInstance());
        myPagerAdapter.addPage(LisReceivedChallenges.newInstance());

        vp.setAdapter(myPagerAdapter);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        vp.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.optionSignOut:
                signOut();
                return true;
            case R.id.optionAccountSettings:
                intent = new Intent(ListChallenges.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.optionChat:
                intent = new Intent(ListChallenges.this, ChatActivity.class);
                startActivity(intent);
                return true;
//        case R.id.optionAdmin:
//            if (mIsAdmin) {
//                intent = new Intent(SignedInActivity.this, AdminActivity.class);
//                startActivity(intent);
//            } else {
//                Toast.makeText(this, "You're not an Admin", Toast.LENGTH_SHORT).show();
//            }
//
//            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * init universal image loader
     */
    private void initImageLoader() {
        UniversalImageLoader imageLoader = new UniversalImageLoader(ListChallenges.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    /**
     * Sign out the current user
     */
    private void signOut() {
        Log.d(TAG, "signOut: signing out");
        FirebaseAuth.getInstance().signOut();
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

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(ListChallenges.this, LoginActivity.class);
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
        isActivityRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
        isActivityRunning = false;
    }
}


