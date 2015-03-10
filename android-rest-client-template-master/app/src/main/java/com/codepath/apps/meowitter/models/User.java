package com.codepath.apps.meowitter.models;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;

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

    public User(){
        super();
    }

    public static User fromJSON(JSONObject json){
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
}
