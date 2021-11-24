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

import java.util.ArrayList;
import java.util.List;

import static hudson.plugins.spotinst.api.SpotinstApi.validateToken;

/**
 * Created by ohadmuchnik on 18/07/2016.
 */
public class SpotinstInstanceWeight implements Describable<SpotinstInstanceWeight> {
    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotinstInstanceWeight.class);
    private AwsInstanceTypeEnum awsInstanceType;
    private Integer             executors;
    private String              awsInstanceTypeFromAPI;
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
            }

            return retVal;
        }

        public FormValidation doCheckAwsInstanceTypeFromAPI() {
            String                accountId        = SpotinstContext.getInstance().getAccountId();
            String                token            = SpotinstContext.getInstance().getSpotinstToken();
            List<AwsInstanceType> awsInstanceTypes = SpotinstContext.getInstance().getAwsInstanceTypes();
            int                   isValid          = validateToken(token, accountId);

            FormValidation result;
            //TODO improve logs logic and messages
            if (isValid != 0 && awsInstanceTypes == null || awsInstanceTypes.isEmpty() == false) {
                result = FormValidation.error(
                        "Invalid Spot tokenֿ\n In order to get the up-to-date instance types list please update Spot token in Configure System page");
            }
            else {
                result = FormValidation.warning("Failed to process credentials validation");

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

    public String getAwsInstanceTypeFromAPI(){
        String retVal;

        if(this.awsInstanceTypeFromAPI != null){
            retVal = awsInstanceTypeFromAPI;
        }else{
            retVal = awsInstanceType.getValue();
        }

        return retVal;
    }
    //endregion
}
