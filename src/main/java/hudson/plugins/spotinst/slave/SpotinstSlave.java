package hudson.plugins.spotinst.slave;

import hudson.Extension;
import hudson.model.*;
import hudson.model.queue.CauseOfBlockage;
import hudson.plugins.spotinst.cloud.BaseSpotinstCloud;
import hudson.plugins.spotinst.common.StatefulInstanceStateEnum;
import hudson.plugins.spotinst.model.aws.AwsStatefulInstance;
import hudson.plugins.spotinst.model.aws.AwsStatefulInstancesManager;
import hudson.plugins.spotinst.queue.StatefulInterruptedTask;
import hudson.slaves.*;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by ohadmuchnik on 23/05/2016.
 */
public class SpotinstSlave extends Slave implements EphemeralNode {

    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotinstSlave.class);

    private String            instanceId;
    private String            instanceType;
    private String            ssiId;
    private String            elastigroupId;
    private String            workspaceDir;
    private String            groupUrl;
    private SlaveUsageEnum    usage;
    private Date              createdAt;
    private BaseSpotinstCloud lastCloud;
    //endregion

    //region Constructor
    public SpotinstSlave(String name, String elastigroupId, String instanceId, String instanceType, String ssiId,
                         String label, String idleTerminationMinutes, String workspaceDir, String numOfExecutors,
                         Mode mode, ComputerLauncher launcher,
                         List<NodeProperty<?>> nodeProperties) throws Descriptor.FormException, IOException {


        super(name, "Elastigroup Id: " + elastigroupId, workspaceDir, numOfExecutors, mode, label, launcher,
              new SpotinstRetentionStrategy(idleTerminationMinutes), nodeProperties);

        this.elastigroupId = elastigroupId;
        this.instanceType = instanceType;
        this.ssiId = ssiId;
        this.instanceId = instanceId;
        this.workspaceDir = workspaceDir;
        this.usage = SlaveUsageEnum.fromMode(mode);
        this.createdAt = new Date();
        groupUrl = getSpotinstCloud().getCloudUrl();
    }
    //endregion

    //region Getters
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public String getSsiId() {
        return ssiId;
    }

    public void setSsiId(String ssiId) {
        this.ssiId = ssiId;
    }

    public String getWorkspaceDir() {
        return workspaceDir;
    }

    public Date getCreatedAt() {
        return (Date) createdAt.clone();
    }

    public String getElastigroupId() {
        return elastigroupId;
    }

    public SlaveUsageEnum getUsage() {
        return usage;
    }

    public String getGroupUrl() {
        return groupUrl;
    }


    /**
     * In some edge-cases (e.g.: user has deleted the cloud before removing its nodes) {@link Jenkins#getCloud} will
     * return null, therefore we keep a possibly-stale (yet usable) instance of the cloud as a member to remedy those
     * scenarios (for example, {@link SpotinstSlave#terminate()} which calls this method).
     */
    public BaseSpotinstCloud getSpotinstCloud() {
        BaseSpotinstCloud retVal;
        Cloud             cloud = Jenkins.get().getCloud(this.elastigroupId);

        if (cloud != null) {
            retVal = (BaseSpotinstCloud) cloud;
            lastCloud = retVal;
        }
        else {
            LOGGER.warn(String.format(
                    "could not get Cloud %s from Jenkins for SpotinstSlave %s - returning the last known cloud instance",
                    this.elastigroupId, this.instanceId));
            retVal = lastCloud;
        }

        return retVal;
    }

    public String getPrivateIp() {
        String               retVal          = null;
        String               instanceId      = getInstanceId();
        SlaveInstanceDetails instanceDetails = getSpotinstCloud().getSlaveDetails(instanceId);

        if (instanceDetails != null) {
            retVal = instanceDetails.getPrivateIp();
        }

        return retVal;
    }

    public String getPublicIp() {
        String               retVal          = null;
        String               instanceId      = getInstanceId();
        SlaveInstanceDetails instanceDetails = getSpotinstCloud().getSlaveDetails(instanceId);

        if (instanceDetails != null) {
            retVal = instanceDetails.getPublicIp();
        }

        return retVal;
    }
    //endregion

    //region Public Override Methods
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public Computer createComputer() {
        return new SpotinstComputer(this);
    }

    @Override
    public Node reconfigure(StaplerRequest req, JSONObject form) throws Descriptor.FormException {
        if (form != null) {
            String         usageStr  = form.getString("usage");
            SlaveUsageEnum usageEnum = SlaveUsageEnum.fromName(usageStr);

            if (usageEnum != null) {
                this.usage = usageEnum;
            }

            this.setMode(this.usage.toMode());

            boolean      shouldUseWebsocket = form.getBoolean("shouldUseWebsocket");
            JNLPLauncher launcher           = (JNLPLauncher) getLauncher();
            launcher.setWebSocket(shouldUseWebsocket);
        }

        return this;
    }

    @Override
    public Node asNode() {
        return this;
    }

    @Override
    public CauseOfBlockage canTake(Queue.BuildableItem item){
        CauseOfBlockage retVal                         = super.canTake(item);

        if(retVal == null) {
            LOGGER.info("can node {} take item {}", this, item);//TODO: remove
            Queue.Task task                           = item.task;
            boolean    isTaskReservedForStatefulSlave = task instanceof StatefulInterruptedTask;

            if (isTaskReservedForStatefulSlave) {
                StatefulInterruptedTask statefulInterruptedTask = (StatefulInterruptedTask) task;
                String                  statefulTaskSsi         = statefulInterruptedTask.getSsi();
                AwsStatefulInstance matchingStatefulInstance = AwsStatefulInstancesManager.getStatefulInstanceBySSi(statefulTaskSsi);
                boolean isSsiExist = matchingStatefulInstance != null;

                if (isSsiExist) {
                    StatefulInstanceStateEnum ssiState = matchingStatefulInstance.getState();

                    switch (ssiState) {
                        case PAUSE:
                        case PAUSING:
                        case PAUSED:
                        case DEALLOCATE:
                        case DEALLOCATING:
                        case DEALLOCATED:
                        case ERROR:
                            LOGGER.info("SSI {} is in state {} and isn't activated, task {} can run on any node",
                                        statefulTaskSsi, ssiState, task.getName());
                            break;

                        case ACTIVE:
                            retVal = checkMatchingSsis(statefulTaskSsi);
                            break;

                        case RECYCLE:
                        case RECYCLING:
                        case RESUME:
                        case RESUMING:
                        default:
                            LOGGER.info("task {} awaits for SSI {} to be active. no node can take it", task.getName(),
                                        statefulTaskSsi);
                            retVal = new AwaitingSsiCauseOfBlockage();
                            break;
                    }
                }
            }
        }

        return retVal;
    }

    private CauseOfBlockage checkMatchingSsis(String statefulTaskSsi) {
        CauseOfBlockage retVal          = null;
        String          slaveSsi        = getSsiId();
        boolean         isStatefulSlave = StringUtils.isNotEmpty(slaveSsi);

        if (isStatefulSlave) {
            boolean canNodeTakeTask = slaveSsi.equals(statefulTaskSsi);

            if (canNodeTakeTask) {
                LOGGER.info("node {} with ssi {} can take task", getNodeName(), slaveSsi);
            }
            else {
                LOGGER.info("task reserved for stateful slave with SSI {} different from current slave's SSI {}",
                            statefulTaskSsi, slaveSsi);
                retVal = new InconsistentSsiCauseOfBlockage();
            }
        }
        else {
            LOGGER.info("task reserved for stateful slave with SSI {}. current slave is not a stateful", statefulTaskSsi);
            retVal = new InconsistentSsiCauseOfBlockage();
        }

        return retVal;
    }
    //endregion

    //region Public Methods
    public void terminate() {
        boolean isGroupManagedByThisController = getSpotinstCloud().isCloudReadyForGroupCommunication();

        if (isGroupManagedByThisController) {
            Boolean isTerminated = getSpotinstCloud().removeInstance(instanceId);

            if (isTerminated) {
                LOGGER.info(String.format("Instance: %s terminated successfully", getInstanceId()));
                removeIfInPending();
                try {
                    Jenkins.getInstance().removeNode(this);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                LOGGER.error(String.format("Failed to terminate instance: %s", getInstanceId()));
            }
        }
        else {
            LOGGER.error("Skipped terminating slave instance {} - slave's group {} is not ready for communication.",
                         getInstanceId(), getSpotinstCloud().getGroupId());
        }
    }

    public Boolean forceTerminate() {
        Boolean retVal                         = false;
        boolean isGroupManagedByThisController = getSpotinstCloud().isCloudReadyForGroupCommunication();

        if (isGroupManagedByThisController) {
            Boolean isTerminated = getSpotinstCloud().removeInstance(instanceId);

            if (isTerminated) {
                LOGGER.info(String.format("Instance: %s terminated successfully", getInstanceId()));
                removeIfInPending();
            }
            else {
                LOGGER.error(String.format("Failed to terminate instance: %s", getInstanceId()));
            }

            try {
                Jenkins.get().removeNode(this);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            retVal = isTerminated;
        }
        else {
            LOGGER.error(
                    "Skipped force terminating slave instance {} - slave's group {} is not ready for communication.",
                    getInstanceId(), getSpotinstCloud().getGroupId());
        }

        return retVal;
    }

    private void removeIfInPending() {
        String            instanceId        = getInstanceId();
        BaseSpotinstCloud cloud             = this.getSpotinstCloud();
        Boolean           isInstancePending = cloud.isInstancePending(instanceId);

        if (isInstancePending) {
            cloud.removeInstanceFromPending(instanceId);
        }
    }

    public boolean isSlavePending() {
        boolean retVal = getSpotinstCloud().isInstancePending(getNodeName());
        return retVal;
    }

    public Boolean onSlaveConnected() {
        Boolean retVal = getSpotinstCloud().onInstanceReady(getNodeName());
        return retVal;
    }
    //endregion

    //region Extension class
    @Extension
    public static class DescriptorImpl extends SlaveDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Spot Node";
        }

        @Override
        public boolean isInstantiable() {
            return false;
        }
    }

    private static class InconsistentSsiCauseOfBlockage extends CauseOfBlockage {
        @Override
        public String getShortDescription() {
            return "task reserved for slave with ssi different from current node";
        }
    }

    private static class AwaitingSsiCauseOfBlockage extends CauseOfBlockage {
        @Override
        public String getShortDescription() {
            return "awaiting for stateful node to recover";
        }
    }
    //endregion
}
