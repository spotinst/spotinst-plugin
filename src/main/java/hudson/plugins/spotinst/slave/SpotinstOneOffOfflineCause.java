package hudson.plugins.spotinst.slave;

import hudson.slaves.OfflineCause;

public class SpotinstOneOffOfflineCause extends OfflineCause.SimpleOfflineCause {
    public SpotinstOneOffOfflineCause (SpotinstNonLocalizable nonLocalizable) {
        super(nonLocalizable);
    }
}
