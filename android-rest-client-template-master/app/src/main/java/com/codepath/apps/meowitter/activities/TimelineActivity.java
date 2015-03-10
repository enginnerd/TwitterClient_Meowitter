package com.codepath.apps.meowitter.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.apps.meowitter.R;
import com.codepath.apps.meowitter.app.TwitterApp;
import com.codepath.apps.meowitter.app.TwitterClient;
import com.codepath.apps.meowitter.adapters.TweetsArrayAdapter;
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

    private TwitterClient client;
    private ArrayList<Tweet> tweets;
    private TweetsArrayAdapter aTweets;
    private ListView lvTweets;
    private SwipeRefreshLayout swipeContainer;
    private ImageView loaderImage;

    private static long last_id = 1;

    private static Compose compose;

    @Override
    public void onFinishCompose(Compose c){
        Log.i("INFO", "onFinishCompose called");
        compose = c;
        sendTweet();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        setupActionBar();
        setupDataAndViews();
        getUserInfo();
        populateTimeline(false);
    }

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
                retweet(id);

            }else if(i.hasExtra(HelperVars.INTENT_KEY_FAVORITE)){
                String id = i.getStringExtra(HelperVars.INTENT_KEY_FAVORITE);
                if(i.getStringExtra(HelperVars.INTENT_KEY_FAVORITE_ACTION).equals(HelperVars.ACTION_CREATE)) {
                    favorite(id);
                }else {
                    removeFavorite(id);
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

//        try {
//            Intent i = getIntent();
//            if(i.hasExtra(HelperVars.INTENT_KEY_COMPOSE)) {
//                Compose c = (Compose) i.getSerializableExtra(HelperVars.INTENT_KEY_COMPOSE);
//                if (c == null) {
//                    logMe("Compose is NULL");
//                } else {
//                    outside_composeTweet(c);
//                }
//            }else{
//                logMe("Intent is missing key: " + HelperVars.INTENT_KEY_COMPOSE);
//            }
//        }catch(Exception e){
//            Log.i("DEBUG", "exception here is fine to swallow");
//            e.printStackTrace();
//        }
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.twitter);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setupDataAndViews(){
        lvTweets = (ListView)findViewById(R.id.lvTweets);
        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(this, tweets);
        lvTweets.setAdapter(aTweets);
        client = TwitterApp.getRestClient();
        loaderImage = (ImageView)findViewById(R.id.ivLoadImage);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
//                aTweets.clear();
                loaderImage.setVisibility(View.VISIBLE);
                last_id = 1;
                populateTimeline(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
//                customLoadMoreDataFromApi(page);
                // or customLoadMoreDataFromApi(totalItemsCount);;
                Toast.makeText(getApplicationContext(), "Getting more data", Toast.LENGTH_SHORT).show();
                logMe("Page: " + String.valueOf(page) + " TotalItemsCount: " + String.valueOf(totalItemsCount));
//                if(totalItemsCount>=25) {
                    populateTimeline(true);
//                }

            }
        });
    }

    private void logMe(String msg){
        Log.i("DEBUG", msg);
    }

    private void favorite(String id){
        client.postFavorite(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d("DEBUG", json.toString());
//                populateTimeline();
                updateTweet(json);
                Toast.makeText(getApplicationContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
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
        }, id);
    }

    private void removeFavorite(String id){
        client.postRemoveFavorite(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d("DEBUG", json.toString());
//                populateTimeline();
                updateTweet(json);
                Toast.makeText(getApplicationContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
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
        }, id);
    }

    private void updateTweet(JSONObject json){
        Tweet nTweet = Tweet.fromJSON(json); //persist returned tweet data and overwrite what we have?
        nTweet.save();
//        Tweet tweet;
//        for(int i=0 ; i<aTweets.getCount() ; i++){
//            tweet = aTweets.getItem(i);
//            if(tweet.getUid() == nTweet.getUid()){
////                aTweets.insert(nTweet, i);
//            }
//        }
//        aTweets.notifyDataSetChanged();
        getTweets(false); //check this
    }

    private void retweet(String id){
        client.postRetweet(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d("DEBUG", json.toString());
//                populateTimeline();
                updateTweet(json);
                //how to best update the arrayAdapter?
                Toast.makeText(getApplicationContext(), "Re-tweeted to your followers", Toast.LENGTH_SHORT).show();
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
        }, id);
    }

    private void sendTweet(){
        client.postStatus(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d("DEBUG", json.toString());
//                populateTimeline(false);
                updateTweet(json);
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
        }, compose);
    }

    private void getUserInfo(){
            compose = new Compose();
            client.getVerifyCredentials(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                    Log.d("DEBUG", json.toString());
                    try {
                        User user = User.fromJSON(json);
                        compose.setUser(user);
                    }catch(Exception e){
                        e.printStackTrace();
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
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
    }

    private void getTweets(boolean dontClear){
        if(!dontClear){
            last_id = 1;
        }
        List<Tweet> tweets = Tweet.getAll(last_id); //need to handle paging and such...
        if(tweets!=null && tweets.size()>0) {
            Tweet lastTweet = tweets.get(tweets.size() - 1);
            last_id = lastTweet.getUid();
            Log.i("DEBUG", "Last id: " + last_id);
        }
        for(int i=0; i<tweets.size(); i++){
            Log.i("TWEET", tweets.get(i).getUid() + " " +tweets.get(i).getBody());
        }
        if(!dontClear) {
            aTweets.clear();
        }
        aTweets.addAll(tweets);
        aTweets.notifyDataSetChanged();
        swipeContainer.setRefreshing(false);
        loaderImage.setVisibility(View.INVISIBLE);
    }

//    private Runnable mTimelineRunnable = new Runnable()
//    {
//        @Override
//        public void run()
//        {
    private void populateTimeline_real(boolean dontClear){
            int count = HelperVars.TWEET_RETRIEVE_COUNT;
            long max_id = last_id;
            final boolean f_dontClear = dontClear;
            Log.i("DEBUG", "since_id: " + max_id);
            client.getHomeTimeline(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                    Log.d("DEBUG", json.toString());
                    /*ArrayList<Tweet> tweets = */
                    Tweet.fromJSONArray(json); //add to DB
                    getTweets(f_dontClear);

                    if(tweets==null || tweets.size()<=0){
                        Toast.makeText(getApplicationContext(), "No tweets", Toast.LENGTH_SHORT).show();
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
//                                getTweets(f_dontClear);
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), "No response, showing cached data.", Toast.LENGTH_SHORT).show();
                        }
//                        getTweets(f_dontClear);
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                    getTweets(f_dontClear);
                    swipeContainer.setRefreshing(false);
                }
            }, count, max_id);
        }
//    };

    private void populateTimeline(boolean clear){
//        Handler myHandler = new Handler();
//        myHandler.postDelayed(mTimelineRunnable, 100); //wait 1.5 seconds till running so we get the updated data from twitter
        populateTimeline_real(clear);
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
                    compose.setReply(false);
                    composeTweet();
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
        compose.setReply(c.isReply());
        compose.setIn_reply_to_status_id(c.getIn_reply_to_status_id());
        compose.setReply_userName(c.getReply_userName());
//        logMe(compose.toString());

        composeTweet();
    }
    private void composeTweet(){
//        Intent i = new Inten(TimelineActivity.this, Compos)
        if(HelperMethods.isNetworkAvailable(this)) {
            FragmentManager fm = getSupportFragmentManager();
            ComposeDialog composeDialog = ComposeDialog.newInstance(compose);
            composeDialog.show(fm, "Compose");
        }else{
            Toast.makeText(getApplicationContext(), "Network access required", Toast.LENGTH_SHORT).show();
        }
    }
}
