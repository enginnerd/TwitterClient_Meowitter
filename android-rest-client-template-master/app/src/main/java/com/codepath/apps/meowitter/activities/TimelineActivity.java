package com.codepath.apps.meowitter.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.meowitter.R;
import com.codepath.apps.meowitter.app.TwitterApp;
import com.codepath.apps.meowitter.app.TwitterClient;
import com.codepath.apps.meowitter.adapters.TweetsArrayAdapter;
import com.codepath.apps.meowitter.fragments.HomeTimelineFragment;
import com.codepath.apps.meowitter.fragments.MentionsTimelineFragment;
import com.codepath.apps.meowitter.fragments.TweetsListFragment;
import com.codepath.apps.meowitter.helpers.HelperMethods;
import com.codepath.apps.meowitter.helpers.HelperVars;
import com.codepath.apps.meowitter.listeners.EndlessScrollListener;
import com.codepath.apps.meowitter.models.Compose;
import com.codepath.apps.meowitter.models.Tweet;
import com.codepath.apps.meowitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TimelineActivity extends ActionBarActivity implements ComposeDialog.ComposeDialogListener {


    private static long last_id = 1;
//    private static Compose compose;
//    private ArrayList<Tweet> tweets;
    private static TweetsListFragment fragmentTweetsList;
    private static TwitterClient client;

    @Override
    public void onFinishCompose(Compose c){
        Log.i("INFO", "onFinishCompose called");
        TwitterApp.g_compose = c;
        sendTweet();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        setupActionBar();
//        TwitterApp.g_compose = new Compose();
        client = TwitterApp.getRestClient();
        fragmentTweetsList = new TweetsListFragment();

        //get the viewpager
        ViewPager viewPager = (ViewPager)findViewById(R.id.viewpager);
        //set the viewpager adapter for the pager
        viewPager.setAdapter(new TweetsPagerAdapter(getSupportFragmentManager()));
        //find the proper sliding tab
        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip)findViewById(R.id.tabs);
        //attach the tabstrip to the viewpager
        tabStrip.setViewPager(viewPager);
    }

    //TODO: bring this back
    @Override
    protected void onNewIntent(Intent newIntent){
        try {
            Intent i = newIntent;
            if(i.hasExtra(HelperVars.INTENT_KEY_COMPOSE)) {
                Compose c = (Compose) i.getSerializableExtra(HelperVars.INTENT_KEY_COMPOSE);
                if (c == null) {
                    logMe("Compose is NULL");
                } else {
                    outside_composeTweet(c);
                }
            }else if(i.hasExtra(HelperVars.INTENT_KEY_RETWEET)){
                String id = i.getStringExtra(HelperVars.INTENT_KEY_RETWEET);
                fragmentTweetsList.retweet(id, client, 1);

            }else if(i.hasExtra(HelperVars.INTENT_KEY_FAVORITE)){
                String id = i.getStringExtra(HelperVars.INTENT_KEY_FAVORITE);
                if(i.getStringExtra(HelperVars.INTENT_KEY_FAVORITE_ACTION).equals(HelperVars.ACTION_CREATE)) {
                    fragmentTweetsList.favorite(id, client, 1);
                }else {
                    fragmentTweetsList.removeFavorite(id, client, 1);
                }
            }else{
                logMe("Intent is missing a key");
            }
        }catch(Exception e){
            Log.i("DEBUG", "exception here is fine to swallow");
            e.printStackTrace();
        }
    }


    @Override
    protected  void onResume(){
        super.onResume();
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.twitter);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }



    private void logMe(String msg){
        Log.i("DEBUG", msg);
    }


//TODO: bring this back
    private void sendTweet(){
        client.postStatus(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d("DEBUG", json.toString());
//                populateTimeline(false);
//                fragmentTweetsList.updateTweet(json, 1, Tweet.REGULAR);
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                for (Fragment fragment : fragments) {
                    if(fragment instanceof HomeTimelineFragment) {
                        HomeTimelineFragment homeTimeLineFragment = (HomeTimelineFragment) fragment;
                        homeTimeLineFragment.updateTweet(json, 1, Tweet.REGULAR);
                        break;
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    if(errorResponse!=null) {
                        JSONArray json = errorResponse.getJSONArray("errors");
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jobj = json.getJSONObject(i);
                            Toast.makeText(getApplicationContext(), jobj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "No response", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, TwitterApp.g_compose);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        MenuItem composeItem = menu.findItem(R.id.miCompose);
        composeItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.miCompose) {
                    TwitterApp.g_compose.setReply(false);
                    composeTweet();
                }
                return true;
            }
        });
        MenuItem profileItem = menu.findItem(R.id.miProfile);
        profileItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.miProfile) {
                    onProfileView();
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private void outside_composeTweet(Compose c){
        TwitterApp.g_compose.setReply(c.isReply());
        TwitterApp.g_compose.setIn_reply_to_status_id(c.getIn_reply_to_status_id());
        TwitterApp.g_compose.setReply_userName(c.getReply_userName());
        composeTweet();
    }

    private void composeTweet(){
        if(HelperMethods.isNetworkAvailable(this)) {
            FragmentManager fm = getSupportFragmentManager();
            ComposeDialog composeDialog = ComposeDialog.newInstance(TwitterApp.g_compose);
            composeDialog.show(fm, "Compose");
        }else{
            Toast.makeText(getApplicationContext(), "Network access required", Toast.LENGTH_SHORT).show();
        }
    }

    //return order of fragments in the viewpager
    // alt enter implement methods
    public class TweetsPagerAdapter extends FragmentPagerAdapter{
//        final int PAGE_COUNT = 2;
        private String tabTitles[] = {"Home", "Mentions"};

        public TweetsPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0){
                return new HomeTimelineFragment();
            }else if(position == 1){
                return new MentionsTimelineFragment();
            }else{
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position){
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }
    }


    public void onProfileView(){
        Intent i = new Intent(this, ProfileActivity.class);
        i.putExtra("screen_name", "khiemkardashian");
        startActivity(i);
    }
}
