package hudson.plugins.spotinst.cloud;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.plugins.spotinst.api.infra.ApiResponse;
import hudson.plugins.spotinst.common.AwsInstanceTypeEnum;
import hudson.plugins.spotinst.common.SpotinstContext;
import hudson.plugins.spotinst.model.aws.AwsInstanceType;
import hudson.plugins.spotinst.repos.IAwsGroupRepo;
import hudson.plugins.spotinst.repos.RepoManager;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import static hudson.plugins.spotinst.api.SpotinstApi.validateToken;

/**
 * Created by ohadmuchnik on 18/07/2016.
 */
public class SpotinstInstanceWeight implements Describable<SpotinstInstanceWeight> {
    //region Members
    private static final Logger              LOGGER                               =
            LoggerFactory.getLogger(SpotinstInstanceWeight.class);
    public static final  String              TYPE_DOES_NOT_EXIST_IN_CONSTANT_ENUM =
            "Previously chosen type does not exist";
    private              AwsInstanceTypeEnum awsInstanceType;
    private              Integer             executors;
    private              String              awsInstanceTypeFromAPI;
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
                if (retVal.size() == AwsInstanceTypeEnum.values().length) {
                    //Edge case - add list item when fallback to constant enum not contains new types that were chosen by the user
                    retVal.add(TYPE_DOES_NOT_EXIST_IN_CONSTANT_ENUM);
                }
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
                        "Invalid Spot token. In order to get the up-to-date instance types list please update Spot token in Configure System page");
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
        if(awsInstanceTypeFromAPI.equals(TYPE_DOES_NOT_EXIST_IN_CONSTANT_ENUM) == false){
            this.awsInstanceTypeFromAPI = awsInstanceTypeFromAPI;
        }
    }

    public String getAwsInstanceTypeFromAPI() {
        String retVal;
        String accountId = SpotinstContext.getInstance().getAccountId();
        String token     = SpotinstContext.getInstance().getSpotinstToken();
        int    isValid   = validateToken(token, accountId);

        if (this.awsInstanceTypeFromAPI != null) {
            if (AwsInstanceTypeEnum.fromValue(this.awsInstanceTypeFromAPI) == null && isValid != 0) {
                retVal = TYPE_DOES_NOT_EXIST_IN_CONSTANT_ENUM;
            }
            else {
                retVal = awsInstanceTypeFromAPI;
            }
        }
        else {
            retVal = awsInstanceType.getValue();
        }

        return retVal;
    }
    //endregion
}
