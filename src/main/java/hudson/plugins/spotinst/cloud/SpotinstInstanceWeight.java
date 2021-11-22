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
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ohadmuchnik on 18/07/2016.
 */
public class SpotinstInstanceWeight implements Describable<SpotinstInstanceWeight> {
    //region Members
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
        public DescriptorImpl() {
        }

        @Override
        public String getDisplayName() {
            return "Spot Instance Weight";
        }

        public ListBoxModel doFillAwsInstanceTypeFromAPIItems() {
            ListBoxModel          retVal           = new ListBoxModel();
            List<AwsInstanceType> allInstanceTypes = loadAllInstanceTypes();

            if (allInstanceTypes != null) {
                for (AwsInstanceType instanceType : allInstanceTypes) {
                    retVal.add(instanceType.getInstanceType());
                }
            }

            return retVal;
        }

        private List<AwsInstanceType> loadAllInstanceTypes() {
            List<AwsInstanceType>              retVal                   = new ArrayList<>();
            String                             accountId                = SpotinstContext.getInstance().getAccountId();
            IAwsGroupRepo                      awsGroupRepo             = RepoManager.getInstance().getAwsGroupRepo();
            ApiResponse<List<AwsInstanceType>> allInstanceTypesResponse = awsGroupRepo.getAllInstanceTypes(accountId);
            Boolean                            isRequestSucceed         = allInstanceTypesResponse.isRequestSucceed();

            if (isRequestSucceed) {
                retVal = allInstanceTypesResponse.getValue();
            }
            else {
                for (AwsInstanceTypeEnum instanceTypeEnum : AwsInstanceTypeEnum.values()) {
                    String          type         = instanceTypeEnum.getValue();
                    Integer         cpus         = instanceTypeEnum.getExecutors();
                    AwsInstanceType instanceType = new AwsInstanceType();
                    instanceType.setInstanceType(type);
                    instanceType.setCpus(cpus);
                    retVal.add(instanceType);
                }
            }

            return retVal;
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
        this.awsInstanceType = null;
    }

    public String getAwsInstanceTypeFromAPI(){
        String retVal;
        if(this.awsInstanceType != null){
            retVal = awsInstanceType.getValue();
        }else{
            retVal = awsInstanceTypeFromAPI;
        }

        return retVal;
    }
    //endregion
}
