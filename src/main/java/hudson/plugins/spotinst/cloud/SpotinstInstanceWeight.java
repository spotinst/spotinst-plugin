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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
            List<AwsInstanceType>              retVal;
            String                             accountId                = SpotinstContext.getInstance().getAccountId();
            IAwsGroupRepo                      awsGroupRepo             = RepoManager.getInstance().getAwsGroupRepo();
            ApiResponse<List<AwsInstanceType>> allInstanceTypesResponse = awsGroupRepo.getAllInstanceTypes(accountId);
            Boolean                            isRequestSucceed         = allInstanceTypesResponse.isRequestSucceed();

            if (isRequestSucceed) {
                retVal = allInstanceTypesResponse.getValue();
                //Log the new types
                List<String> newTypesFromAPI = calcInstanceTypesFromList(retVal,true);
                String massage = "There are %d new instance types loaded using API call, They are not in the constant Enum list: \n%s";
                String massageWithList = String.format(massage, newTypesFromAPI.size(), newTypesFromAPI);
                LOGGER.info(massageWithList);
            }
            else {
                retVal = getConstantInstanceTypesList();
                //Log the constant types
                List<String> constantInstanceTypes = calcInstanceTypesFromList(retVal,false);
                String massage = "Loading all instance types with an API call failed, using %d constant instance types: \n%s";
                String massageWithList = String.format(massage, constantInstanceTypes.size(), constantInstanceTypes);
                LOGGER.info(massageWithList);
            }

            return retVal;
        }

        private List<String> calcInstanceTypesFromList(List<AwsInstanceType> instanceTypesFromAPI, boolean isListFromAPI) {
            List<String> retVal = new ArrayList<>();

            for (AwsInstanceType instanceType : instanceTypesFromAPI) {
                boolean isInstanceInConstantEnum = false;
                for (AwsInstanceTypeEnum instanceTypeNum : AwsInstanceTypeEnum.values()) {
                    if (instanceTypeNum.getValue().equals(instanceType.getInstanceType())) {
                        isInstanceInConstantEnum = true;
                        break;
                    }
                }

                if (isInstanceInConstantEnum == false && isListFromAPI == true) {
                    retVal.add(instanceType.getInstanceType());
                }
                else if(isInstanceInConstantEnum == true && isListFromAPI == false){
                    retVal.add(instanceType.getInstanceType());
                }
            }

            return retVal;
        }

        private List<AwsInstanceType> getConstantInstanceTypesList() {
            List<AwsInstanceType> retVal = new ArrayList<>();

            for (AwsInstanceTypeEnum instanceTypeEnum : AwsInstanceTypeEnum.values()) {
                String          type         = instanceTypeEnum.getValue();
                Integer         cpus         = instanceTypeEnum.getExecutors();
                AwsInstanceType instanceType = new AwsInstanceType();
                instanceType.setInstanceType(type);
                instanceType.setCpus(cpus);
                retVal.add(instanceType);
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
