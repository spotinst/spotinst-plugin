package hudson.plugins.spotinst.model.aws;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AwsInstanceType {

    //region Members
    String instanceType;
    Integer cpus;
    //endregion

    //region Public Methods
    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public void setCpus(Integer cpus) {
        this.cpus = cpus;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public Integer getCpus() {
        return cpus;
    }

    //endregion
}
