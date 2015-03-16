package com.codepath.apps.meowitter.models;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.codepath.apps.meowitter.app.TwitterApp;
import com.codepath.apps.meowitter.helpers.HelperVars;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrewblaich on 3/5/15.
 */

@Table(name = "Tweet")
public class Tweet extends Model{
    public static final String MENTIONS = "MENTIONS";
    public static final String REGULAR = "REGULAR";
    public static final String PASS = "PASS";
    public static final String PROFILE = "PROFILE";

    @Column(name = "body")
    private String body;

    @Column(name = "uid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long uid; //unique id for tweet

    @Column(name = "user")
    private User user;

    @Column(name = "createdAt")
    private String createdAt;

    @Column(name = "retweetCount")
    private int retweetCount;

    @Column(name = "favoriteCount")
    private int favoriteCount;

    @Column(name = "favorited")
    private boolean favorited;

    @Column(name = "retweeted")
    private boolean retweeted;

    @Column(name = "type")
    private String type;

    @Column(name = "timeline_owner")
    private String timeline_owner;

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public boolean isRetweeted() {

        return retweeted;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public boolean isFavorited() {

        return favorited;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public int getRetweetCount() {
        return retweetCount;
    }



    public String getBody() {
        return body;
    }

    public long getUid() {
        return uid;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public Tweet(){
        super();
    }

    public static Tweet fromJSON(JSONObject jsonObject, String type, String screenName){
        Tweet tweet = new Tweet();
        try {
            tweet.body = jsonObject.getString("text");
            tweet.uid = jsonObject.getLong("id");
//            Log.i("DEBUG", "Tweet id: " + tweet.uid);
            tweet.createdAt = jsonObject.getString("created_at");

            tweet.user = User.fromJSON(jsonObject.getJSONObject("user"), false);
            tweet.retweetCount = 0;
            if(jsonObject.has("retweet_count"))
                tweet.retweetCount = jsonObject.getInt("retweet_count");
            tweet.favoriteCount = 0;
            if(jsonObject.has("favorite_count")) {
                tweet.favoriteCount = jsonObject.getInt("favorite_count");
//                Log.i("DEBUG", "FavoriteCount: " + jsonObject.getInt("favorite_count"));
            }else{
//                Log.i("DEBUG", "FavoriteCount: none found");
            }
            tweet.retweeted = false;
            if(jsonObject.has("retweeted"))
                tweet.retweeted = true;
            tweet.favorited = jsonObject.getBoolean("favorited");
//            Log.i("DEBUG", "Favorited? " + jsonObject.getBoolean("favorited"));
            if(!type.equals(Tweet.PASS)) {
                tweet.type = type;
            }
            tweet.timeline_owner = screenName;
        }catch(JSONException e){
            e.printStackTrace();
        }

        return tweet;
    }

    public static /*ArrayList<Tweet>*/void fromJSONArray(JSONArray jsonArray, String type, String screenName){
        ActiveAndroid.beginTransaction(); //documentation says to use transaction for bulk work, speeds things up
        try {
//            ArrayList<Tweet> tweets = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject tweetJson = jsonArray.getJSONObject(i);
                    Tweet tweet = Tweet.fromJSON(tweetJson, type, screenName);
                    if (tweet != null) {
//                        tweets.add(tweet);
                        tweet.save();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue; //redundant?
                }
            }
            ActiveAndroid.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            ActiveAndroid.endTransaction();
        }
//        return tweets;
    }

    /* gets last 25 tweets */
//    public static List<Tweet> getAll() {
//        return new Select()
//                .from(Tweet.class)
//                //.where("Tweet = *")
//                .orderBy("createdAt DESC")
//                .limit(HelperVars.TWEET_RETRIEVE_COUNT)
//                .execute();
//    }

    /* gets last 25 tweets greater than last_id */
    public static List<Tweet> getAll(long last_id, String type, String screenName) {
        if(last_id == 1) {
            return new Select()
                    .from(Tweet.class)
//                    .where("uid > ?", last_id)
                            //.orderBy("createdAt DESC")
                    .where("type == ?", type)
                    .and("timeline_owner == ?", screenName)
                    .orderBy("uid DESC")
                    .limit(HelperVars.TWEET_RETRIEVE_COUNT)
                    .execute();
        }else{
            return new Select()
                    .from(Tweet.class)
//                    .where("uid < ?", last_id)
                            //.orderBy("createdAt DESC")
                    .where("type == ?", type)
                    .and("timeline_owner == ?", screenName)
                    .orderBy("uid DESC")
                    .limit(HelperVars.TWEET_RETRIEVE_COUNT)
                    .execute();
        }
    }

    public static List<Tweet> getAll(long last_id) {
        if(last_id == 1) {
            return new Select()
                    .from(Tweet.class)
                    .where("uid > ?", last_id)
                            //.orderBy("createdAt DESC")
                    .orderBy("uid DESC")
                    .limit(HelperVars.TWEET_RETRIEVE_COUNT)
                    .execute();
        }else{
            return new Select()
                    .from(Tweet.class)
                    .where("uid < ?", last_id)
                            //.orderBy("createdAt DESC")
                    .orderBy("uid DESC")
                    .limit(HelperVars.TWEET_RETRIEVE_COUNT)
                    .execute();
        }
    }

    public static void updateTweet(Tweet tweet){
        tweet.save(); //is this it?
    }
}
