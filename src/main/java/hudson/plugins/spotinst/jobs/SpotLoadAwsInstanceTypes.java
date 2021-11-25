package hudson.plugins.spotinst.jobs;

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import hudson.plugins.spotinst.cloud.AwsSpotinstCloud;
import hudson.plugins.spotinst.cloud.SpotAWSInstanceTypesConfig;
import hudson.slaves.Cloud;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Extension
public class SpotLoadAwsInstanceTypes extends AsyncPeriodicWork {

    //region Members
    private static final Logger  LOGGER                  = LoggerFactory.getLogger(SpotLoadAwsInstanceTypes.class);
    public static final  Integer JOB_INTERVAL_IN_MINUTES = 720;
    final                long    recurrencePeriod;
    //endregion

    //region Constructor
    public SpotLoadAwsInstanceTypes() {
        super("Load AWS Instance Types");
        recurrencePeriod = TimeUnit.MINUTES.toMillis(JOB_INTERVAL_IN_MINUTES);
    }
    //endregion

    //region Public Methods
    @Override
    protected void execute(TaskListener taskListener) {
        List<Cloud> cloudList       = Jenkins.getInstance().clouds;
        boolean     isAwsCloudExist = false;

        if (cloudList != null && cloudList.size() > 0) {
            for (Cloud cloud : cloudList) {
                if (cloud instanceof AwsSpotinstCloud) {
                    isAwsCloudExist = true;
                }
            }
        }

        if (isAwsCloudExist == true) {
            SpotAWSInstanceTypesConfig.loadAllInstanceTypes();
        }
        else {
            LOGGER.info("There are no AWS clouds to load instance types into");
        }
    }

    @Override
    public long getRecurrencePeriod() {
        return recurrencePeriod;
    }
    //endregion
}
