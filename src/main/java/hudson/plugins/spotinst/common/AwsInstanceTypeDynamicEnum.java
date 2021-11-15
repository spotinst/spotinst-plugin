package hudson.plugins.spotinst.common;

import hudson.plugins.spotinst.model.aws.AwsInstanceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;


/**
 * Created by Liron Arad on 07/11/21.
 */
public class AwsInstanceTypeDynamicEnum {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsInstanceTypeDynamicEnum.class);

    private static List<AwsInstanceType> values;

    public static List<AwsInstanceType> getValues() {
        return values;
    }

    public static void setValues(List<AwsInstanceType> instanceTypes) {
        AwsInstanceTypeDynamicEnum.values = instanceTypes;
    }

    public static AwsInstanceType fromName(String name) {
        AwsInstanceType retVal = null;

        for (AwsInstanceType instanceTypeEnum : AwsInstanceTypeDynamicEnum.getValues()) {
            if (Objects.equals(name, instanceTypeEnum.getInstanceType())) {
                retVal = instanceTypeEnum;
                break;
            }
        }

        if (retVal == null) {
            LOGGER.error("Tried to create Instance type enum for: " + name + ", but we don't support such type ");
        }
        return retVal;
    }
}