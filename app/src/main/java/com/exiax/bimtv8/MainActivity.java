package com.exiax.bimtv8;

/**
 * Kevin Bishop - www.kevin-bish.com
 * Prototype BimTV-8 Viewing & Messaging Application for Android
 *
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// TV Imports
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.content.Context;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
//


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.Slide;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements IVLCVout.Callback, GoogleApiClient.OnConnectionFailedListener {


        // TV
        public final static String TAG = "TV";
        private String mFilePath;
        private SurfaceView mSurface;
        private SurfaceHolder holder;
        private LibVLC libvlc;
        private MediaPlayer mMediaPlayer = null;
        private int mVideoWidth;
        private int mVideoHeight;
        private AdView mAdView;
        private InterstitialAd mInterstitialAd;
        public boolean mFullScreen;


        //


    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public TextView signout;
    public TextView invite;
    public ImageButton fullscreen, fulltext, exit, settings;





    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mFirebaseAdapter;


    public boolean isFullScreen() {

        if(mFullScreen){
            return false;
        }else{
            return true;
        }

    }

    public void setFullScreen(boolean fullScreen) {
        fullScreen = false;

        if (mFullScreen) {

            findViewById(R.id.chat).setVisibility(View.GONE);
            findViewById(R.id.bottom_bar).setVisibility(View.GONE);

            mFullScreen = fullScreen;

        }

        else{
            findViewById(R.id.chat).setVisibility(View.VISIBLE);
            findViewById(R.id.bottom_bar).setVisibility(View.VISIBLE);
            mFullScreen = !fullScreen;
        }
    }

    public void toggleFullScreen() {
        setFullScreen(isFullScreen());
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        //These are the things displayed on the recyclerview
        public TextView messageTextView;
        public TextView messengerTextView;
        public TextView timeView;
        public CircleImageView messengerImageView;

        //Constructor
        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
            timeView = (TextView) itemView.findViewById(R.id.timeView);

        }
    }

    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 300;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;
    private static final String MESSAGE_URL = "http://chat.firebase.google.com/message/";

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;

    public void displayInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        //Ads initialize
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-8528486810114891~9825054176");

        //Load the ads
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .tagForChildDirectedTreatment(true)
                .build();
        mAdView.loadAd(adRequest);

        //TV
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8528486810114891/5988330063");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        mFilePath = "https://rebrand.ly/de3ce";

        Log.d(TAG, "CBC TV8 (Barbados) App Created by Exiax00 ");
        mSurface = (SurfaceView) findViewById(R.id.surface);
        holder = mSurface.getHolder();
        //

        Target listObjetivo = new ViewTarget(R.id.invite, this);

        //SharedPreferences to show ShowCaseView a single time
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstime", false)) {
            // run your one time code

            new ShowcaseView.Builder(this, true)
                    .setTarget(listObjetivo)
                    .setContentTitle("Click to invite your friends")
                    .setContentText("Long press on a name to view Google+ accounts")
                    .setStyle(4)
                    .build();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstime", true);
            editor.commit();


        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default username to anonymous.
        mUsername = ANONYMOUS;

        invite = (TextView) findViewById(R.id.invite);

        signout = (TextView) findViewById(R.id.signout);

        fullscreen = (ImageButton) findViewById(R.id.fullscreen);

        fulltext = (ImageButton) findViewById(R.id.fulltext);

        exit = (ImageButton) findViewById(R.id.exit);

        settings = (ImageButton) findViewById(R.id.settings);

        //Signing you out
        signout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFirebaseAuth.signOut();
                        finish();
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                        mUsername = ANONYMOUS;

                        startActivity(new Intent(MainActivity.this, SignInActivity.class));
                        Toast.makeText(MainActivity.this, "You've been signed out.", Toast.LENGTH_SHORT).show();

                    }

                }
        );

        fullscreen.setOnClickListener (
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleFullScreen();
                    }

                }
        );

        fulltext.setOnClickListener (
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleFullScreen();
                    }

                }
        );

        exit.setOnClickListener (
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }

                }
        );

        settings.setOnClickListener (
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            showMenu(v);
                    }

                }
        );


        //Invite text onclick
        invite.setOnClickListener(
                new View.OnClickListener()

    {
        public void onClick (View v){
            releasePlayer();
            sendInvitation();

        }
    }

    );

    // Initialize Firebase Auth

    mFirebaseAuth = FirebaseAuth.getInstance();
    mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if(mFirebaseUser ==null)

    {
        // Not signed in, launch the Sign In activity
        finish();
        startActivity(new Intent(this, SignInActivity.class));
        return;
    } else

    {
        mUsername = mFirebaseUser.getDisplayName();
        if (mFirebaseUser.getPhotoUrl() != null) {
            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }
    }

    mGoogleApiClient = new GoogleApiClient.Builder(this)
            .enableAutoManage(this /* FragmentActivity */,this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addApi(AppInvite.API)
                .build();

    // Initialize ProgressBar and RecyclerView.
    mProgressBar =(ProgressBar) findViewById(R.id.progressBar);

    mMessageRecyclerView =(RecyclerView) findViewById(R.id.messageRecyclerView);

    mLinearLayoutManager = new  LinearLayoutManager(this);
    mLinearLayoutManager.setStackFromEnd(true);
    mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);



    // Initialize Firebase Remote Config.
    mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

    // Define Firebase Remote Config Settings.
    FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
            new FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(true)
                    .build();

    // Define default config values. Defaults are used when fetched config values are not
