package hudson.plugins.spotinst.model.aws;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AwsInstanceType {

    //region Members
    String  instanceType;
    Integer vCPU;
    //endregion

    //region Public Methods
    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public void setvCPU(Integer vCPU) {
        this.vCPU = vCPU;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public Integer getVCPU() {
        return vCPU;
    }

    //endregion
}
