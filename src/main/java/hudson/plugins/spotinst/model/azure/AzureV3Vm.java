package hudson.plugins.spotinst.model.azure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

/**
 * Created by Shibel Karmi Mansour on 01/12/2020.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AzureV3Vm {
    //region members
    private String vmName;
    private String region;
    private String vmSize;
    private String lifeCycle;
    private String product;
    private String privateIp;
    private String publicIp;
    private String provisioningState;
    private String powerState;
    private Date   createdAt;
    //endregion

    //region getters & setters
    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getVmSize() {
        return vmSize;
    }

    public void setVmSize(String vmSize) {
        this.vmSize = vmSize;
    }

    public String getLifeCycle() {
        return lifeCycle;
    }

    public void setLifeCycle(String lifeCycle) {
        this.lifeCycle = lifeCycle;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public String getProvisioningState() {
        return provisioningState;
    }

    public void setProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
    }

    public String getPowerState() {
        return powerState;
    }

    public void setPowerState(String powerState) {
        this.powerState = powerState;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    //endregion
}
