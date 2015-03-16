package com.codepath.apps.meowitter.app;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.FlickrApi;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;
import android.util.Log;

import com.codepath.apps.meowitter.models.Compose;
import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1/"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "";       // Change this
	public static final String REST_CONSUMER_SECRET = ""; // Change this
	public static final String REST_CALLBACK_URL = "oauth://cpmeowitter"; // Change this (here and in manifest)

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */

    public void getHomeTimeline(AsyncHttpResponseHandler handler, int count, long max_id){
        String apiUrl = getApiUrl("statuses/home_timeline.json");

        RequestParams params = new RequestParams();
        params.put("count", /*25*/count);
        if(max_id == 1) {
            params.put("since_id", /*1*/max_id);
        } else{
            params.put("max_id", /*1*/max_id);
        }

        getClient().get(apiUrl, params, handler);
    }

    public void getMentionsTimeline(AsyncHttpResponseHandler handler, int count, long max_id){
        String apiUrl = getApiUrl("statuses/mentions_timeline.json");
        RequestParams params = new RequestParams();
        params.put("count", /*25*/count);
        getClient().get(apiUrl, params, handler);
    }

    public void getVerifyCredentials(AsyncHttpResponseHandler handler){
        String apiUrl = getApiUrl("account/verify_credentials.json");
        RequestParams params = new RequestParams();
        getClient().get(apiUrl, params, handler);
    }

    public void postStatus(AsyncHttpResponseHandler handler, Compose compose){
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", compose.getMessage());
        getClient().post(apiUrl, params, handler);
    }

    public void postRetweet(AsyncHttpResponseHandler handler, String id){
        String apiUrl = getApiUrl("statuses/retweet/"+id+".json");
        Log.i("DEBUG", "postRetweet to " + apiUrl);
        RequestParams params = new RequestParams();
        params.put("id", Long.valueOf(id));
        getClient().post(apiUrl, params, handler);
    }

    public void postFavorite(AsyncHttpResponseHandler handler, String id){
        String apiUrl = getApiUrl("favorites/create.json");
        Log.i("DEBUG", "postFavorite to " + apiUrl);
        RequestParams params = new RequestParams();
        params.put("id", Long.valueOf(id));
        getClient().post(apiUrl, params, handler);
    }

    public void postRemoveFavorite(AsyncHttpResponseHandler handler, String id){
        String apiUrl = getApiUrl("favorites/destroy.json");
        Log.i("DEBUG", "postRemoveFavorite to " + apiUrl);
        RequestParams params = new RequestParams();
        params.put("id", Long.valueOf(id));
        getClient().post(apiUrl, params, handler);
    }

    public void getUserTimeline(String screenName, AsyncHttpResponseHandler handler){
        String apiUrl = getApiUrl("statuses/user_timeline.json");
        RequestParams params = new RequestParams();
        params.put("count", 25);
        params.put("screen_name", screenName);
        getClient().get(apiUrl, params, handler);
    }

    public void getUserInfo(AsyncHttpResponseHandler handler, String screenName){
//        String apiUrl = getApiUrl("account/verify_credentials.json");
        String apiUrl = getApiUrl("users/show.json");
        RequestParams params = new RequestParams();
        params.put("screen_name", screenName);
        getClient().get(apiUrl, params, handler);
    }
}