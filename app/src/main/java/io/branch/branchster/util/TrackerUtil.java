package io.branch.branchster.util;

import android.content.Context;

import java.util.Map;

import io.branch.referral.util.BranchEvent;

public class TrackerUtil {
    private TrackerUtil(){
        //Do nothing
    }

    public enum Event{
        app_started,

        //branch events
        branch_init,
        branch_error,
        branch_url_created,
        branch_url_shared,

        //monster events
        monster_edit,
        monster_view
    }
    public static void logAppStarted(Context context){
        logEvent(context, Event.app_started);
    }
    public static void logBranchInit(Context context){
        logEvent(context, Event.branch_init);
    }

    public static void logMonsterEdit(Context context){
        logEvent(context, Event.monster_edit);
    }

    public static void logMonsterView(Context context, Map<String, String> monsterMetadata){
        logEvent(context, Event.monster_view, monsterMetadata);
    }

    public static void logEvent(Context context, Event event){
        BranchEvent branchEvent = new BranchEvent(event.toString());
        branchEvent.logEvent(context);
    }

    public static void logEvent(Context context, Event event, Map<String, String> monsterMetadata){
        BranchEvent branchEvent = new BranchEvent(event.toString());

        if(null == monsterMetadata){
            branchEvent.logEvent(context);
        }else{
            for (Map.Entry<String, String> entry : monsterMetadata.entrySet()) {
                branchEvent.addCustomDataProperty(entry.getKey(), entry.getValue());
            }
            branchEvent.logEvent(context);
        }
    }


}
