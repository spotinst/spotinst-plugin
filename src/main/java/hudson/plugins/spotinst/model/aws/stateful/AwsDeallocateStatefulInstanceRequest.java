package hudson.plugins.spotinst.model.aws.stateful;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by sitay on 30/08/23.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AwsDeallocateStatefulInstanceRequest {
    //region members
    private AwsStatefulDeAllocationConfig statefulDeAllocationConfig;
    //endregion

    //region getters & setters
    public AwsStatefulDeAllocationConfig getStatefulDeAllocationConfig() {
        return statefulDeAllocationConfig;
    }

    public void setStatefulDeAllocationConfig(AwsStatefulDeAllocationConfig statefulDeAllocationConfig) {
        this.statefulDeAllocationConfig = statefulDeAllocationConfig;
    }
    //endregion
}