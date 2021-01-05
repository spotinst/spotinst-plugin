package hudson.plugins.spotinst.slave;

import hudson.model.*;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.DelegatingComputerLauncher;
import hudson.slaves.SlaveComputer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shibel Karmi Mansour on 30/12/2020.
 */
public class SpotSSHComputerLauncher extends DelegatingComputerLauncher {
    //region Members
    //todo shibel - remove
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotSSHComputerLauncher.class);
    private Boolean shouldRetriggerBuilds;
    //endregion

    //region Constructor
    public SpotSSHComputerLauncher(final ComputerLauncher launcher, Boolean shouldRetriggerBuilds) {
        super(launcher);
        this.shouldRetriggerBuilds = shouldRetriggerBuilds;
    }
    //endregion

    @Override
    public void afterDisconnect(final SlaveComputer computer, final TaskListener listener) {
        SpotLauncherHelper.handleDisconnect(computer, this.shouldRetriggerBuilds);
        // call parent
        super.afterDisconnect(computer, listener);
    }

}
