package com.codepath.apps.meowitter.app;

import android.content.Context;

import com.codepath.apps.meowitter.models.Compose;
import com.codepath.apps.meowitter.models.User;

/*
 * This is the Android application itself and is used to configure various settings
 * including the image cache in memory and on disk. This also adds a singleton
 * for accessing the relevant rest client.
 *
 *     RestClient client = RestApplication.getRestClient();
 *     // use client to send requests to API
 *
 */
public class TwitterApp extends com.activeandroid.app.Application {
	/*private*/public static Context context;
    public static User g_user; //populate with current user, quick access
    public static Compose g_compose; //cheating, but acceptable for now

	@Override
	public void onCreate() {
		super.onCreate();
		TwitterApp.context = this;
	}

	public static TwitterClient getRestClient() {
		return (TwitterClient) TwitterClient.getInstance(TwitterClient.class, TwitterApp.context);
	}
}