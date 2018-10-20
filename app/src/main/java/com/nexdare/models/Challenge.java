package com.nexdare.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;


public class Challenge implements Parcelable{

    private String name;
    private String description;
    private String rules;
    private String challenge_id;
    private String status;
    private int timeFrame;
    private Date createdTimestamp;
    private Date updatedTimestamp;
    private String sentTo;
    private String receivedFrom;
    private String userId;

    public Challenge() {

    }

    protected Challenge(Parcel in) {
        name = in.readString();
        description = in.readString();
        rules = in.readString();
        challenge_id = in.readString();
        status = in.readString();
        timeFrame = in.readInt();
        createdTimestamp = (Date) in.readValue(ClassLoader.getSystemClassLoader());
        updatedTimestamp = (Date) in.readValue(ClassLoader.getSystemClassLoader());
        sentTo = in.readString();
        receivedFrom = in.readString();
        userId = in.readString();
    }

    public static final Creator<Challenge> CREATOR = new Creator<Challenge>() {
        @Override
        public Challenge createFromParcel(Parcel in) {
            return new Challenge(in);
        }

        @Override
        public Challenge[] newArray(int size) {
            return new Challenge[size];
        }
    };

    public String getReceivedFrom() {
        return receivedFrom;
    }

    public void setReceivedFrom(String receivedFrom) {
        this.receivedFrom = receivedFrom;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getChallenge_id() {
        return challenge_id;
    }

    public void setChallenge_id(String challenge_id) {
        this.challenge_id = challenge_id;
    }

    public int getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(int timeFrame) {
        this.timeFrame = timeFrame;
    }

    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Date createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public Date getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(Date updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public String getSentTo() {
        return sentTo;
    }

    public void setSentTo(String sentTo) {
        this.sentTo = sentTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(challenge_id);
        parcel.writeString(sentTo);
    }
}
