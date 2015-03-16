package com.codepath.apps.meowitter.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.apps.meowitter.R;
import com.codepath.apps.meowitter.adapters.TweetsArrayAdapter;
import com.codepath.apps.meowitter.app.TwitterApp;
import com.codepath.apps.meowitter.app.TwitterClient;
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

/**
 * Created by andrewblaich on 3/14/15.
 */
public class TweetsListFragment extends Fragment{

    private ArrayList<Tweet> tweets;
    private TweetsArrayAdapter aTweets;
    private ListView lvTweets;
    private SwipeRefreshLayout swipeContainer;
    private ImageView loaderImage;
//    private static long last_id = 1;
//    private static Compose compose;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_tweets_list, parent, false);
        setupDataAndViews(v);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setupNoViewNeeded();
    }

    public void addAll(List<Tweet> tweets){
        aTweets.addAll(tweets);
        aTweets.notifyDataSetChanged();
    }

    public void clearAll(){
        aTweets.clear();
//        aTweets.notifyDataSetChanged();
    }

    public void swipeContainerNotify(boolean value){
        swipeContainer.setRefreshing(value);
    }

    public void setLoaderImage(int status) {
        loaderImage.setVisibility(status);
    }

    private void setupNoViewNeeded(){
        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(getActivity(), tweets);

    }

    private void setupDataAndViews(View v){
        lvTweets = (ListView)v.findViewById(R.id.lvTweets);
//        tweets = new ArrayList<>();
//        aTweets = new TweetsArrayAdapter(getActivity(), tweets);
        lvTweets.setAdapter(aTweets);
//        client = TwitterApp.getRestClient();
        loaderImage = (ImageView)v.findViewById(R.id.ivLoadImage);

        swipeContainer = (SwipeRefreshLayout)v.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
//                aTweets.clear();
                loaderImage.setVisibility(View.VISIBLE);
//                last_id = 1; //TODO: bring back
//                populateTimeline(false); //TODO: bring back
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
                Toast.makeText(getActivity(), "Getting more data", Toast.LENGTH_SHORT).show();
                logMe("Page: " + String.valueOf(page) + " TotalItemsCount: " + String.valueOf(totalItemsCount));
//                if(totalItemsCount>=25) {
//                populateTimeline(true); //TODO: bring back
//                }
                populateTimeline_frag(1);

            }
        });
    }

    protected void populateTimeline_frag(long id){
        ;
    }

    private void logMe(String msg){
        Log.i("DEBUG", msg);
    }

    public void favorite(String id, TwitterClient client, final long last_id){
        client.postFavorite(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d("DEBUG", json.toString());
//                populateTimeline();
                updateTweet(json, last_id, Tweet.PASS);
                Toast.makeText(TwitterApp.context, "Added to favorites", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    if(errorResponse!=null) {
                        JSONArray json = errorResponse.getJSONArray("errors");
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jobj = json.getJSONObject(i);
                            Toast.makeText(TwitterApp.context, jobj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(TwitterApp.context, "No response", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, id);
    }

    public void removeFavorite(String id, TwitterClient client, final long last_id){
        client.postRemoveFavorite(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d("DEBUG", json.toString());
//                populateTimeline();
                updateTweet(json, last_id, Tweet.PASS);
                Toast.makeText(TwitterApp.context, "Removed from favorites", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    if(errorResponse!=null) {
                        JSONArray json = errorResponse.getJSONArray("errors");
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jobj = json.getJSONObject(i);
                            Toast.makeText(TwitterApp.context, jobj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(TwitterApp.context, "No response", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, id);
    }

    public void updateTweet(JSONObject json, long last_id, String type){
        Tweet nTweet = Tweet.fromJSON(json, type, TwitterApp.g_user.getScreenName()); //persist returned tweet data and overwrite what we have?
        nTweet.save();
//        Tweet tweet;
//        for(int i=0 ; i<aTweets.getCount() ; i++){
//            tweet = aTweets.getItem(i);
//            if(tweet.getUid() == nTweet.getUid()){
////                aTweets.insert(nTweet, i);
//            }
//        }
//        aTweets.notifyDataSetChanged();
        getTweets(false, last_id, type, TwitterApp.g_user.getScreenName()); //check this
    }

    public void retweet(String id, TwitterClient client,final long last_id){
        client.postRetweet(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d("DEBUG", json.toString());
//                populateTimeline();
//                updateTweet(json, last_id, Tweet.PASS); //bypass this
                //how to best update the arrayAdapter?
                Toast.makeText(TwitterApp.context, "Re-tweeted to your followers", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    if(errorResponse!=null) {
                        JSONArray json = errorResponse.getJSONArray("errors");
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jobj = json.getJSONObject(i);
                            Toast.makeText(TwitterApp.context, jobj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(TwitterApp.context, "No response", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, id);
    }

    public void getUserInfo(TwitterClient client){
        TwitterApp.g_compose = new Compose();
        client.getVerifyCredentials(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d("DEBUG", json.toString());
                try {
                    User user = User.fromJSON(json, true);
                    TwitterApp.g_user = user;
                    TwitterApp.g_compose.setUser(user);
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
                            Toast.makeText(TwitterApp.context, jobj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public long getTweets(boolean dontClear, long last_id, String type, String screenName){
        if(!dontClear){
            last_id = 1;
        }
        List<Tweet> tweets;
        if(type.equals(Tweet.REGULAR) || type.equals(Tweet.PASS))
            tweets = Tweet.getAll(last_id); //need to handle paging and such...
        else if(type.equals(Tweet.MENTIONS) || type.equals(Tweet.PROFILE)){
            Log.i("DEBUG", "Grabbing mention tweets");
            tweets = Tweet.getAll(last_id, type, screenName);
            Log.i("DEBUG", "Grabbing mention tweets size of "+ tweets.size());
        }
        else{
            tweets = null;
        }
        if(tweets!=null && tweets.size()>0) {
            Tweet lastTweet = tweets.get(tweets.size() - 1);
            last_id = lastTweet.getUid();
            Log.i("DEBUG", "Last id: " + last_id);
        }
        for(int i=0; i<tweets.size(); i++){
            Log.i("TWEET", tweets.get(i).getUid() + " " +tweets.get(i).getBody());
        }
        if(!dontClear) {
            clearAll();
        }
        addAll(tweets);

        swipeContainerNotify(false);
        setLoaderImage(View.INVISIBLE);
        return last_id;
    }

}
