package hudson.plugins.spotinst.repos;

import hudson.plugins.spotinst.api.infra.ApiResponse;
import hudson.plugins.spotinst.model.azure.AzureV3VM;

import java.util.List;

/**
 * Created by Shibel Karmi Mansour on 01/12/2020.
 */
public interface IAzureV3GroupRepo {
    ApiResponse<List<AzureV3VM>> getGroupInstances(String groupId, String accountId);

    ApiResponse<Boolean> detachVM(String groupId, String vmId, String accountId);

    ApiResponse<Boolean> scaleUp(String groupId, Integer adjustment, String accountId);
}
