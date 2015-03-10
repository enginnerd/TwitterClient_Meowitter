package com.codepath.apps.meowitter.activities;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.meowitter.R;
import com.codepath.apps.meowitter.app.TwitterApp;
import com.codepath.apps.meowitter.app.TwitterClient;
import com.codepath.apps.meowitter.helpers.HelperVars;
import com.codepath.apps.meowitter.models.Compose;
import com.squareup.picasso.Picasso;

/**
 * Created by andrewblaich on 3/7/15.
 */
public class ComposeDialog extends DialogFragment {

    private static ImageView ivPhoto;
    private static TextView tvUserName;
    private static TextView tvFullName;
    private static TextView tvCount;
    private static EditText etTweetBody;
    private static Button btnTweet;
    private static Compose g_compose;

    public interface ComposeDialogListener {
        void onFinishCompose(Compose compose);
    }


    public ComposeDialog() {
        // Empty constructor required for DialogFragment
    }

    public static ComposeDialog newInstance(Compose compose) {
        ComposeDialog frag = new ComposeDialog();
        Bundle args = new Bundle();
//        args.putString("title", title);
        args.putSerializable(HelperVars.INTENT_KEY_COMPOSE, compose);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_compose, container);;
        g_compose = (Compose)getArguments().getSerializable(HelperVars.INTENT_KEY_COMPOSE);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE|
                Window.FEATURE_SWIPE_TO_DISMISS);

        ivPhoto = (ImageView)view.findViewById(R.id.ivProfileImage);
        tvUserName = (TextView)view.findViewById(R.id.tvUsername);
        tvFullName = (TextView)view.findViewById(R.id.tvFullName);
        tvCount = (TextView)view.findViewById(R.id.tvCount);
        tvCount.setText(String.valueOf(HelperVars.MAX_TWEET_CHARS));

        etTweetBody = (EditText)view.findViewById(R.id.etTweetBody);
        btnTweet = (Button)view.findViewById(R.id.btnTweet);

        tvUserName.setText("@"+g_compose.getUser().getScreenName());
        tvFullName.setText(g_compose.getUser().getUserRealName());

        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTweet();
            }
        });

        etTweetBody.setText(""); //clear any previous text

        if(g_compose.isReply()){
            etTweetBody.setText("@"+g_compose.getReply_userName());
        }

        etTweetBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Fires right as the text is being changed (even supplies the range of text)
                int newCount = HelperVars.MAX_TWEET_CHARS - etTweetBody.getText().length();
                tvCount.setText(String.valueOf(newCount));
                Log.i("DEBUG", "onTextChanged" + String.valueOf(newCount));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // Fires right before text is changing
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Fires right after the text has changed
            }
        });

        Picasso.with(TwitterApp.context).load(g_compose.getUser().getProfileImageUrl()).into(ivPhoto);

        return view;
    }

    private void sendTweet(){
        Log.i("INFO", "Send Tweet called");
        int charCount = HelperVars.MAX_TWEET_CHARS - etTweetBody.getText().length();
        if(charCount < 0){
            Toast.makeText(TwitterApp.context, "Too many characters", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(TwitterApp.context, "Sending Tweet", Toast.LENGTH_SHORT).show();
            g_compose.setMessage(etTweetBody.getText().toString());
            ComposeDialogListener listener = (ComposeDialogListener)getActivity();
            listener.onFinishCompose(g_compose);
            dismiss(); //only dismiss on success
        }

    }
}
