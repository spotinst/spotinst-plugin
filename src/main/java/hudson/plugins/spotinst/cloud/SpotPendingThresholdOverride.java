package hudson.plugins.spotinst.cloud;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Created by Itay Shklar on 28/02/2023.
 */
public class SpotPendingThresholdOverride implements Describable<SpotPendingThresholdOverride> {
    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotPendingThresholdOverride.class);

    private boolean isEnabled;
    private Integer pendingThreshold;
    //endregion

    //region Constructors
    @DataBoundConstructor
    public SpotPendingThresholdOverride(boolean isEnabled, Integer pendingThreshold) {
        this.isEnabled = isEnabled;
        boolean isPendingThresholdOverrideValid = isPendingThresholdOverrideValid(pendingThreshold);

        if (isPendingThresholdOverrideValid) {
            this.pendingThreshold = pendingThreshold;
        }
    }

    //region Overrides
    @Override
    public Descriptor<SpotPendingThresholdOverride> getDescriptor() {
        Descriptor<SpotPendingThresholdOverride> retVal = Jenkins.getInstance().getDescriptor(getClass());

        if (retVal == null) {
            throw new RuntimeException("Descriptor of type SpotPendingThresholdOverride cannot be null");
        }

        return retVal;
    }
    //endregion

    //region Classes
    @Extension
    public static final class DescriptorImpl extends Descriptor<SpotPendingThresholdOverride> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Spot Pending Threshold Override";
        }
    }
    //endregion

    //region Getters / Setters
    public boolean getIsEnabled() {
        return isEnabled;
    }

    @DataBoundSetter
    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Integer getPendingThreshold() {
        return pendingThreshold;
    }

    @DataBoundSetter
    public void setPendingThreshold(Integer pendingThreshold) {
        this.pendingThreshold = pendingThreshold;
    }
    //endregion

    //region private methods
    private static boolean isPendingThresholdOverrideValid(Integer pendingThreshold) {
        boolean retVal = false;
        String warningMsg =
                String.format("Pending Threshold Override attribute has an invalid value %s", pendingThreshold);

        if (pendingThreshold != null) {
            if (pendingThreshold > 0) {
                retVal = true;
            }
            else {
                LOGGER.warn(warningMsg);
            }
        }
        else {
            LOGGER.warn(warningMsg);
        }

        return retVal;
    }
    //endregion
}
