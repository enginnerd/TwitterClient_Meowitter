package com.codepath.apps.meowitter.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.meowitter.app.TwitterApp;
import com.codepath.apps.meowitter.app.TwitterClient;
import com.codepath.apps.meowitter.helpers.HelperVars;
import com.codepath.apps.meowitter.models.Compose;
import com.codepath.apps.meowitter.models.Tweet;
import com.codepath.apps.meowitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by andrewblaich on 3/14/15.
 */
public class HomeTimelineFragment extends TweetsListFragment {
    private TwitterClient client;
    private static long last_id = 1;
//    private static Compose compose;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        client = TwitterApp.getRestClient();
//        populateTimeline(false);
        populateTimeline(false);
        getUserInfo(client);
    }

    @Override
    public void populateTimeline_frag(long id){
        populateTimeline(true);
    }

    private void populateTimeline(boolean clear){
//        Handler myHandler = new Handler();
//        myHandler.postDelayed(mTimelineRunnable, 100); //wait 1.5 seconds till running so we get the updated data from twitter
        populateTimeline_real(clear);
    }

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
                Tweet.fromJSONArray(json, Tweet.REGULAR, /*TwitterApp.g_user.getScreenName()*/"khiemkardashian"); //add to DB
                last_id = getTweets(f_dontClear, last_id, Tweet.REGULAR, /*TwitterApp.g_user.getScreenName()*/"khiemkardashian");

//                    if(tweets==null || tweets.size()<=0){
//                        Toast.makeText(getApplicationContext(), "No tweets", Toast.LENGTH_SHORT).show();
//                    }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    if(errorResponse!=null) {
                        JSONArray json = errorResponse.getJSONArray("errors");
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jobj = json.getJSONObject(i);
                            Toast.makeText(getActivity(), jobj.getString("message"), Toast.LENGTH_SHORT).show();
//                                getTweets(f_dontClear);
                        }
                    }else{
                        Toast.makeText(getActivity(), "No response, showing cached data.", Toast.LENGTH_SHORT).show();
                    }
//                        getTweets(f_dontClear);
                }catch(JSONException e){
                    e.printStackTrace();
                }
                getTweets(f_dontClear, last_id, Tweet.REGULAR, TwitterApp.g_user.getScreenName());
                swipeContainerNotify(false);
            }
        }, count, max_id);
    }




}
