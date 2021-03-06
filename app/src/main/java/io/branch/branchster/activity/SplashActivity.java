package io.branch.branchster.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import io.branch.branchster.R;
import io.branch.branchster.util.MonsterObject;
import io.branch.branchster.util.MonsterPreferences;
import io.branch.branchster.util.TrackerUtil;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;

public class SplashActivity extends Activity {

    TextView txtLoading;
    int messageIndex;
    private static final String TAG = "SplashActivity";
    ImageView imgSplash1, imgSplash2;
    Context mContext;
    final int ANIM_DURATION = 1500;
    boolean isAsyncTaskComplete = false;
    MonsterObject deepLinkMonsterObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mContext = this;

        // Get loading messages from XML definitions.
        final String[] loadingMessages = getResources().getStringArray(R.array.loading_messages);
        txtLoading = (TextView) findViewById(R.id.txtLoading);
        imgSplash1 = (ImageView) findViewById(R.id.imgSplashFactory1);
        imgSplash2 = (ImageView) findViewById(R.id.imgSplashFactory2);
        imgSplash2.setVisibility(View.INVISIBLE);
        imgSplash1.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        TrackerUtil.logBranchInit(getApplicationContext());

        // Initialize Branch session.
        Branch branch = Branch.getInstance();


        // Branch init
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user clicked -> was re-directed to this app
                    // params will be empty if no data found
                    // ... insert custom logic here ...
                    createDeeplinkMonsterIfFound(referringParams);
                    Log.i("BRANCH SDK", referringParams.toString());
                } else {
                    Log.i("BRANCH SDK", error.getMessage());
                }
                isAsyncTaskComplete = true;
            }
        }, this.getIntent().getData(), this);

//        while(!isAsyncTaskComplete) {
//        }
        proceedToAppTransparent();

    }

    private void createDeeplinkMonsterIfFound(JSONObject referringParams){
        // If a monster was linked to, open the viewer Activity to that Monster.
        MonsterObject obj = new MonsterObject();
        try {
            obj.setMonsterName(referringParams.getString(MonsterPreferences.KEY_MONSTER_NAME));
            obj.setColorIndex(Integer.parseInt(referringParams.getString(MonsterPreferences.KEY_COLOR_INDEX)));
            obj.setBodyIndex(Integer.parseInt(referringParams.getString(MonsterPreferences.KEY_BODY_INDEX)));
            obj.setFaceIndex(Integer.parseInt(referringParams.getString(MonsterPreferences.KEY_FACE_INDEX)));
            obj.setMonsterDescription(referringParams.getString(MonsterPreferences.KEY_MONSTER_DESCRIPTION));

        }catch (Exception e){
            return;
//            Toast.makeText(getApplicationContext(), "improper monster data", Toast.LENGTH_LONG).show();
        }
        deepLinkMonsterObj = obj;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }

    /**
     * Opens the appropriate next Activity, based on whether a Monster has been saved in {@link MonsterPreferences}.
     */
    private void proceedToApp() {
        MonsterPreferences prefs = MonsterPreferences.getInstance(getApplicationContext());
        Intent intent;

        if(null != deepLinkMonsterObj){
            intent = new Intent(SplashActivity.this, MonsterViewerActivity.class);
            intent.putExtra(MonsterViewerActivity.MY_MONSTER_OBJ_KEY, deepLinkMonsterObj);
        }else if (prefs.getMonsterName() == null || prefs.getMonsterName().length() == 0) {
            prefs.setMonsterName("");
            intent = new Intent(SplashActivity.this, MonsterCreatorActivity.class);
        } else {
            // Create a default monster
            intent = new Intent(SplashActivity.this, MonsterViewerActivity.class);
            intent.putExtra(MonsterViewerActivity.MY_MONSTER_OBJ_KEY, prefs.getLatestMonsterObj());
        }

        startActivity(intent);
        finish();
    }

    /**
     * Displays an animation to start the app. Once the animation has finished, will call {@link #proceedToApp()}.
     */
    private void proceedToAppTransparent() {
        Animation animSlideIn = AnimationUtils.loadAnimation(mContext, R.anim.push_down_in);
        animSlideIn.setDuration(ANIM_DURATION);
        animSlideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                proceedToApp();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imgSplash1.setVisibility(View.VISIBLE);
        imgSplash2.setVisibility(View.VISIBLE);
        imgSplash2.startAnimation(animSlideIn);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
