package hudson.plugins.spotinst.cloud;

import hudson.Extension;
import hudson.plugins.spotinst.common.SpotinstContext;
import hudson.plugins.spotinst.jobs.SpotLoadAwsInstanceTypes;
import hudson.plugins.spotinst.model.aws.AwsInstanceType;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Extension
public class spotAWSInstanceTypesConfig extends GlobalConfiguration {

    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(spotAWSInstanceTypesConfig.class);
    //endregion

    public spotAWSInstanceTypesConfig() {
        load();
        List<AwsInstanceType> awsInstanceTypes = SpotLoadAwsInstanceTypes.loadAllInstanceTypes();
        SpotinstContext.getInstance().setAwsInstanceTypes(awsInstanceTypes);
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) {
        List<AwsInstanceType> awsInstanceTypes = SpotLoadAwsInstanceTypes.loadAllInstanceTypes();
        SpotinstContext.getInstance().setAwsInstanceTypes(awsInstanceTypes);
        save();

        return true;
    }
}
