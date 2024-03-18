package hudson.plugins.spotinst.cloud;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.apache.commons.lang.BooleanUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class SpotReTriggerBuilds implements Describable<SpotReTriggerBuilds> {
    //region members
    private Boolean shouldReTriggerBuilds;
    private Boolean stickyNode;
    //endregion

    //region Ctor
    @DataBoundConstructor
    public SpotReTriggerBuilds(Boolean shouldReTriggerBuilds, Boolean stickyNode) {
        this.shouldReTriggerBuilds = BooleanUtils.isTrue(shouldReTriggerBuilds);
        this.stickyNode = this.shouldReTriggerBuilds && BooleanUtils.isTrue(stickyNode);
    }
    //endregion

    //region Overrides
    @Override
    public Descriptor<SpotReTriggerBuilds> getDescriptor() {
        Descriptor<SpotReTriggerBuilds> retVal = Jenkins.get().getDescriptor(getClass());

        if (retVal == null) {
            throw new RuntimeException("Descriptor of type SpotReTriggerBuilds cannot be null");
        }

        return retVal;
    }
    //endregion

    //region Classes
    @Extension
    public static final class DescriptorImpl extends Descriptor<SpotReTriggerBuilds> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Spot ReTrigger Builds";
        }
    }
    //endregion

    //region getters & setters
    public Boolean getShouldReTriggerBuilds() {
        return shouldReTriggerBuilds;
    }

    public void setShouldReTriggerBuilds(Boolean shouldReTriggerBuilds) {
        this.shouldReTriggerBuilds = shouldReTriggerBuilds;
    }

    public Boolean getStickyNode() {
        return stickyNode;
    }

    public void setStickyNode(Boolean stickyNode) {
        this.stickyNode = stickyNode;
    }
    //endregio
}
