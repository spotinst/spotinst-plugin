package hudson.plugins.spotinst.model.aws;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AwsInstanceType {

    //region Members
    String  instanceType;
    Integer executors;
    //endregion

    //region Public Methods
    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public void setExecutors(Integer executors) {
        this.executors = executors;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public Integer getExecutors() {
        return executors;
    }

    //endregion
}
