package hudson.plugins.spotinst.repos;

import hudson.plugins.spotinst.api.SpotinstApi;
import hudson.plugins.spotinst.api.infra.ApiException;
import hudson.plugins.spotinst.api.infra.ApiResponse;
import hudson.plugins.spotinst.api.infra.ExceptionHelper;
import hudson.plugins.spotinst.model.azure.AzureV3GroupStatus;
import hudson.plugins.spotinst.model.azure.AzureV3VM;

import java.util.List;

/**
 * Created by Shibel Karmi Mansour on 01/12/2020.
 */
public class AzureV3GroupRepo implements IAzureV3GroupRepo {

    @Override
    public ApiResponse<List<AzureV3VM>> getGroupInstances(String groupId, String accountId) {
        ApiResponse<List<AzureV3VM>> retVal;

        try {
            AzureV3GroupStatus groupStatus = SpotinstApi.getAzureV3GroupStatus(groupId, accountId);
            List<AzureV3VM>    vms         = groupStatus.getVms();
            retVal = new ApiResponse<>(vms);
        }
        catch (ApiException e) {
            retVal = ExceptionHelper.handleDalException(e);
        }

        return retVal;
    }

    @Override
    public ApiResponse<Boolean> detachVM(String groupId, String vmId, String accountId) {
        ApiResponse<Boolean> retVal;

        try {
            Boolean isDetached = SpotinstApi.azureV3DetachVM(groupId, vmId, accountId);

            retVal = new ApiResponse<>(isDetached);

        }
        catch (ApiException e) {
            retVal = ExceptionHelper.handleDalException(e);
        }

        return retVal;
    }

    @Override
    public ApiResponse<Boolean> scaleUp(String groupId, Integer adjustment, String accountId) {
        ApiResponse<Boolean> retVal;

        try {
            Boolean didSucceed = SpotinstApi.azureV3ScaleUp(groupId, adjustment, accountId);

            retVal = new ApiResponse<>(didSucceed);

        }
        catch (ApiException e) {
            retVal = ExceptionHelper.handleDalException(e);
        }

        return retVal;
    }
}
