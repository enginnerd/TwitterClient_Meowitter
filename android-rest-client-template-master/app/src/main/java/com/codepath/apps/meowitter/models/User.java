package com.codepath.apps.meowitter.models;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.codepath.apps.meowitter.helpers.HelperVars;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by andrewblaich on 3/5/15.
 */

@Table(name = "User")
public class User extends Model {

    @Column(name = "name")
    private String name;

    @Column(name = "uid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long uid;

    @Column(name = "screenName")
    private String screenName;

    @Column(name = "userRealName")
    private String userRealName;

    @Column(name = "profileImageUrl")
    private String profileImageUrl;

    @Column(name = "tagline")
    private String tagline;

    @Column(name = "followersCount")
    private int followersCount;

    @Column(name = "followingCount")
    private int followingCount;

    @Column(name = "profile_use_background_image")
    private Boolean profile_use_background_image;

    @Column(name = "profile_background_image_url_https")
    private String profile_background_image_url_https;

    @Column(name = "profile_background_color")
    private String profile_background_color;

    @Column(name = "profile_text_color")
    private String profile_text_color;

    @Column(name = "main_user")
    private Boolean main_user;

    public Boolean getMain_user() {
        return main_user;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getScreenName() {
        return screenName;
    }

    public long getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getTagline() {
        return tagline;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public Boolean getProfile_use_background_image() {
        return profile_use_background_image;
    }

    public String getProfile_background_image_url_https() {
        return profile_background_image_url_https;
    }

    public String getProfile_background_color() {
        return profile_background_color;
    }

    public String getProfile_text_color() {
        return profile_text_color;
    }

    public User(){
        super();
    }

    public static User fromJSON(JSONObject json, boolean mainuser){
//        ActiveAndroid.beginTransaction();
//        User user = new User();
        try {
            User existingUser = new Select().from(User.class).where("uid = ?", json.getLong("id")).executeSingle();
            if(existingUser!=null){
                return existingUser;
            }else {
                User user = new User();
                user.name = json.getString("name");
                user.uid = json.getLong("id");
                user.screenName = json.getString("screen_name");
                user.profileImageUrl = json.getString("profile_image_url");
                user.userRealName = json.getString("name");
                user.tagline = json.getString("description");
                user.followersCount = json.getInt("followers_count");
                user.followingCount = json.getInt("friends_count");
                user.profile_use_background_image = json.getBoolean("profile_use_background_image");
                if(user.profile_use_background_image){
                    user.profile_background_image_url_https = json.getString("profile_background_image_url_https");
                }
                user.profile_background_color = json.getString("profile_background_color");
                user.profile_text_color = json.getString("profile_text_color");
                user.main_user = mainuser;
                user.save();
                return user;
            }
//            ActiveAndroid.setTransactionSuccessful();
        }catch(JSONException e){
            e.printStackTrace();
        }finally {
//            ActiveAndroid.endTransaction();
        }
//        throw new Exception("soemthing bad happened with user creation");
        return null;
    }

    public static List<User> getAll(String screenName) {
        return new Select()
                .from(User.class)
                .where("screenName == ?", screenName)
                .limit(1)
                .execute();
    }
}
