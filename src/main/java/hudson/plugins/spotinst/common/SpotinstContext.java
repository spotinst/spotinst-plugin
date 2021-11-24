package hudson.plugins.spotinst.common;

import hudson.plugins.spotinst.model.aws.AwsInstanceType;

import java.util.List;

/**
 * Created by ohadmuchnik on 24/05/2016.
 */
public class SpotinstContext {

    //region Members
    private static SpotinstContext       instance;
    private        String                spotinstToken;
    private        String                accountId;
    private        List<AwsInstanceType> awsInstanceTypes;
    //endregion

    public static SpotinstContext getInstance() {
        if (instance == null) {
            instance = new SpotinstContext();
        }
        return instance;
    }

    //region Public Methods
    public String getSpotinstToken() {
        return spotinstToken;
    }

    public void setSpotinstToken(String spotinstToken) {
        this.spotinstToken = spotinstToken;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public List<AwsInstanceType> getAwsInstanceTypes() {
        return awsInstanceTypes;
    }

    public void setAwsInstanceTypes(List<AwsInstanceType> awsInstanceTypes) {
        this.awsInstanceTypes = awsInstanceTypes;
    }
    //endregion
}
