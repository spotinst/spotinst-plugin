package hudson.plugins.spotinst.repos;

import hudson.plugins.spotinst.api.infra.ApiResponse;
import hudson.plugins.spotinst.model.aws.AwsGroupInstance;
import hudson.plugins.spotinst.model.aws.AwsScaleUpResult;

import java.util.List;
import java.util.Map;

/**
 * Created by ohadmuchnik on 05/11/2018.
 */
public interface IAwsGroupRepo {
    ApiResponse<List<AwsGroupInstance>> getGroupInstances(String groupId, String accountId);

    ApiResponse<Boolean> detachInstance(String instanceId, String accountId);

    ApiResponse<AwsScaleUpResult> scaleUp(String groupId, Integer adjustment, String accountId);

    ApiResponse<Map<String,String>> getAllInstanceTypes(String accountId);

}
