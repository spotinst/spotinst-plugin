package hudson.plugins.spotinst.slave;

import jenkins.util.NonLocalizable;

import java.util.Locale;

class SpotinstNonLocalizable extends NonLocalizable {

    public SpotinstNonLocalizable(String nonLocalizable) {
        super(nonLocalizable);
    }

    @Override
    public String toString(Locale locale) {
        return super.toString(locale);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
