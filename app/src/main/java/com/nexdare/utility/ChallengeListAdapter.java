package com.nexdare.utility;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nexdare.ChatActivity;
import com.nexdare.R;
import com.nexdare.models.Challenge;
import com.nexdare.models.Chatroom;
import com.nexdare.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class ChallengeListAdapter extends ArrayAdapter<Challenge> {

    private static final String TAG = "ChallengeListAdapter";

    private int mLayoutResource;
    private Context mContext;
    private LayoutInflater mInflater;

    public ChallengeListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Challenge> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResource = resource;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static class ViewHolder {
        TextView name, desc, timeFrame, sentTo, status;
        ImageView mProfileImage, mTrash;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        return convertView;
    }
}
