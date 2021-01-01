package hudson.plugins.spotinst.slave;

import hudson.model.*;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.DelegatingComputerLauncher;
import hudson.slaves.SlaveComputer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Created by Shibel Karmi Mansour on 30/12/2020.
 */
@ThreadSafe
public class SpotSSHComputerLauncher extends DelegatingComputerLauncher {
    //region Members
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
        LOGGER.info("afterDisconnect SSH called");
        SpotLauncherHelper.handleDisconnect(computer, this.shouldRetriggerBuilds);

        // call parent
        super.afterDisconnect(computer, listener);
    }

}
