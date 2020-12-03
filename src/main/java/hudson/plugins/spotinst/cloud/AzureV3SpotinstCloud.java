package hudson.plugins.spotinst.cloud;

import hudson.Extension;
import hudson.plugins.spotinst.api.infra.ApiResponse;
import hudson.plugins.spotinst.repos.IAzureV3GroupRepo;
import hudson.plugins.spotinst.repos.RepoManager;
import hudson.plugins.spotinst.slave.SlaveUsageEnum;
import hudson.plugins.spotinst.slave.SpotinstSlave;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.tools.ToolLocationNodeProperty;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class AzureV3SpotinstCloud extends BaseSpotinstCloud {
    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureV3SpotinstCloud.class);
    //endregion


    //region Constructor
    @DataBoundConstructor
    public AzureV3SpotinstCloud(String groupId, String labelString, String idleTerminationMinutes, String workspaceDir,
                                SlaveUsageEnum usage, String tunnel, Boolean shouldUseWebsocket,
                                Boolean shouldRetriggerBuilds, String vmargs,
                                EnvironmentVariablesNodeProperty environmentVariables,
                                ToolLocationNodeProperty toolLocations, String accountId) {
        super(groupId, labelString, idleTerminationMinutes, workspaceDir, usage, tunnel, shouldUseWebsocket,
              shouldRetriggerBuilds, vmargs, environmentVariables, toolLocations, accountId);
    }
    //endregion

    // region Override Methods
    @Override
    List<SpotinstSlave> scaleUp(ProvisionRequest request) {
        List<SpotinstSlave> retVal = new LinkedList<>();

        IAzureV3GroupRepo    azureV3GroupRepo = RepoManager.getInstance().getAzureV3GroupRepo();
        ApiResponse<Boolean> scaleUpResponse  = azureV3GroupRepo.scaleUp(groupId, request.getExecutors(), this.accountId);

        if (scaleUpResponse.isRequestSucceed()) {
            LOGGER.info(String.format("Scale up group %s succeeded", groupId));
            addToGroupPending(request);
        }
        else {
            LOGGER.error(
                    String.format("Failed to scale up group: %s. Errors: %s", groupId, scaleUpResponse.getErrors()));
        }

        return retVal;
    }

    @Override
    public Boolean detachInstance(String instanceId) {
        return null;
    }

    @Override
    public String getCloudUrl() {
        return null;
    }

    @Override
    public void syncGroupInstances() {

    }
    //endregion

    //region Classes
    @Extension
    public static class DescriptorImpl extends BaseSpotinstCloud.DescriptorImpl {

        @Override
        public String getDisplayName() {
            return "Spotinst Azure Elastigroup";
        }
    }
    //endregion

}
