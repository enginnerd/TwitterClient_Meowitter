package com.codepath.apps.meowitter.models;

import java.io.Serializable;
import java.io.StringBufferInputStream;

/**
 * Created by andrewblaich on 3/7/15.
 */
public class Compose implements Serializable{
    private User user;
    private String message;
    private boolean reply;
    private String reply_userName;
    private String in_reply_to_status_id;

    public void setReply_userName(String reply_userName) {
        this.reply_userName = reply_userName;
    }

    public String getReply_userName() {
        return reply_userName;
    }


    public void setReply(boolean reply) {
        this.reply = reply;
    }

    public void setIn_reply_to_status_id(String in_reply_to_status_id) {
        this.in_reply_to_status_id = in_reply_to_status_id;
    }

    public boolean isReply() {
        return reply;
    }

    public String getIn_reply_to_status_id() {
        return in_reply_to_status_id;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {

        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Compose(){
    }

    @Override
    public String toString(){
        return user.getScreenName() + "," +
                user.getName() + "," +
                user.getProfileImageUrl() + "," +
                user.getUserRealName() + "," +
                user.getUid() + "," +
                reply + "," +
                reply_userName + "," +
                in_reply_to_status_id;
    }
}
