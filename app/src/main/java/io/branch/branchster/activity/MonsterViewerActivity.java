package io.branch.branchster.activity;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

import io.branch.branchster.R;
import io.branch.branchster.fragment.InfoFragment;
import io.branch.branchster.util.MonsterImageView;
import io.branch.branchster.util.MonsterObject;
import io.branch.branchster.util.MonsterPreferences;
import io.branch.branchster.util.TrackerUtil;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;

public class MonsterViewerActivity extends FragmentActivity implements InfoFragment.OnFragmentInteractionListener {
    static final int SEND_SMS = 12345;

    private static String TAG = MonsterViewerActivity.class.getSimpleName();
    public static final String MY_MONSTER_OBJ_KEY = "my_monster_obj_key";

    TextView monsterUrl;
    View progressBar;

    MonsterImageView monsterImageView_;
    MonsterObject myMonsterObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monster_viewer);

        Map<String, String> monsterMetaData = MonsterPreferences.getInstance(getApplicationContext())
                                                                .getLatestMonsterObj()
                                                                .monsterMetaData();
        TrackerUtil.logMonsterView(getApplicationContext(), monsterMetaData);

        monsterImageView_ = (MonsterImageView) findViewById(R.id.monster_img_view);
        monsterUrl = (TextView) findViewById(R.id.shareUrl);
        progressBar = findViewById(R.id.progress_bar);

        // Change monster
        findViewById(R.id.cmdChange).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MonsterCreatorActivity.class);
                startActivity(i);
                finish();
            }
        });

        // More info
        findViewById(R.id.infoButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                InfoFragment infoFragment = InfoFragment.newInstance();
                ft.replace(R.id.container, infoFragment).addToBackStack("info_container").commit();
            }
        });

        //Share monster
        findViewById(R.id.share_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                shareMyMonster();
            }
        });

        initUI();
    }

    private void initUI() {
        myMonsterObject = getIntent().getParcelableExtra(MY_MONSTER_OBJ_KEY);

        if (myMonsterObject != null) {
            String monsterName = myMonsterObject.getMonsterName(); //getString(R.string.monster_name);

            LinkProperties linkProperties = createSmsLinkProperties();
            ContentMetadata cm = createContentMetadata(myMonsterObject);
            BranchUniversalObject buo = createBranchUniversalObject(cm, myMonsterObject);

            buo.generateShortUrl(getApplicationContext(), linkProperties, new Branch.BranchLinkCreateListener() {
                @Override
                public void onLinkCreate(String url, BranchError error) {
                    if(error == null){
                        monsterUrl.setText(url);
                        TrackerUtil.logEvent(getApplicationContext(), TrackerUtil.Event.branch_url_created);
                    }else{
                        TrackerUtil.logEvent(getApplicationContext(), TrackerUtil.Event.branch_error);
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });

            if (!TextUtils.isEmpty(myMonsterObject.getMonsterName())) {
                monsterName = myMonsterObject.getMonsterName();
            }

            ((TextView) findViewById(R.id.txtName)).setText(monsterName);
            String description = MonsterPreferences.getInstance(this).getMonsterDescription();

            if (!TextUtils.isEmpty(myMonsterObject.getMonsterDescription())) {
                description = myMonsterObject.getMonsterDescription();
            }

            ((TextView) findViewById(R.id.txtDescription)).setText(description);

            // set my monster image
            monsterImageView_.setMonster(myMonsterObject);

//            progressBar.setVisibility(View.GONE);
        } else {
            Log.e(TAG, "Monster is null. Unable to view monster");
        }
    }

    /**
     * Method to share my custom monster with sharing with Branch Share sheet
     */
    private void shareMyMonster() {
        progressBar.setVisibility(View.VISIBLE);

        ContentMetadata cm = createContentMetadata(myMonsterObject);
        BranchUniversalObject buo = createBranchUniversalObject(cm, myMonsterObject);
        LinkProperties linkProperties = createSmsLinkProperties();

        buo.generateShortUrl(getApplicationContext(), linkProperties, new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                if(error == null){
                    TrackerUtil.logEvent(getApplicationContext(), TrackerUtil.Event.branch_url_created);

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, String.format("Check out my Branchster named %s at %s", myMonsterObject.getMonsterName(), url));
                    startActivityForResult(i, SEND_SMS);

                    //progressBar.setVisibility(View.GONE);
                }else{
                    TrackerUtil.logEvent(getApplicationContext(), TrackerUtil.Event.branch_error);
                }
            }
        });
        progressBar.setVisibility(View.GONE);
    }

    private LinkProperties createSmsLinkProperties() {
        return new LinkProperties()
                    .setChannel("sms")
                    .setFeature("sharing");
    }

    private BranchUniversalObject createBranchUniversalObject(ContentMetadata cm, MonsterObject myMonsterObject) {
        return new BranchUniversalObject()
                .setTitle("Branchster Monster : " + myMonsterObject.getMonsterName())
                .setContentDescription("Monster created and shared by Branch's SDK")
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(cm);
    }

    private ContentMetadata createContentMetadata(MonsterObject myMonsterObject){
        ContentMetadata cm = new ContentMetadata();
        Map<String, String> params = myMonsterObject.prepareBranchDict();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            cm.addCustomMetadata(entry.getKey(), entry.getValue());
        }
        return cm;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (SEND_SMS == requestCode) {
            if (RESULT_OK == resultCode) {
                TrackerUtil.logEvent(getApplicationContext(), TrackerUtil.Event.branch_url_shared);
                Toast.makeText(getApplicationContext(), "Link shared", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create().show();
        }
    }


    @Override
    public void onFragmentInteraction() {
        //no-op
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initUI();
    }
}
