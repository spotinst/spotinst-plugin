package hudson.plugins.spotinst.slave;

import hudson.slaves.ComputerLauncher;
import hudson.slaves.DelegatingComputerLauncher;

public class SpotSSHComputerLauncher extends DelegatingComputerLauncher {

    protected SpotSSHComputerLauncher(ComputerLauncher launcher) {
        super(launcher);
    }
}
