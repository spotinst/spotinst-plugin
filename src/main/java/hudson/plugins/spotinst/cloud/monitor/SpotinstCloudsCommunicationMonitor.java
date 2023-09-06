package hudson.plugins.spotinst.cloud.monitor;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.plugins.spotinst.cloud.BaseSpotinstCloud;
import hudson.plugins.spotinst.common.GroupLockingManager;
import hudson.plugins.spotinst.common.SpotinstCloudCommunicationState;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Extension
public class SpotinstCloudsCommunicationMonitor extends AdministrativeMonitor {

    //region Members
    List<String> spotinstCloudsCommunicationFailures;
    List<String> spotinstCloudsCommunicationInitializing;
    //endregion

    //region Overridden Public Methods
    @Override
    public boolean isActivated() {
        return isSpotinstCloudsCommunicationFailuresExist() || isSpotinstCloudsCommunicationInitializingExist();
    }

    @Override
    public String getDisplayName() {
        return "Spotinst Clouds Communication Monitor";
    }
    //endregion

    //region methods
    public boolean isSpotinstCloudsCommunicationFailuresExist() {
        return isSpotinstCloudsCommunicationStateExist(SpotinstCloudCommunicationState.FAILED);
    }

    public boolean isSpotinstCloudsCommunicationInitializingExist() {
        return isSpotinstCloudsCommunicationStateExist(SpotinstCloudCommunicationState.INITIALIZING);
    }

    private boolean isSpotinstCloudsCommunicationStateExist(SpotinstCloudCommunicationState state) {
        boolean                     retVal               = false;
        Stream<GroupLockingManager> groupLockingManagers = getGroupLockingManagers();

        if (groupLockingManagers != null) {
            retVal = groupLockingManagers.anyMatch(groupDetails -> state == groupDetails.getCloudCommunicationState());
        }

        return retVal;
    }
    //endregion

    //region private methods
    private Stream<GroupLockingManager> getGroupLockingManagers() {
        Stream<GroupLockingManager> retVal  = null;
        Jenkins                     jenkins = Jenkins.getInstanceOrNull();

        if (jenkins != null) {
            retVal = jenkins.clouds.stream().filter(cloud -> cloud instanceof BaseSpotinstCloud)
                                   .map(baseCloud -> ((BaseSpotinstCloud) baseCloud).getGroupLockingManager())
                                   .filter(Objects::nonNull);
        }

        return retVal;
    }
    //endregion

    //region getters & setters
    public List<String> getSpotinstCloudsCommunicationFailures() {
        Stream<GroupLockingManager> groupLockingManagers = getGroupLockingManagers();

        if (groupLockingManagers != null) {
            spotinstCloudsCommunicationFailures = groupLockingManagers.filter(
                                                                       group -> group.getCloudCommunicationState() == SpotinstCloudCommunicationState.FAILED)
                                                               .map(GroupLockingManager::getErrorDescription)
                                                               .collect(Collectors.toList());
        }
        else {
            spotinstCloudsCommunicationFailures = new LinkedList<>();
        }

        return spotinstCloudsCommunicationFailures;
    }

    public String getSpotinstCloudsCommunicationInitializing() {
        String retVal;
        spotinstCloudsCommunicationInitializing = new ArrayList<>();
        Stream<GroupLockingManager> groupLockingManagers = getGroupLockingManagers();

        if (groupLockingManagers != null) {
            groupLockingManagers.forEach(group -> {
                if (group.getCloudCommunicationState() == SpotinstCloudCommunicationState.INITIALIZING) {
                    spotinstCloudsCommunicationInitializing.add(group.getGroupId());
                }
            });
        }

        retVal = String.join(", ", spotinstCloudsCommunicationInitializing);

        return retVal;
    }
    //endregion
}
