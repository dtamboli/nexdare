package com.nexdare.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nexdare.R;
import com.nexdare.models.Challenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

/**
 * Created by Oclemy on 9/29/2016 for ProgrammingWizards Channel and http://www.camposha.com.
 */
public class LisReceivedChallenges extends Fragment {

    private static final String TAG = "ChallengeActivity";
    private ArrayList<Challenge> mChallenges;

    //HEADER DATA SOURCE
    static String[] spaceProbeHeaders={"Name","Status","Received From", "Time Remaining"};


    public static LisReceivedChallenges newInstance() {
        return new LisReceivedChallenges();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //VIEWS
        View rootView = inflater.inflate(R.layout.list_received_challenges, null);
        getChallenges(rootView);

        return rootView;
    }

    private void getChallenges(final View rootView) {
        Log.d(TAG, "getChallenges: retrieving challenges from firebase database.");
        mChallenges = new ArrayList<>();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.dbnode_challenges));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found challenge: " + singleSnapshot.getValue());

                    Challenge challenge = new Challenge();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                    if (objectMap.get(getString(R.string.field_challenge_userId)) != null
                            && objectMap.get(getString(R.string.field_challenge_receivedFrom)) != null) {
                        String userId = objectMap.get(getString(R.string.field_challenge_userId)).toString();
                        if (userId.equals(user.getEmail())) {
                            challenge.setChallenge_id(objectMap.get(getString(R.string.field_challenge_id)).toString());
                            challenge.setName(objectMap.get(getString(R.string.field_challenge_name)).toString());
                            challenge
                                    .setDescription(objectMap.get(getString(R.string.field_challenge_desc)).toString());
                            challenge.setReceivedFrom(objectMap.get(getString(R.string.field_challenge_receivedFrom)).toString());
                            challenge.setStatus(objectMap.get(getString(R.string.field_challenge_status)).toString());
                            challenge.setTimeFrame(Integer
                                    .parseInt(objectMap.get(getString(R.string.field_challenge_timeframe)).toString()));
                            mChallenges.add(challenge);
                        }
                    }
                }


                final TableView<String[]> tableView = (TableView<String[]>) rootView.findViewById(R.id.list_received_challenges_TB);

                //SET TABLE PROPERTIES
                tableView.setHeaderBackgroundColor(Color.parseColor("#f38630"));
                tableView.setHeaderAdapter(new SimpleTableHeaderAdapter(getActivity(),spaceProbeHeaders));
                tableView.setColumnCount(4);

                //ADAPTER
                List<String[]> data = new ArrayList<>();
                for(Challenge challenge : mChallenges) {
                    String[] strings = {challenge.getName(),challenge.getStatus(),challenge.getReceivedFrom(),
                            String.valueOf(challenge.getTimeFrame())};
                    data.add(strings);
                }
                tableView.setDataAdapter(new SimpleTableDataAdapter(getActivity(), data));

                //ROW CLICK LISTENER
                tableView.addDataClickListener(new TableDataClickListener() {
                    @Override
                    public void onDataClicked(int rowIndex, Object clickedData) {
                        Toast.makeText(getActivity(), ((String[])clickedData)[1], Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public String toString() {
        return "Received-Challenges";
    }
}