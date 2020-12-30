package hudson.plugins.spotinst.slave;

import hudson.slaves.ComputerLauncher;
import hudson.slaves.DelegatingComputerLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;

//TODO shibel consider with Ohad: should we duplicate the code for afterDisconnect()?
// put in helper
/**
 * Created by Shibel Karmi Mansour on 30/12/2020.
 */
@ThreadSafe
public class SpotSSHComputerLauncher extends DelegatingComputerLauncher {
    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotSSHComputerLauncher.class);
    //endregion

    //region Constructor
    public SpotSSHComputerLauncher(final ComputerLauncher launcher) {
        super(launcher);
    }
    //endregion
}
