package hudson.plugins.spotinst.cloud;

import hudson.Extension;
import hudson.plugins.spotinst.api.infra.ApiResponse;
import hudson.plugins.spotinst.common.AwsInstanceTypeEnum;
import hudson.plugins.spotinst.common.SpotinstContext;
import hudson.plugins.spotinst.model.aws.AwsInstanceType;
import hudson.plugins.spotinst.repos.IAwsGroupRepo;
import hudson.plugins.spotinst.repos.RepoManager;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Extension
public class SpotAWSInstanceTypesConfig extends GlobalConfiguration {

    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotAWSInstanceTypesConfig.class);
    //endregion

    public SpotAWSInstanceTypesConfig() {
        load();
        loadAllInstanceTypes();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) {
        loadAllInstanceTypes();
        save();
        return true;
    }

    public static void loadAllInstanceTypes() {
        List<AwsInstanceType>              retVal;
        String                             accountId                = SpotinstContext.getInstance().getAccountId();
        IAwsGroupRepo                      awsGroupRepo             = RepoManager.getInstance().getAwsGroupRepo();
        ApiResponse<List<AwsInstanceType>> allInstanceTypesResponse = awsGroupRepo.getAllInstanceTypes(accountId);
        Boolean                            isRequestSucceed         = allInstanceTypesResponse.isRequestSucceed();

        if (isRequestSucceed) {
            retVal = allInstanceTypesResponse.getValue();
            //Log the new types
            List<String> newTypesFromAPI = calcInstanceTypesFromList(retVal, true);
            String massage =
                    "There are %d new instance types loaded using API call, They are not in the constant Enum list: \n%s";
            String massageWithList = String.format(massage, newTypesFromAPI.size(), newTypesFromAPI);
            LOGGER.info(massageWithList);
        }
        else {
            retVal = getConstantInstanceTypesList();
            //Log the constant types
            List<String> constantInstanceTypes = calcInstanceTypesFromList(retVal, false);
            String massage =
                    "Loading all instance types with an API call failed, using %d constant instance types: \n%s";
            String massageWithList = String.format(massage, constantInstanceTypes.size(), constantInstanceTypes);
            LOGGER.info(massageWithList);
        }

        SpotinstContext.getInstance().setAwsInstanceTypes(retVal);
    }


    private static List<String> calcInstanceTypesFromList(List<AwsInstanceType> instanceTypesFromAPI,
                                                          boolean isListFromAPI) {
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
            else if (isInstanceInConstantEnum == true && isListFromAPI == false) {
                retVal.add(instanceType.getInstanceType());
            }
        }

        return retVal;
    }

    private static List<AwsInstanceType> getConstantInstanceTypesList() {
        List<AwsInstanceType> retVal = new ArrayList<>();

        for (AwsInstanceTypeEnum instanceTypeEnum : AwsInstanceTypeEnum.values()) {
            String          type         = instanceTypeEnum.getValue();
            Integer         cpus         = instanceTypeEnum.getExecutors();
            AwsInstanceType instanceType = new AwsInstanceType();
            instanceType.setInstanceType(type);
            instanceType.setvCPU(cpus);
            retVal.add(instanceType);
        }

        return retVal;
    }
}
