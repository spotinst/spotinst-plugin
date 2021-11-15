package hudson.plugins.spotinst.cloud;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.plugins.spotinst.api.infra.ApiResponse;
import hudson.plugins.spotinst.common.AwsInstanceTypeDynamicEnum;
import hudson.plugins.spotinst.common.AwsInstanceTypeEnum;
import hudson.plugins.spotinst.common.SpotinstContext;
import hudson.plugins.spotinst.model.aws.AwsInstanceType;
import hudson.plugins.spotinst.repos.IAwsGroupRepo;
import hudson.plugins.spotinst.repos.RepoManager;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.List;

/**
 * Created by ohadmuchnik on 18/07/2016.
 */
public class SpotinstInstanceWeight implements Describable<SpotinstInstanceWeight> {
    //region Members
    private AwsInstanceTypeEnum awsInstanceType;
    private Integer             executors;
    List<AwsInstanceType> allInstanceTypes;
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

    //region Private Methods

    @DataBoundSetter
    private void setAllInstanceTypes() {
        String token     = SpotinstContext.getInstance().getSpotinstToken();
        String accountId = SpotinstContext.getInstance().getAccountId();

        if (token != null && accountId != null) {
            IAwsGroupRepo                      awsGroupRepo             = RepoManager.getInstance().getAwsGroupRepo();
            ApiResponse<List<AwsInstanceType>> allInstanceTypesResponse = awsGroupRepo.getAllInstanceTypes(accountId);
            this.allInstanceTypes = allInstanceTypesResponse.getValue();
            AwsInstanceTypeDynamicEnum.setValues(this.allInstanceTypes);
        }
    }
    //endregion

    //region Classes
    @Extension
    public static final class DescriptorImpl extends Descriptor<SpotinstInstanceWeight> {

        @Override
        public String getDisplayName() {
            return "Spot Instance Weight";
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
    //endregion
}
