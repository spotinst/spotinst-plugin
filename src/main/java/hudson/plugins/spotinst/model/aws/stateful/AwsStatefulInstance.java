package hudson.plugins.spotinst.model.aws.stateful;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import hudson.plugins.spotinst.common.stateful.AwsStatefulInstanceStateEnum;
import hudson.plugins.spotinst.model.common.BaseStatefulInstance;

/**
 * Created by ItayShklar on 07/08/2023.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AwsStatefulInstance extends BaseStatefulInstance {
    //region members
    private String                       instanceId;
    private AwsStatefulInstanceStateEnum state;
    //endregion

    //region getters & setters
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public AwsStatefulInstanceStateEnum getState() {
        return state;
    }

    public void setState(AwsStatefulInstanceStateEnum state) {
        this.state = state;
    }
    //endregion
}
