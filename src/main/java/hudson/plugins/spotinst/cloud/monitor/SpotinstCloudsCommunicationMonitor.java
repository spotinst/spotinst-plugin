package hudson.plugins.spotinst.cloud.monitor;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.plugins.spotinst.cloud.BaseSpotinstCloud;
import hudson.plugins.spotinst.common.GroupLockingManager;
import hudson.plugins.spotinst.common.SpotinstCloudCommunicationState;
import jenkins.model.Jenkins;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Extension
public class SpotinstCloudsCommunicationMonitor extends AdministrativeMonitor {

    //region Members
    private static List<GroupLockingManager> groupLockingManagers;
    private        List<String>              spotinstCloudsCommunicationFailures;
    private        List<String>              spotinstCloudsCommunicationInitializing;
    //endregion

    //region Overridden Public Methods
    @Override
    public boolean isActivated() {
        initMonitor();
        return isSpotinstCloudsCommunicationFailuresExist() || isSpotinstCloudsCommunicationInitializingExist();
    }

    @Override
    public String getDisplayName() {
        return "Spotinst Clouds Communication Monitor";
    }
    //endregion

    //region methods
    public boolean isSpotinstCloudsCommunicationFailuresExist() {
        return CollectionUtils.isNotEmpty(spotinstCloudsCommunicationFailures);
    }

    public boolean isSpotinstCloudsCommunicationInitializingExist() {
        return CollectionUtils.isNotEmpty(spotinstCloudsCommunicationInitializing);
    }
    //endregion

    //region private methods
    private void initMonitor(){
        groupLockingManagers = getGroupLockingManagers();
        initSpotinstCloudsCommunicationInitializing();
        initSpotinstCloudsCommunicationFailures();
    }

    private void initSpotinstCloudsCommunicationFailures() {
        if (groupLockingManagers != null) {
            spotinstCloudsCommunicationFailures = groupLockingManagers.stream().filter(group ->
                                                                                               group.getCloudCommunicationState() ==
                                                                                               SpotinstCloudCommunicationState.FAILED)
                                                                      .map(GroupLockingManager::getErrorDescription)
                                                                      .collect(Collectors.toList());
        }
        else {
            spotinstCloudsCommunicationFailures = Collections.emptyList();
        }
    }

    private void initSpotinstCloudsCommunicationInitializing() {
        if (groupLockingManagers != null) {
            spotinstCloudsCommunicationInitializing = groupLockingManagers.stream().filter(group ->
                                                                                                   group.getCloudCommunicationState() ==
                                                                                                   SpotinstCloudCommunicationState.INITIALIZING)
                                                                          .map(GroupLockingManager::getGroupId)
                                                                          .collect(Collectors.toList());
        }
        else {
            spotinstCloudsCommunicationInitializing = Collections.emptyList();
        }
    }

    private static List<GroupLockingManager> getGroupLockingManagers() {
        List<GroupLockingManager> retVal  = null;
        Jenkins                   jenkins = Jenkins.getInstanceOrNull();

        if (jenkins != null) {
            retVal = jenkins.clouds.stream().filter(cloud -> cloud instanceof BaseSpotinstCloud)
                                   .map(baseCloud -> ((BaseSpotinstCloud) baseCloud).getGroupLockingManager())
                                   .filter(Objects::nonNull).collect(Collectors.toList());
        }

        return retVal;
    }
    //endregion

    //region getters & setters
    public List<String> getSpotinstCloudsCommunicationFailures() {
        return spotinstCloudsCommunicationFailures;
    }

    public String getSpotinstCloudsCommunicationInitializing() {
        return String.join(", ", spotinstCloudsCommunicationInitializing);
    }
    //endregion
}
