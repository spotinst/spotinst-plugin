package hudson.plugins.spotinst.cloud;

import hudson.Extension;
import hudson.model.Node;
import hudson.plugins.spotinst.api.infra.ApiResponse;
import hudson.plugins.spotinst.api.infra.JsonMapper;
import hudson.plugins.spotinst.model.azure.AzureV3GroupVm;
import hudson.plugins.spotinst.model.azure.AzureV3VmSizeEnum;
import hudson.plugins.spotinst.repos.IAzureV3GroupRepo;
import hudson.plugins.spotinst.repos.RepoManager;
import hudson.plugins.spotinst.slave.SlaveInstanceDetails;
import hudson.plugins.spotinst.slave.SlaveUsageEnum;
import hudson.plugins.spotinst.slave.SpotinstSlave;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.tools.ToolLocationNodeProperty;
import jenkins.model.Jenkins;
import org.apache.commons.lang.BooleanUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

//TODO shibel:
// - check pending executors function - why is Azure treated as 1 executors?
// - check remove from pending function - why is only Azure overridden here?
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

        IAzureV3GroupRepo azureV3GroupRepo = RepoManager.getInstance().getAzureV3GroupRepo();
        ApiResponse<Boolean> scaleUpResponse =
                azureV3GroupRepo.scaleUp(groupId, request.getExecutors(), this.accountId);

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
        Boolean              retVal           = false;
        IAzureV3GroupRepo    azureV3GroupRepo = RepoManager.getInstance().getAzureV3GroupRepo();
        ApiResponse<Boolean> detachVmResponse = azureV3GroupRepo.detachVM(groupId, instanceId, this.accountId);

        if (detachVmResponse.isRequestSucceed()) {
            LOGGER.info(String.format("Instance %s detached", instanceId));
            retVal = true;
        }
        else {
            LOGGER.error(String.format("Failed to detach instance %s. Errors: %s", instanceId,
                                       detachVmResponse.getErrors()));
        }

        return retVal;
    }

    //TODO shibel: take care of this
    @Override
    public String getCloudUrl() {
        return null;
    }

    @Override
    public void syncGroupInstances() {
        IAzureV3GroupRepo                 azureV3GroupRepo  = RepoManager.getInstance().getAzureV3GroupRepo();
        ApiResponse<List<AzureV3GroupVm>> instancesResponse = azureV3GroupRepo.getGroupVms(groupId, this.accountId);

        if (instancesResponse.isRequestSucceed()) {
            List<AzureV3GroupVm> vms = instancesResponse.getValue();

            LOGGER.info(String.format("There are %s instances in group %s", vms.size(), groupId));

            addNewSlaveInstances(vms);
            removeOldSlaveInstances(vms);

            Map<String, SlaveInstanceDetails> slaveInstancesDetailsByInstanceId = new HashMap<>();

            for (AzureV3GroupVm vm : vms) {
                SlaveInstanceDetails instanceDetails = SlaveInstanceDetails.build(vm);
                slaveInstancesDetailsByInstanceId.put(instanceDetails.getInstanceId(), instanceDetails);
            }

            this.slaveInstancesDetailsByInstanceId = new HashMap<>(slaveInstancesDetailsByInstanceId);
        }
        else {
            LOGGER.error(String.format("Failed to get group %s instances. Errors: %s", groupId,
                                       instancesResponse.getErrors()));
        }
    }
    //endregion

    //region Private Methods
    private void removeOldSlaveInstances(List<AzureV3GroupVm> azureV3GroupVms) {
        List<SpotinstSlave> allGroupsSlaves = getAllSpotinstSlaves();

        if (allGroupsSlaves.size() > 0) {
            List<String> groupVmIds = azureV3GroupVms.stream().filter(x -> x.getVmName() != null)
                                                     .map(AzureV3GroupVm::getVmName)
                                                     .collect(Collectors.toList());

            for (SpotinstSlave slave : allGroupsSlaves) {
                String slaveInstanceId = slave.getInstanceId();

                Boolean slaveIdNotNull = slaveInstanceId != null;
                Boolean slaveIdNotInGroupVms = BooleanUtils.isFalse(groupVmIds.contains(slaveInstanceId));

                if (slaveIdNotNull && slaveIdNotInGroupVms) {
                    LOGGER.info(String.format("Slave for instance: %s is no longer running in group: %s, removing it",
                                              slaveInstanceId, groupId));
                    try {
                        Jenkins.get().removeNode(slave);
                        LOGGER.info(String.format("Slave: %s removed successfully", slaveInstanceId));
                    }
                    catch (IOException e) {
                        LOGGER.error(String.format("Failed to remove slave from group: %s", groupId), e);
                    }
                }
            }
        }
        else {
            LOGGER.info(String.format("There are no slaves for group: %s", groupId));
        }
    }

    private void addNewSlaveInstances(List<AzureV3GroupVm> azureV3GroupVms) {
        if (azureV3GroupVms.size() > 0) {
            for (AzureV3GroupVm vm : azureV3GroupVms) {
                Boolean doesSlaveNotExist = BooleanUtils.isFalse(isSlaveExistForInstance(vm));

                if (doesSlaveNotExist) {
                    LOGGER.info(String.format("Instance: %s of group: %s doesn't have slave , adding new one",
                                              JsonMapper.toJson(vm), groupId));
                    addSpotinstSlave(vm);
                }
            }
        }
        else {
            LOGGER.info(String.format("There are no new instances to add for group: %s", groupId));
        }
    }

    private void addSpotinstSlave(AzureV3GroupVm vm) {
        SpotinstSlave slave = null;

        if (vm.getVmName() != null) {
            Integer executors = getNumOfExecutors(vm.getVmSize());
            slave = buildSpotinstSlave(vm.getVmName(), vm.getVmSize(), String.valueOf(executors));
        }

        if (slave != null) {
            try {
                Jenkins.get().addNode(slave);
            }
            catch (IOException e) {
                LOGGER.error(String.format("Failed to create node for slave: %s", slave.getInstanceId()), e);
            }
        }
    }

    private Integer getNumOfExecutors(String vmSize) {
        LOGGER.info(String.format("Determining the # of executors for instance type: %s", vmSize));

        Integer           retVal     = 1;
        AzureV3VmSizeEnum vmSizeEnum = AzureV3VmSizeEnum.fromValue(vmSize);

        if (vmSizeEnum != null) {
            retVal = vmSizeEnum.getExecutors();
        }

        return retVal;
    }

    private Boolean isSlaveExistForInstance(AzureV3GroupVm vm) {
        Boolean retVal = false;
        // In Azure V3 vmName is its ID
        Node node = Jenkins.get().getNode(vm.getVmName());

        if (node != null) {
            retVal = true;
        }

        return retVal;
    }

    private void addToGroupPending(ProvisionRequest request) {
        for (int i = 0; i < request.getExecutors(); i++) {
            String          key             = UUID.randomUUID().toString();
            PendingInstance pendingInstance = new PendingInstance();
            pendingInstance.setCreatedAt(new Date());
            pendingInstance.setNumOfExecutors(1);
            pendingInstance.setRequestedLabel(request.getLabel());
            pendingInstance.setStatus(PendingInstance.StatusEnum.PENDING);

            pendingInstances.put(key, pendingInstance);
        }
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
