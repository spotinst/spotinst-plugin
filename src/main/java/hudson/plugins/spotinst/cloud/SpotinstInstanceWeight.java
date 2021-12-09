package hudson.plugins.spotinst.cloud;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.plugins.spotinst.common.AwsInstanceTypeEnum;
import hudson.plugins.spotinst.common.SpotAwsInstanceTypesHelper;
import hudson.plugins.spotinst.common.SpotinstContext;
import hudson.plugins.spotinst.model.aws.AwsInstanceType;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.List;

import static hudson.plugins.spotinst.api.SpotinstApi.validateToken;

/**
 * Created by ohadmuchnik on 18/07/2016.
 */
public class SpotinstInstanceWeight implements Describable<SpotinstInstanceWeight> {
    //region Members
    private Integer             executors;
    //TODO improve naming
    private String              awsInstanceTypeFromAPI;
    //Deprecated
    private AwsInstanceTypeEnum awsInstanceType;
    //endregion

    //region Constructors
    @DataBoundConstructor
    public SpotinstInstanceWeight(AwsInstanceTypeEnum awsInstanceType, Integer executors) {
        this.awsInstanceType = awsInstanceType;
        this.executors = executors;
    }
    //endregion

    //region Overrides
    @Override
    public Descriptor<SpotinstInstanceWeight> getDescriptor() {
        Descriptor<SpotinstInstanceWeight> retVal = Jenkins.getInstance().getDescriptor(getClass());

        if (retVal == null) {
            throw new RuntimeException("Descriptor of type SpotinstInstanceWeight cannot be null");
        }

        return retVal;
    }
    //endregion

    //region Classes
    @Extension
    public static final class DescriptorImpl extends Descriptor<SpotinstInstanceWeight> {

        @Override
        public String getDisplayName() {
            return "Spot Instance Weight";
        }

        public ListBoxModel doFillAwsInstanceTypeFromAPIItems() {
            ListBoxModel          retVal           = new ListBoxModel();
            List<AwsInstanceType> allInstanceTypes = SpotAwsInstanceTypesHelper.loadAllInstanceTypes();

            if (allInstanceTypes != null) {
                for (AwsInstanceType instanceType : allInstanceTypes) {
                    retVal.add(instanceType.getInstanceType());
                }
            }

            return retVal;
        }

        public FormValidation doCheckAwsInstanceTypeFromAPI() {
            String  accountId                 = SpotinstContext.getInstance().getAccountId();
            String  token                     = SpotinstContext.getInstance().getSpotinstToken();
            int     isValid                   = validateToken(token, accountId);
            //TODO - check with Ziv why needed here (if not call here, error message display)
            SpotAwsInstanceTypesHelper.loadAllInstanceTypes();
            Boolean isInstanceTypesListUpdate = SpotAwsInstanceTypesHelper.isInstanceTypesListUpdate();

            FormValidation result;
            if (isValid != 0 || isInstanceTypesListUpdate == false) {
                result = FormValidation.error(
                        "Usage of this configuration might not work as expected. In order to get the up-to-date instance types please check the Spot token on the “Configure System” page.");
            }
            else {
                result = FormValidation.okWithMarkup(
                        "<div style=\"color:green\">instance types list is up-to-date</div>");
            }

            return result;
        }
    }
    //endregion

    //region Getters / Setters
    public Integer getExecutors() {
        return executors;
    }

    public AwsInstanceTypeEnum getAwsInstanceType() {
        return awsInstanceType;
    }

    @DataBoundSetter
    public void setAwsInstanceTypeFromAPI(String awsInstanceTypeFromAPI) {
        this.awsInstanceTypeFromAPI = awsInstanceTypeFromAPI;
    }

    public String getAwsInstanceTypeFromAPI() {
        //TODO - CR
        String retVal;
        String accountId = SpotinstContext.getInstance().getAccountId();
        String token     = SpotinstContext.getInstance().getSpotinstToken();
        int    isValid   = validateToken(token, accountId);

        if (this.awsInstanceTypeFromAPI != null) {

            /*
            If the user Previously chosen was a type that not exist in the hard coded list
             and did not configure the token right, we will present the chosen type and set the default vCPU to 1
             The descriptor of this class will show a warning message will note the user that something is wrong,
             and point to authentication fix before saving this configuration.
             */
            if (AwsInstanceTypeEnum.fromValue(this.awsInstanceTypeFromAPI) == null && isValid != 0) {
                AwsInstanceType instanceType = new AwsInstanceType();
                instanceType.setInstanceType(awsInstanceTypeFromAPI);
                instanceType.setvCPU(1);
                SpotinstContext.getInstance().getAwsInstanceTypes().add(instanceType);
            }

            retVal = awsInstanceTypeFromAPI;

        }
        else {
            retVal = awsInstanceType.getValue();
        }

        return retVal;
    }
    //endregion
}
