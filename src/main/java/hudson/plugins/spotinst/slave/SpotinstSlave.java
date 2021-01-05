package hudson.plugins.spotinst.slave;

import hudson.Extension;
import hudson.model.*;
import hudson.plugins.spotinst.cloud.BaseSpotinstCloud;
import hudson.plugins.spotinst.common.ConnectionMethodEnum;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.JNLPLauncher;
import hudson.slaves.NodeProperty;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by ohadmuchnik on 23/05/2016.
 */
public class SpotinstSlave extends Slave {

    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotinstSlave.class);

    private String            instanceId;
    private String            instanceType;
    private String            elastigroupId;
    private String            workspaceDir;
    private String            groupUrl;
    private SlaveUsageEnum    usage;
    private Date              createdAt;
    private BaseSpotinstCloud spotinstCloud;
    //endregion

    //region Constructor
    //todo shibel - there are some unused params here
    public SpotinstSlave(BaseSpotinstCloud spotinstCloud, String name, String elastigroupId, String instanceId,
                         String instanceType, String label, String idleTerminationMinutes, String workspaceDir,
                         String numOfExecutors, Mode mode, String tunnel, Boolean shouldUseWebsocket, String vmargs,
                         List<NodeProperty<?>> nodeProperties,
                         Boolean shouldRetriggerBuilds) throws Descriptor.FormException, IOException {


        //todo sibel - shy using deprecated constructor?
        super(name, "Elastigroup Id: " + elastigroupId, workspaceDir, numOfExecutors, mode, label, null,
              new SpotinstRetentionStrategy(idleTerminationMinutes), nodeProperties);

        this.spotinstCloud = spotinstCloud;
        SlaveInstanceDetails instanceDetailsById = spotinstCloud.getSlaveDetails(this.name);
        ComputerLauncher     launcher            = buildLauncher(instanceDetailsById);

        this.setLauncher(launcher);
        this.elastigroupId = elastigroupId;
        this.instanceType = instanceType;
        this.instanceId = instanceId;
        this.workspaceDir = workspaceDir;
        this.usage = SlaveUsageEnum.fromMode(mode);
        this.createdAt = new Date();
        groupUrl = spotinstCloud.getCloudUrl();
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

    public BaseSpotinstCloud getSpotinstCloud() {
        return spotinstCloud;
    }

    public String getPrivateIp() {
        String               retVal          = null;
        String               instanceId      = getInstanceId();
        SlaveInstanceDetails instanceDetails = spotinstCloud.getSlaveDetails(instanceId);

        if (instanceDetails != null) {
            retVal = instanceDetails.getPrivateIp();
        }

        return retVal;
    }

    public String getPublicIp() {
        String               retVal          = null;
        String               instanceId      = getInstanceId();
        SlaveInstanceDetails instanceDetails = spotinstCloud.getSlaveDetails(instanceId);

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        SpotinstSlave that = (SpotinstSlave) o;
        return Objects.equals(instanceId, that.instanceId) && Objects.equals(instanceType, that.instanceType) &&
               Objects.equals(elastigroupId, that.elastigroupId) && Objects.equals(workspaceDir, that.workspaceDir) &&
               Objects.equals(groupUrl, that.groupUrl) && usage == that.usage &&
               Objects.equals(createdAt, that.createdAt) && Objects.equals(spotinstCloud, that.spotinstCloud);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instanceId, instanceType, elastigroupId, workspaceDir, groupUrl, usage,
                            createdAt, spotinstCloud);
    }
    //endregion

    //region Public Methods
    public void terminate() {
        Boolean isTerminated = spotinstCloud.detachInstance(instanceId);

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

    public Boolean forceTerminate() {
        Boolean isTerminated = spotinstCloud.detachInstance(instanceId);

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

        return isTerminated;
    }

    //todo shibel - the method name is remove bit you call here 'onInstanceReady' maybe iyt has the same logic but its not clear
    private void removeIfInPending() {
        if (this.getSpotinstCloud().isInstancePending(getInstanceId())) {
            // all that onInstanceReady does is remove from pending instances
            this.getSpotinstCloud().onInstanceReady(getInstanceId());
            LOGGER.info(
                    String.format("Instance: %s removed from pending instances after termination", getInstanceId()));
        }
    }

    public boolean isSlavePending() {
        boolean retVal = this.spotinstCloud.isInstancePending(getNodeName());
        return retVal;
    }

    public void onSlaveConnected() {
        this.spotinstCloud.onInstanceReady(getNodeName());
    }
    //endregion

    //region Private Methods
    //todo shibel - wht this logic is here? it should be in the Cloud that knoes if he is SSH or JNLP
    private ComputerLauncher buildLauncher(SlaveInstanceDetails instanceDetailsById) throws IOException {
        ComputerLauncher retVal;
        Boolean isSshCloud = spotinstCloud.getConnectionMethod().equals(ConnectionMethodEnum.SSH_OR_COMMAND);

        if (isSshCloud) {
            retVal = HandleSSHLauncher(instanceDetailsById);
        }
        else {
            retVal = handleJNLPLauncher();
        }

        return retVal;
    }

    private ComputerLauncher handleJNLPLauncher() {
        //todo shibel - can be in one line
        ComputerLauncher launcher;
        launcher = new SpotinstComputerLauncher(this.spotinstCloud.getTunnel(), this.spotinstCloud.getVmargs(),
                                                this.spotinstCloud.getShouldUseWebsocket(),
                                                this.spotinstCloud.getShouldRetriggerBuilds());
        return launcher;
    }

    private ComputerLauncher HandleSSHLauncher(SlaveInstanceDetails instanceDetailsById) throws IOException {
        ComputerLauncher  retVal     = null;
        BaseSpotinstCloud cloud      = this.getSpotinstCloud();
        String            instanceId = this.name;
        String            ipAddress;

        if (instanceDetailsById == null) {
            LOGGER.info(String.format(
                    "no details about instance %s in instanceDetailsById map, not initializing launcher yet.",
                    this.name));
            return null;
        }

        if (cloud.getShouldUsePrivateIp()) {
            ipAddress = instanceDetailsById.getPrivateIp();
        }
        else {
            ipAddress = instanceDetailsById.getPublicIp();
        }

        if (ipAddress != null) {
            try {
                // TODO shibel: fix this
                Boolean shouldRetriggerBuilds = cloud.getShouldRetriggerBuilds();
                retVal = new SpotSSHComputerLauncher(
                        cloud.getComputerConnector().launch(instanceDetailsById.getPublicIp(), TaskListener.NULL),
                        shouldRetriggerBuilds);
                this.getSpotinstCloud().connectAgent(this, ipAddress);

            }
            catch (InterruptedException e) {
                String preformatted = "Creating SSHComputerLauncher for SpotinstSlave (instance %s) was interrupted";
                LOGGER.error(String.format(preformatted, instanceId));
            }
        }
        else {
            String preformatted = "SSH-cloud instance %s does not have an IP yet, setting launcher to null for now.";
            LOGGER.info(String.format(preformatted, instanceId));
        }
        return retVal;
    }
    //endregion


    //region Extension class
    @Extension
    public static class DescriptorImpl extends SlaveDescriptor {

        @Override
        public String getDisplayName() {
            return "Spot Node";
        }

        @Override
        public boolean isInstantiable() {
            return false;
        }
    }
    //endregion
}