// available. Eg: if an error occurred fetching values from the server.
    Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("friendly_msg_length",300L);
        defaultConfigMap.put("cbc", "https://rebrand.ly/de3ce");


// Apply config settings and default values.
        mFirebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);


        // Fetch remote config.
    fetchConfig();


// New child entries
    //Get reference to database

    mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();


        mFirebaseDatabaseReference.keepSynced(true);
        //mFirebaseDatabaseReference.getDatabase().setPersistenceEnabled(true);

    //Prepare the adapter
    mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage,
            MessageViewHolder>(
    FriendlyMessage.class,
    R.layout.item_message,
    MessageViewHolder.class, mFirebaseDatabaseReference.child(MESSAGES_CHILD))



    {

        @Override
        public void populateViewHolder (MessageViewHolder viewHolder,
            FriendlyMessage friendlyMessage,int position){
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        viewHolder.messageTextView.setText(friendlyMessage.getText());



       // viewHolder.messengerTextView.setText(friendlyMessage.getName()+" "+friendlyMessage.getCurrentTime());

        viewHolder.messengerTextView.setText(friendlyMessage.getName());

            int[] androidColors = getResources().getIntArray(R.array.androidcolors);
            int randomAndroidColor = androidColors[new Random().nextInt(androidColors.length)];

            viewHolder.messengerTextView.setTextColor(randomAndroidColor);

            viewHolder.timeView.setTextColor(Color.GRAY);

        if (friendlyMessage.getCurrentTime() != null)
            viewHolder.timeView.setText(friendlyMessage.getCurrentTime());
        if (friendlyMessage.getPhotoUrl() == null) {
            viewHolder.messengerImageView
                    .setImageDrawable(ContextCompat
                            .getDrawable(MainActivity.this,
                                    R.drawable.ic_account_circle_black_36dp));
        } else {
            Glide.with(MainActivity.this)
                    .load(friendlyMessage.getPhotoUrl())
                    .into(viewHolder.messengerImageView);
        }

           // Toast.makeText(MainActivity.this, "POPULATED!", Toast.LENGTH_SHORT).show();

    }


          };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()

    {
        @Override
        public void onItemRangeInserted ( int positionStart, int itemCount){
        super.onItemRangeInserted(positionStart, itemCount);
        int friendlyMessageCount = mFirebaseAdapter.getItemCount();
        int lastVisiblePosition =  mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
        // If the recycler view is initially being loaded or the
        // user is at the bottom of the list, scroll to the bottom
        // of the list to show the newly added message.
        if (lastVisiblePosition == -1 ||
                (positionStart >= (friendlyMessageCount - 1) &&
                        lastVisiblePosition == (positionStart - 1))) {
            mMessageRecyclerView.scrollToPosition(positionStart);
        }
    }
    });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    mMessageEditText =(EditText)

    findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]

    {
        new InputFilter.LengthFilter(mSharedPreferences
                .getInt(MyPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))
    });
        mMessageEditText.addTextChangedListener(new

    TextWatcher() {
        @Override
        public void beforeTextChanged (CharSequence charSequence,int i, int i1, int i2){
        }

        @Override
        public void onTextChanged (CharSequence charSequence,int i, int i1, int i2){
            if (charSequence.toString().trim().length() > 0) {
                mSendButton.setEnabled(true);
            } else {
                mSendButton.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged (Editable editable){
        }
    });

        FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = firebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {

                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);

        mSendButton =(Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener()

    {
        @Override
        public void onClick (View view){

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm a");
        String currentTime = dateFormat.format(new Date()).toString();

        //Check whether the message contains any abusive words
        int c = 0;
        String finalMessage = new String(mMessageEditText.getText().toString());
        String filterMessage = finalMessage;

        OffensiveWords of = new OffensiveWords();
        for (String str : of.getOffensive()) {
            //System.out.print(str+" ,");
            if (finalMessage.toLowerCase().contains(str)) {
                filterMessage = filterMessage.toLowerCase().replace(str, " *offensive* ");
                c++; //There is at least one offensive word
            }

        }
        FriendlyMessage friendlyMessage;
        if (c == 0) //No offensive words
        {
            friendlyMessage = new
                    FriendlyMessage(finalMessage,
                    mUsername,
                    mPhotoUrl,
                    currentTime);

        } else {
            friendlyMessage = new
                    FriendlyMessage(filterMessage,
                    mUsername,
                    mPhotoUrl,
                    currentTime);
            showAlertDialog();
        }

        mFirebaseDatabaseReference.child(MESSAGES_CHILD)
                .push().setValue(friendlyMessage);
        mMessageEditText.setText("");
    }
    });

}

    // Fetch the config to determine the allowed length of messages.
    public void fetchConfig() {
        long cacheExpiration = 0; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that
        // each fetch goes to the server. This should not be used in release
        // builds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings()
                .isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Make the fetched config available via
                        // FirebaseRemoteConfig get<type> calls.
                        mFirebaseRemoteConfig.activateFetched();
                        applyRetrievedLengthLimit();
                        applyNew_CBCStream();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // There has been an error fetching the config
                        Log.w(TAG, "Error fetching config: " +
                                e.getMessage());
                        applyRetrievedLengthLimit();
                        applyNew_CBCStream();
                    }
                });
    }


    /**
     * Apply retrieved length limit to edit text field.
     * This result may be fresh from the server or it may be from cached
     * values.
     */
    private void applyRetrievedLengthLimit() {
        Long friendly_msg_length =
                mFirebaseRemoteConfig.getLong("friendly_msg_length");
        mMessageEditText.setFilters(new InputFilter[]{new
                InputFilter.LengthFilter(friendly_msg_length.intValue())});
        Log.d(TAG, "FML is: " + friendly_msg_length);
    }

    private void applyNew_CBCStream() {
        String cbc = mFirebaseRemoteConfig.getString("cbc");

    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            setContentView(R.layout.landscape);

            releasePlayer();
            this.finish();
            setSize(mVideoWidth, mVideoHeight);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);




        }

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.portrait);

            releasePlayer();
            this.finish();
            setSize(mVideoWidth, mVideoHeight);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);



        }


    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();


        //finish();

    }



    @Override
    public void onResume() {

        super.onResume();
        releasePlayer();
        createPlayer(mFilePath);

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        releasePlayer();

       finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releasePlayer();



    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode +
                ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Check how many invitations were sent.
                String[] ids = AppInviteInvitation
                        .getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                // Sending failed or it was canceled, show failure message to
                // the user
                Log.d(TAG, "Sending invitation(s) failed. Please try again!");
            }
        }
        this.finish();
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }

    public void showAlertDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(
                this).create();

        // Setting Dialog Title
        alertDialog.setTitle("BimTV!");
        // String s= (Html.fromHtml("<a href=\"http://www.google.com\">Check this link out</a>")).toString();
        // Setting Dialog Message
        alertDialog.setMessage("No abusive/offensive words please. You'll unnecessarily increase your chances of getting banned!");
        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.icon);

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed
                //Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services Error. Please check your internet connection!", Toast.LENGTH_SHORT).show();
    }

    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        if (holder == null || mSurface == null)
            return;

        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        holder.setFixedSize(mVideoWidth, mVideoHeight);
        LayoutParams lp = mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();

    }

    /**
     * Creates MediaPlayer and plays video
     *
     * @param media
     */
    private void createPlayer(String media) {

        releasePlayer();


        try {

            /**
            if (media.length() > 0) {
                Toast toast = Toast.makeText(this, "Your now watching CBC TV 8, created by Exiax00.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();
            }
            */


            // Create LibVLC

            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            libvlc = new LibVLC(this, options);
            holder.setKeepScreenOn(true);

            // Creating media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);

            // Seting up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(mSurface);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(this);
            vout.attachViews();

            Media m = new Media(libvlc, Uri.parse(media));
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
        } catch (Exception e) {
            Toast.makeText(this, "We have encountered an error. Please restart BimTV!", Toast
                    .LENGTH_LONG).show();
        }
    }

    private void releasePlayer() {
        if (libvlc == null)
            return;
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        holder = null;
        libvlc.release();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    /**
     * Registering callbacks
     */
    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    @Override
    public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0)
            return;

        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    public void onSurfacesCreated(IVLCVout vout) {


    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {
        Log.e(TAG, "Error with hardware acceleration");
        this.releasePlayer();
        Toast.makeText(this, "Problem with Video Hardware Acceleration!", Toast.LENGTH_LONG).show();
    }

private static class MyPlayerListener implements MediaPlayer.EventListener {
    private WeakReference<MainActivity> mOwner;

    public MyPlayerListener(MainActivity owner) {
        mOwner = new WeakReference<MainActivity>(owner);
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        MainActivity player = mOwner.get();

        switch (event.type) {
            case MediaPlayer.Event.EndReached:
                Log.d(TAG, "MediaPlayerEndReached");
                player.releasePlayer();
                break;
            case MediaPlayer.Event.Playing:
            case MediaPlayer.Event.Paused:
            case MediaPlayer.Event.Stopped:
            default:
                break;
        }
    }
}

    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));

                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public void showMenu(View v)
    {
        PopupMenu popup = new PopupMenu(this,v);
        //popup.setOnMenuItemClickListener(a);// to implement on click event on items of menu
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.main_menu, popup.getMenu());
        popup.show();
    }




}