package hudson.plugins.spotinst.cloud;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHAuthenticator;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.SchemeRequirement;
import com.trilead.ssh2.Connection;
import hudson.DescriptorExtensionList;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.plugins.spotinst.api.infra.JsonMapper;
import hudson.plugins.spotinst.common.ConnectionMethodEnum;
import hudson.plugins.spotinst.common.Constants;
import hudson.plugins.spotinst.common.TimeUtils;
import hudson.plugins.spotinst.slave.SlaveInstanceDetails;
import hudson.plugins.spotinst.slave.SlaveUsageEnum;
import hudson.plugins.spotinst.slave.SpotinstSlave;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.plugins.sshslaves.verifiers.SshHostKeyVerificationStrategy;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.slaves.Cloud;
import hudson.slaves.ComputerConnector;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodeProvisioner.PlannedNode;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolLocationNodeProperty;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang.BooleanUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;
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
    private transient   StandardUsernameCredentials       credentials;
    private             ComputerConnector                 computerConnector;
    private             ConnectionMethodEnum              connectionMethod;
    public static final SchemeRequirement                 SSH_SCHEME = new SchemeRequirement("ssh");
    private             SshHostKeyVerificationStrategy    sshHostKeyVerificationStrategy;
    private             String                            credentialsId;
    //endregion

    //region Constructor
    public BaseSpotinstCloud(String groupId, String labelString, String idleTerminationMinutes, String workspaceDir,
                             SlaveUsageEnum usage, String tunnel, Boolean shouldUseWebsocket,
                             Boolean shouldRetriggerBuilds, String vmargs,
                             EnvironmentVariablesNodeProperty environmentVariables,
                             ToolLocationNodeProperty toolLocations, String accountId, String credentialsId,
                             ConnectionMethodEnum connectionMethod, ComputerConnector computerConnector) {
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
        }
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

    public SshHostKeyVerificationStrategy getSshHostKeyVerificationStrategy() {
        return sshHostKeyVerificationStrategy;
    }

    public void setSshHostKeyVerificationStrategy(SshHostKeyVerificationStrategy sshHostKeyVerificationStrategy) {
        this.sshHostKeyVerificationStrategy = sshHostKeyVerificationStrategy;
    }

    public String getCredentialsId() {
        return credentialsId;
    }


    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }
    //endregion

    //region Abstract Methods
    abstract List<SpotinstSlave> scaleUp(ProvisionRequest request);

    public abstract Boolean detachInstance(String instanceId);

    public abstract String getCloudUrl();

    public abstract void syncGroupInstances();
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

        @RequirePOST
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath AccessControlled context,
                                                     @QueryParameter String host, @QueryParameter String port,
                                                     @QueryParameter String credentialsId) {
            Jenkins jenkins = Jenkins.get();
            if ((context == jenkins && !jenkins.hasPermission(Computer.CREATE)) ||
                (context != jenkins && !context.hasPermission(Computer.CONFIGURE))) {
                return new StandardUsernameListBoxModel().includeCurrentValue(credentialsId);
            }
            try {
                // TODO shibel: a number format exception was possible when the port is specified
                //  but is not possible anymore, so check whether to catch here at all, and what.

                //                int portValue = Integer.parseInt(port);
                return new StandardUsernameListBoxModel()
                        .includeMatchingAs(ACL.SYSTEM, jenkins, StandardUsernameCredentials.class,
                                           Collections.singletonList(new DomainRequirement()),
                                           SSHAuthenticator.matcher(Connection.class)).includeCurrentValue(
                                credentialsId); // always add the current value last in case already present
            }
            catch (NumberFormatException ex) {
                return new StandardUsernameListBoxModel().includeCurrentValue(credentialsId);
            }
        }
    }
    //endregion

    //region Helper Methods
    public StandardUsernameCredentials getCredentials() {
        String credentialsId =
                this.credentialsId == null ? (this.credentials == null ? null : this.credentials.getId()) :
                this.credentialsId;
        try {
            StandardUsernameCredentials credentials =
                    credentialsId != null ? SSHLauncher.lookupSystemCredentials(credentialsId) : null;
            if (credentials != null) {
                this.credentials = credentials;
                return credentials;
            }
        }
        catch (Throwable t) {
            // ignore
        }

        return this.credentials;
    }

    public static StandardUsernameCredentials lookupSystemCredentials(String credentialsId) {
        return CredentialsMatchers.firstOrNull(CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class,
                                                                                     Jenkins.get(), ACL.SYSTEM,
                                                                                     SSH_SCHEME),
                                               CredentialsMatchers.withId(credentialsId));
    }
    //endregion
}