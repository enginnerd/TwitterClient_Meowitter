package com.codepath.apps.meowitter.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.meowitter.R;
import com.codepath.apps.meowitter.activities.ProfileActivity;
import com.codepath.apps.meowitter.activities.TimelineActivity;
import com.codepath.apps.meowitter.app.TwitterApp;
import com.codepath.apps.meowitter.helpers.HelperVars;
import com.codepath.apps.meowitter.models.Compose;
import com.codepath.apps.meowitter.models.Tweet;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by andrewblaich on 3/5/15.
 */
public class TweetsArrayAdapter extends ArrayAdapter<Tweet>{
    private static class ViewHolder {
        ImageView ivProfileImage;
        ImageView ivReply;
        ImageView ivRetweet;
        ImageView ivFavorite;
        TextView tvUserName;
        TextView tvBody;
        TextView tvFullName;
        TextView tvRetweetCount;
        TextView tvFavoriteCount;
        TextView tvTime;
    }

    public TweetsArrayAdapter(Context context, List<Tweet> tweets) {
        super(context, android.R.layout.simple_list_item_1, tweets);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        //apply viewholder pattern here
        final ViewHolder viewHolder;
        Tweet tweet = getItem(position);
        if(convertView == null){
            viewHolder =  new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tweet, parent, false);
            viewHolder.ivProfileImage = (ImageView) convertView.findViewById(R.id.ivProfileImage);
            viewHolder.ivReply = (ImageView) convertView.findViewById(R.id.ivReply);
            viewHolder.ivRetweet = (ImageView) convertView.findViewById(R.id.ivRetweet);
            viewHolder.ivFavorite = (ImageView) convertView.findViewById(R.id.ivFavorite);
            viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUsername);
            viewHolder.tvBody = (TextView)convertView.findViewById(R.id.tvBody);
            viewHolder.tvFullName = (TextView) convertView.findViewById(R.id.tvFullName);
            viewHolder.tvRetweetCount = (TextView) convertView.findViewById(R.id.tvRetweetCount);
            viewHolder.tvFavoriteCount = (TextView) convertView.findViewById(R.id.tvFavoriteCount);
            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        //HANDLE RETWEETED TWEETS LOGIC
        //check tweet.isRetweeted for true

        viewHolder.tvUserName.setText("@"+tweet.getUser().getScreenName());
        viewHolder.tvFullName.setText(tweet.getUser().getUserRealName());

        //official twitter app does not show 0, just hides the text
        if(tweet.getFavoriteCount()<=0){
            viewHolder.tvFavoriteCount.setVisibility(View.INVISIBLE);
        }
        //official twitter app does not show 0, just hides the text
        viewHolder.tvFavoriteCount.setText(String.valueOf(tweet.getFavoriteCount()));
        if(tweet.getRetweetCount()<=0){
            viewHolder.tvRetweetCount.setVisibility(View.INVISIBLE);
        }
        viewHolder.tvRetweetCount.setText(String.valueOf(tweet.getRetweetCount()));
        viewHolder.tvTime.setText(getRelativeTimeAgo(tweet.getCreatedAt()));
        viewHolder.tvBody.setText(tweet.getBody());
        viewHolder.tvBody.setMovementMethod(LinkMovementMethod.getInstance());

        if(!tweet.isFavorited()) {
            viewHolder.ivFavorite.setImageResource(R.drawable.star);
        }else {
            viewHolder.ivFavorite.setImageResource(R.drawable.star_fav);
        }

        final Tweet f_tweet = tweet;
        viewHolder.ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), TimelineActivity.class);
                i.putExtra(HelperVars.INTENT_KEY_FAVORITE, String.valueOf(f_tweet.getUid()));
                if(f_tweet.isFavorited()) { //we fav'd it , but now are removing it
                    i.putExtra(HelperVars.INTENT_KEY_FAVORITE_ACTION, HelperVars.ACTION_DESTROY);
//                    f_tweet.setFavorited(false);
                    viewHolder.ivFavorite.setImageResource(R.drawable.star);
                }else{
                    i.putExtra(HelperVars.INTENT_KEY_FAVORITE_ACTION, HelperVars.ACTION_CREATE);
//                    f_tweet.setFavorited(true);
                    viewHolder.ivFavorite.setImageResource(R.drawable.star_fav);
                }
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                TwitterApp.context.startActivity(i);
            }
        });

        viewHolder.ivRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), TimelineActivity.class);
                i.putExtra(HelperVars.INTENT_KEY_RETWEET, String.valueOf(f_tweet.getUid()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                TwitterApp.context.startActivity(i);
            }
        });

        viewHolder.ivReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Compose c = new Compose();
                c.setReply(true);
                c.setIn_reply_to_status_id(String.valueOf(f_tweet.getUid()));
                c.setReply_userName(f_tweet.getUser().getScreenName());

                Intent i = new Intent(getContext(), TimelineActivity.class);
                i.putExtra(HelperVars.INTENT_KEY_COMPOSE, c);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                TwitterApp.context.startActivity(i);
            }
        });

        viewHolder.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), ProfileActivity.class);
                i.putExtra("screen_name", f_tweet.getUser().getScreenName());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                TwitterApp.context.startActivity(i);
            }
        });


        viewHolder.ivProfileImage.setImageResource(android.R.color.transparent);
        Picasso.with(getContext()).load(tweet.getUser().getProfileImageUrl()).placeholder(R.drawable.catbird).into(viewHolder.ivProfileImage);
        return convertView;
    }

    private String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String[] tokens = relativeDate.split(" ");
        if(tokens.length > 1 ){
            String second = " " + tokens[1];
            if(tokens[1].startsWith("m") || tokens[1].startsWith("h") || tokens[1].startsWith("d")){
                second = tokens[1].substring(0,1);
            }else if(tokens[1].startsWith("seconds")){
                second = "secs";
            }
            relativeDate = tokens[0]+second;
        }
        return relativeDate;
    }

}
