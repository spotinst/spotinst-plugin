package hudson.plugins.spotinst.slave;

import hudson.model.*;
import hudson.model.queue.SubTask;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.DelegatingComputerLauncher;
import hudson.slaves.SlaveComputer;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.LinkedList;
import java.util.List;

//TODO shibel consider with Ohad: should we duplicate the code for afterDisconnect()?
// put in helper
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
