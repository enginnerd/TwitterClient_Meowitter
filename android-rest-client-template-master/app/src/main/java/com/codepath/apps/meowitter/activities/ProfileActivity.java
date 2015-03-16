package com.codepath.apps.meowitter.activities;

import android.graphics.Color;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.meowitter.R;
import com.codepath.apps.meowitter.app.TwitterApp;
import com.codepath.apps.meowitter.app.TwitterClient;
import com.codepath.apps.meowitter.fragments.UserTimelineFragment;
import com.codepath.apps.meowitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileActivity extends ActionBarActivity {
    TwitterClient client;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final String screenName = getIntent().getStringExtra("screen_name");
        client = TwitterApp.getRestClient();
        client.getUserInfo(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                boolean mainuser = false;
                if(screenName.equals(/*TwitterApp.g_user.getScreenName()*/"khiemkardashian")){
                    mainuser = true;
                }
                user = User.fromJSON(response, mainuser);
                //my current user account information
                getSupportActionBar().setTitle("@"+user.getScreenName());
                populateProfileHeader(user);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    if (errorResponse != null) {
                        JSONArray json = errorResponse.getJSONArray("errors");
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jobj = json.getJSONObject(i);
                            Toast.makeText(getApplicationContext(), jobj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No response, showing cached data.", Toast.LENGTH_SHORT).show();
                    }
                    //get a cached value if possible
//                    List<User> users = User.getAll()

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //add on error handling
        }, screenName);
        //get screen name

        if(savedInstanceState == null) {
            UserTimelineFragment fragmentUserTimeline = UserTimelineFragment.newInstance(screenName);
            //insert timeline dynamically
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flContainer, fragmentUserTimeline);
            ft.commit();
        }
    }

    private String formatNumbers(String input) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(input);
        NumberFormat nf = NumberFormat.getInstance();
        StringBuffer sb = new StringBuffer();
        while(m.find()) {
            String g = m.group();
            m.appendReplacement(sb, nf.format(Double.parseDouble(g)));
        }
        return m.appendTail(sb).toString();
    }

    private void populateProfileHeader(User user){
        RelativeLayout rlUserHeader = (RelativeLayout)findViewById(R.id.rlUserHeader);
//        rlUserHeader.setBackgroundColor(Color.parseColor("#"+user.getProfile_background_color()));
        TextView tvName = (TextView)findViewById(R.id.tvName);
        TextView tvTagline = (TextView)findViewById(R.id.tvTagLine);
        TextView tvFollowers = (TextView)findViewById(R.id.tvFollowers);
        TextView tvFollowing = (TextView)findViewById(R.id.tvFollowing);
        ImageView ivProfileImage = (ImageView)findViewById(R.id.ivProfileImage);
        ImageView ivBackground = (ImageView)findViewById(R.id.ivBackground);
        tvName.setText(user.getName());
        tvName.setTextColor(Color.parseColor("#"+user.getProfile_text_color()));
        tvTagline.setText(user.getTagline());
        tvTagline.setTextColor(Color.parseColor("#"+user.getProfile_text_color()));
        tvFollowers.setText(formatNumbers(String.valueOf(user.getFollowersCount())) + " Followers");
        tvFollowing.setText(formatNumbers(String.valueOf(user.getFollowingCount())) + " Following");
        Picasso.with(this).load(user.getProfileImageUrl()).placeholder(R.drawable.catbird).into(ivProfileImage);
        if(user.getProfile_use_background_image()){
            Picasso.with(this).load(user.getProfile_background_image_url_https()).placeholder(R.drawable.catbird).fit().into(ivBackground);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
