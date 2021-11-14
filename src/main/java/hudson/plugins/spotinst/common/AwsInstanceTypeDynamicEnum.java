package hudson.plugins.spotinst.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Created by Liron Arad on 07/11/21.
 */
public class AwsInstanceTypeDynamicEnum {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsInstanceTypeDynamicEnum.class);

    private static List<InstanceType> values;

    public static List<InstanceType> getValues() {
        return values;
    }

    public static void setValues(Map<String,String> values) {

        List<InstanceType> instanceTypes = new LinkedList<>();
        for (Map.Entry<String,String> entry : values.entrySet()) {
            String type = entry.getKey();
            String cpus = entry.getValue();
            InstanceType instanceType = new InstanceType(type,cpus);
            instanceTypes.add(instanceType);
        }

        AwsInstanceTypeDynamicEnum.values = instanceTypes;
    }

    public static InstanceType fromName(String name) {
        InstanceType retVal = null;

        for (InstanceType instanceTypeEnum : AwsInstanceTypeDynamicEnum.getValues()) {
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

    public static class InstanceType implements Serializable {
        private String instanceType;
        private String cpus;

        public InstanceType(String name, String cpus) {
            this.instanceType = name;
            this.cpus = cpus;
        }

        public String getInstanceType() {
            return instanceType;
        }

        public String getCpus() {
            return cpus;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            InstanceType that = (InstanceType) o;

            return getInstanceType() != null ? getInstanceType().equals(that.getInstanceType()) : that.getInstanceType() == null;

        }

        @Override
        public int hashCode() {
            return getInstanceType() != null ? getInstanceType().hashCode() : 0;
        }

        @Override
        public String toString() {
            return "InstanceType{" + "name='" + instanceType + '\'' + '}';
        }
    }
}