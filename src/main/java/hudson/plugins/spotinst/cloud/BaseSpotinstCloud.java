package hudson.plugins.spotinst.cloud;

import hudson.DescriptorExtensionList;
import hudson.model.*;
import hudson.model.labels.LabelAtom;
import hudson.plugins.spotinst.api.infra.JsonMapper;
import hudson.plugins.spotinst.common.ConnectionMethodEnum;
import hudson.plugins.spotinst.common.Constants;
import hudson.plugins.spotinst.common.TimeUtils;
import hudson.plugins.spotinst.slave.*;
import hudson.slaves.Cloud;
import hudson.slaves.ComputerConnector;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodeProvisioner.PlannedNode;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolLocationNodeProperty;
import jenkins.model.Jenkins;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by ohadmuchnik on 25/05/2016.
 */
public abstract class BaseSpotinstCloud extends Cloud {

    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseSpotinstCloud.class);

    protected           String                            accountId;
    protected           String                            groupId;
    protected           Map<String, PendingInstance>      pendingInstances;
    protected           Map<String, SlaveInstanceDetails> slaveInstancesDetailsByInstanceId;
    private             String                            labelString;
    private             String                            idleTerminationMinutes;
    private             String                            workspaceDir;
    private             Set<LabelAtom>                    labelSet;
    private             SlaveUsageEnum                    usage;
    private             String                            tunnel;
    private             String                            vmargs;
    private             EnvironmentVariablesNodeProperty  environmentVariables;
    private             ToolLocationNodeProperty          toolLocations;
    private             Boolean                           shouldUseWebsocket;
    private             Boolean                           shouldRetriggerBuilds;
    private             ComputerConnector                 computerConnector;
    private             ConnectionMethodEnum              connectionMethod;
    private             String                            credentialsId;
    private             Boolean                           shouldUsePrivateIp;
    //endregion

    //region Constructor
    public BaseSpotinstCloud(String groupId, String labelString, String idleTerminationMinutes, String workspaceDir,
                             SlaveUsageEnum usage, String tunnel, Boolean shouldUseWebsocket,
                             Boolean shouldRetriggerBuilds, String vmargs,
                             EnvironmentVariablesNodeProperty environmentVariables,
                             ToolLocationNodeProperty toolLocations, String accountId, String credentialsId,
                             ConnectionMethodEnum connectionMethod, ComputerConnector computerConnector,
                             Boolean shouldUsePrivateIp) {
        // TODO shibel: check descriptorOrg in branch ssh-research

        super(groupId);
        this.groupId = groupId;
        this.accountId = accountId;
        this.labelString = labelString;
        this.idleTerminationMinutes = idleTerminationMinutes;
        this.workspaceDir = workspaceDir;
        this.pendingInstances = new HashMap<>();
        labelSet = Label.parse(labelString);

        if (usage != null) {
            this.usage = usage;
        }
        else {
            this.usage = SlaveUsageEnum.NORMAL;
        }

        this.shouldRetriggerBuilds = shouldRetriggerBuilds == null || BooleanUtils.isTrue(shouldRetriggerBuilds);
        this.tunnel = tunnel;
        this.shouldUseWebsocket = shouldUseWebsocket;
        this.vmargs = vmargs;
        this.environmentVariables = environmentVariables;
        this.toolLocations = toolLocations;
        this.slaveInstancesDetailsByInstanceId = new HashMap<>();
        this.computerConnector = computerConnector;
        this.credentialsId = credentialsId;
        this.connectionMethod = connectionMethod;
        this.shouldUsePrivateIp = shouldUsePrivateIp;
    }
    //endregion

    //region Overridden Public Methods
    @Override
    public Collection<PlannedNode> provision(Label label, int excessWorkload) {
        ProvisionRequest request = new ProvisionRequest(label, excessWorkload);

        LOGGER.info(String.format("Got provision slave request: %s", JsonMapper.toJson(request)));

        setNumOfNeededExecutors(request);

        if (request.getExecutors() > 0) {
            LOGGER.info(String.format("Need to scale up %s units", request.getExecutors()));

            List<SpotinstSlave> slaves = provisionSlaves(request);

            if (slaves.size() > 0) {
                for (final SpotinstSlave slave : slaves) {
                    try {
                        Jenkins.getInstance().addNode(slave);
                    }
                    catch (IOException e) {
                        LOGGER.error(String.format("Failed to create node for slave: %s", slave.getInstanceId()), e);
                    }
                }
            }
        }
        else {
            LOGGER.info("No need to scale up new slaves, there are some that are initiating");
        }

        return Collections.emptyList();
    }

    @Override
    public boolean canProvision(Label label) {
        boolean canProvision = true;

        if (label != null) {
            canProvision = label.matches(labelSet);
        }
        else {
            if (this.usage.equals(SlaveUsageEnum.EXCLUSIVE) && labelSet.size() > 0) {
                canProvision = false;
            }
        }

        return canProvision;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public String getDisplayName() {
        return this.name;
    }

    public Boolean isInstancePending(String id) {
        Boolean retVal = pendingInstances.containsKey(id);
        return retVal;
    }
    //endregion

    //region Public Methods
    public void onInstanceReady(String instanceId) {
        pendingInstances.remove(instanceId);
    }

    public void monitorInstances() {
        if (pendingInstances.size() > 0) {
            List<String> keys = new LinkedList<>(pendingInstances.keySet());

            for (String key : keys) {
                PendingInstance pendingInstance = pendingInstances.get(key);

                if (pendingInstance != null) {

                    Integer pendingThreshold = getPendingThreshold();
                    Boolean isPendingOverThreshold =
                            TimeUtils.isTimePassed(pendingInstance.getCreatedAt(), pendingThreshold);

                    if (isPendingOverThreshold) {
                        LOGGER.info(String.format(
                                "Instance %s is in initiating state for over than %s minutes, ignoring this instance",
                                pendingInstance.getId(), pendingThreshold));
                        pendingInstances.remove(key);
                    }
                }
            }

            checkIpsForSSHAgents(pendingInstances);
        }
    }

    private void checkIpsForSSHAgents(Map<String, PendingInstance> pendingInstances) {

        List<SpotinstSlave> offlineAgents = getOfflineSSHAgents(pendingInstances);

        if (offlineAgents.size() > 0) {
            Map<String, String> instanceIpById = getInstanceIpsById();

            for (SpotinstSlave offlineAgent : offlineAgents) {
                String agentName  = offlineAgent.getNodeName();
                String ipForAgent = instanceIpById.get(agentName);

                if (ipForAgent != null) {
                    String preFormat = "IP for agent %s is now available at %s, trying to attach SSHLauncher and launch";
                    LOGGER.info(String.format(preFormat, agentName, ipForAgent));
                    //TODO shibel: handle failures better
                    connectAgent(offlineAgent, ipForAgent);
                }
                else {
                    String preFormat = "IP for agent %s is still null, not attaching SSH launcher";
                    LOGGER.info(String.format(preFormat, agentName));
                }
            }
        }
        else {
            LOGGER.info("There are no offline SSH agents waiting to connect");
        }
    }

    private void connectAgent(SpotinstSlave offlineAgent, String ipForAgent) {
        SpotinstComputer computerForAgent = (SpotinstComputer) offlineAgent.getComputer();

        if (computerForAgent != null) {
            ComputerConnector connector = getComputerConnector();


            // TODO shibel: check this logic more thoroughly - can we connect a JNLP launcher here by mistake?
            if (computerForAgent.getLauncher() == null ||
                computerForAgent.getLauncher().getClass() != SpotinstComputerLauncher.class) {

                try {
                    SpotSSHComputerLauncher launcher = new SpotSSHComputerLauncher(connector.launch(ipForAgent, computerForAgent.getListener()));

                    offlineAgent.setLauncher(launcher);
                    // TODO shibel: ask Ohad, do we want to expose those logs?
                    // naturally the first 1-2 attempts will fail
                    // because instance is still initiating / Java isn't installed yet
                    launcher.launch(computerForAgent, computerForAgent.getListener());


                }
                catch (IOException | InterruptedException e) {
                    // TODO shibel: handle better
                    LOGGER.error(e.getCause().toString());
                    LOGGER.error(e.getMessage());
                    e.printStackTrace();
                }

            }
        }
    }

    private List<SpotinstSlave> getOfflineSSHAgents(Map<String, PendingInstance> pendingInstances) {
        List<SpotinstSlave> retVal = new LinkedList<>();

        if (pendingInstances.size() > 0) {
            List<String> keys = new LinkedList<>(pendingInstances.keySet());

            for (String key : keys) {
                PendingInstance pendingInstance = pendingInstances.get(key);
                String          instanceId      = pendingInstance.getId();
                SpotinstSlave   agent           = (SpotinstSlave) Jenkins.get().getNode(instanceId);

                if (agent == null) {
                    LOGGER.warn(String.format("Pending instance %s does not have a SpotinstSlave", instanceId));
                    continue;
                }

                SpotinstComputer computerForAgent = (SpotinstComputer) agent.getComputer();

                if (computerForAgent == null) {
                    LOGGER.warn(String.format("Agent %s does not have a computer", instanceId));
                    continue;
                }

                if (computerForAgent.isOnline()) {
                    LOGGER.info(String.format("Agent %s is already online, no need to handle", instanceId));
                }
                else {

                    if (computerForAgent.getLauncher().getClass() != SpotinstComputerLauncher.class) {
                        retVal.add(agent);
                    }
                }

            }
        }

        return retVal;
    }

    public SlaveInstanceDetails getSlaveDetails(String instanceId) {
        SlaveInstanceDetails retVal = slaveInstancesDetailsByInstanceId.get(instanceId);

        return retVal;
    }
    //endregion

    //region Private Methods
    private synchronized List<SpotinstSlave> provisionSlaves(ProvisionRequest request) {
        LOGGER.info(String.format("Scale up group: %s with %s workload units", groupId, request.getExecutors()));

        List<SpotinstSlave> slaves = scaleUp(request);
        return slaves;
    }

    private void setNumOfNeededExecutors(ProvisionRequest request) {
        PendingExecutorsCounts pendingExecutorsCounts = getPendingExecutors(request);

        Integer pendingExecutors    = pendingExecutorsCounts.getPendingExecutors();
        Integer initiatingExecutors = pendingExecutorsCounts.getInitiatingExecutors();

        Integer currentPendingExecutors = pendingExecutors + initiatingExecutors;

        LOGGER.info(
                String.format("Pending instances executors: %s, Initiating instances executors: %s, for request: %s",
                              pendingExecutors, initiatingExecutors, JsonMapper.toJson(request)));

        if (request.getExecutors() > currentPendingExecutors) {
            Integer neededExecutors = request.getExecutors() - currentPendingExecutors;
            request.setExecutors(neededExecutors);
        }
        else {
            request.setExecutors(0);
        }
    }

    private List<NodeProperty<?>> buildNodeProperties() {
        List<NodeProperty<?>> retVal = new LinkedList<>();

        if (this.environmentVariables != null && this.environmentVariables.getEnvVars() != null) {
            retVal.add(this.environmentVariables);
        }

        if (this.toolLocations != null && this.toolLocations.getLocations() != null) {
            retVal.add(this.toolLocations);
        }

        return retVal;
    }
    //endregion

    //region Protected Methods
    protected void addToPending(String id, Integer numOfExecutors, PendingInstance.StatusEnum status, String label) {
        PendingInstance pendingInstance = new PendingInstance();
        pendingInstance.setId(id);
        pendingInstance.setNumOfExecutors(numOfExecutors);
        pendingInstance.setStatus(status);
        pendingInstance.setCreatedAt(new Date());
        pendingInstance.setRequestedLabel(label);
        pendingInstances.put(id, pendingInstance);
    }

    protected SpotinstSlave buildSpotinstSlave(String id, String instanceType, String numOfExecutors) {
        SpotinstSlave slave = null;
        Node.Mode     mode  = Node.Mode.NORMAL;

        if (this.usage != null && this.usage.equals(SlaveUsageEnum.EXCLUSIVE)) {
            mode = Node.Mode.EXCLUSIVE;
        }

        List<NodeProperty<?>> nodeProperties = buildNodeProperties();

        try {
            slave = new SpotinstSlave(this, id, groupId, id, instanceType, labelString, idleTerminationMinutes,
                                      workspaceDir, numOfExecutors, mode, this.tunnel, this.shouldUseWebsocket,
                                      this.vmargs, nodeProperties, this.shouldRetriggerBuilds);
        }
        catch (Descriptor.FormException | IOException e) {
            LOGGER.error(String.format("Failed to build Spotinst slave for: %s", id));
            e.printStackTrace();
        }

        return slave;
    }

    protected List<SpotinstSlave> getAllSpotinstSlaves() {
        LOGGER.info(String.format("Getting all existing slaves for group: %s", groupId));

        List<SpotinstSlave> retVal   = new LinkedList<>();
        List<Node>          allNodes = Jenkins.getInstance().getNodes();

        LOGGER.info(String.format("Found total %s nodes in Jenkins, filtering the group nodes", allNodes.size()));

        for (Node node : allNodes) {

            if (node instanceof SpotinstSlave) {
                SpotinstSlave slave = (SpotinstSlave) node;

                if (slave.getElastigroupId().equals(groupId)) {
                    retVal.add(slave);
                }
            }
        }

        return retVal;
    }

    protected PendingExecutorsCounts getPendingExecutors(ProvisionRequest request) {
        PendingExecutorsCounts retVal              = new PendingExecutorsCounts();
        Integer                pendingExecutors    = 0;
        Integer                initiatingExecutors = 0;

        for (PendingInstance pendingInstance : pendingInstances.values()) {
            if (request.getLabel() == null || (pendingInstance.getRequestedLabel() != null &&
                                               pendingInstance.getRequestedLabel().equals(request.getLabel()))) {
                switch (pendingInstance.getStatus()) {
                    case PENDING: {
                        pendingExecutors += pendingInstance.getNumOfExecutors();
                    }
                    break;
                    case INSTANCE_INITIATING: {
                        initiatingExecutors += pendingInstance.getNumOfExecutors();
                    }
                    break;
                    default: {
                        LOGGER.warn(String.format("Pending instance %s has unknown status %s", pendingInstance.getId(),
                                                  pendingInstance.getStatus().getName()));
                    }
                    break;
                }
            }
        }

        retVal.setPendingExecutors(pendingExecutors);
        retVal.setInitiatingExecutors(initiatingExecutors);

        return retVal;
    }

    protected Integer getPendingThreshold() {
        return Constants.PENDING_INSTANCE_TIMEOUT_IN_MINUTES;
    }

    protected Integer getSlaveOfflineThreshold() {
        return Constants.SLAVE_OFFLINE_THRESHOLD_IN_MINUTES;
    }
    //endregion

    //region Getters / Setters
    public String getGroupId() {
        return groupId;
    }

    public String getWorkspaceDir() {
        return workspaceDir;
    }

    public SlaveUsageEnum getUsage() {
        return usage;
    }

    public String getTunnel() {
        return tunnel;
    }

    public String getVmargs() {
        return vmargs;
    }

    public String getAccountId() {
        return accountId;
    }

    public EnvironmentVariablesNodeProperty getEnvironmentVariables() {
        return environmentVariables;
    }

    public ToolLocationNodeProperty getToolLocations() {
        return toolLocations;
    }

    public String getLabelString() {
        return labelString;
    }

    public String getIdleTerminationMinutes() {
        return idleTerminationMinutes;
    }

    public void setPendingInstances(Map<String, PendingInstance> pendingInstances) {
        this.pendingInstances = pendingInstances;
    }

    public Boolean getShouldUseWebsocket() {
        return shouldUseWebsocket;
    }

    public void setShouldUseWebsocket(Boolean shouldUseWebsocket) {
        this.shouldUseWebsocket = shouldUseWebsocket;
    }

    public Boolean getShouldRetriggerBuilds() {
        return shouldRetriggerBuilds;
    }

    public void setShouldRetriggerBuilds(Boolean shouldRetriggerBuilds) {
        this.shouldRetriggerBuilds = shouldRetriggerBuilds;
    }

    public ConnectionMethodEnum getConnectionMethod() {
        return connectionMethod;
    }

    public void setConnectionMethod(ConnectionMethodEnum connectionMethod) {
        this.connectionMethod = connectionMethod;
    }


    public ComputerConnector getComputerConnector() {
        return computerConnector;
    }

    public void setComputerConnector(ComputerConnector computerConnector) {
        this.computerConnector = computerConnector;
    }

    public String getCredentialsId() {
        return credentialsId;
    }


    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public Boolean getShouldUsePrivateIp() {
        // default for clouds that were configured before introducing this field
        if (shouldUsePrivateIp == null) {
            return false;
        }
        return shouldUsePrivateIp;
    }

    public void setShouldUsePrivateIp(Boolean shouldUsePrivateIp) {
        this.shouldUsePrivateIp = shouldUsePrivateIp;
    }
    //endregion

    //region Abstract Methods
    abstract List<SpotinstSlave> scaleUp(ProvisionRequest request);

    public abstract Boolean detachInstance(String instanceId);

    public abstract String getCloudUrl();

    public abstract void syncGroupInstances();

    public abstract Map<String, String> getInstanceIpsById();
    //endregion

    //region Abstract Class
    public static abstract class DescriptorImpl extends Descriptor<Cloud> {
        public DescriptorExtensionList<ToolInstallation, ToolDescriptor<?>> getToolDescriptors() {
            return ToolInstallation.all();
        }

        public String getKey(ToolInstallation installation) {
            return installation.getDescriptor().getClass().getName() + "@" + installation.getName();
        }

        public List getComputerConnectorDescriptors() {
            return Jenkins.get().getDescriptorList(ComputerConnector.class);
        }
    }
    //endregion

}