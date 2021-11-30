package hudson.plugins.spotinst.cloud;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.plugins.spotinst.common.AwsInstanceTypeEnum;
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
    private AwsInstanceTypeEnum awsInstanceType;
    private Integer             executors;
    private String              awsInstanceTypeFromAPI;
    //    public static final String              TYPE_DOES_NOT_EXIST_IN_CONSTANT_ENUM =
    //            "Previously chosen type does not exist";
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
            List<AwsInstanceType> allInstanceTypes = SpotinstContext.getInstance().getAwsInstanceTypes();

            if (allInstanceTypes != null) {
                for (AwsInstanceType instanceType : allInstanceTypes) {
                    retVal.add(instanceType.getInstanceType());
                }
                //                if (retVal.size() == AwsInstanceTypeEnum.values().length) {
                //                    //Edge case - add list item when fallback to constant enum not contains new types that were chosen by the user
                //                    retVal.add(TYPE_DOES_NOT_EXIST_IN_CONSTANT_ENUM);
                //                }
            }

            return retVal;
        }

        public FormValidation doCheckAwsInstanceTypeFromAPI() {
            String accountId = SpotinstContext.getInstance().getAccountId();
            String token     = SpotinstContext.getInstance().getSpotinstToken();
            int    isValid   = validateToken(token, accountId);

            FormValidation result;
            if (isValid != 0) {
                result = FormValidation.error(
                        "Invalid Spot token!\nUsage of this configuration might not work as expected.\nIn order to get the up-to-date instance types data please update Spot token in Configure System page.");
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
        // if (awsInstanceTypeFromAPI.equals(TYPE_DOES_NOT_EXIST_IN_CONSTANT_ENUM) == false) {
        this.awsInstanceTypeFromAPI = awsInstanceTypeFromAPI;
        // }
    }

    public String getAwsInstanceTypeFromAPI() {
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
