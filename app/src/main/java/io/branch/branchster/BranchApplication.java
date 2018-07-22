package io.branch.branchster;

import io.branch.branchster.util.TrackerUtil;
import io.branch.referral.Branch;

public class BranchApplication extends io.branch.referral.BranchApp {
    @Override
    public void onCreate() {
        super.onCreate();
        TrackerUtil.logAppStarted(getApplicationContext());

        Branch.enableLogging();
        Branch.getAutoInstance(this);
    }
}
