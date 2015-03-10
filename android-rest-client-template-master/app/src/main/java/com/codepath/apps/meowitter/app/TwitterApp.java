package com.codepath.apps.meowitter.app;

import android.content.Context;

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

	@Override
	public void onCreate() {
		super.onCreate();
		TwitterApp.context = this;
	}

	public static TwitterClient getRestClient() {
		return (TwitterClient) TwitterClient.getInstance(TwitterClient.class, TwitterApp.context);
	}
}