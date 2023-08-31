package hudson.plugins.spotinst.model.aws;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sitay on 30/08/2013.
 */
public class AwsStatefulInstancesManager {
    //region members
    private final static Map<String, Map<String, AwsStatefulInstance>> awsStatefulInstanceBySsiById =
            new ConcurrentHashMap<>();
    //endregion

    //region methods
    public static AwsStatefulInstance getStatefulInstanceBySSi(String ssi){
        AwsStatefulInstance retVal = null;
        Collection<Map<String, AwsStatefulInstance>> allGroupsSsiById =
                awsStatefulInstanceBySsiById.values();

        Optional<AwsStatefulInstance> optionalMatchingStatefulInstance =
                allGroupsSsiById.stream().filter(groupSsiById -> groupSsiById.containsKey(ssi))
                                .map(groupSsiById -> groupSsiById.get(ssi)).findFirst();

        if (optionalMatchingStatefulInstance.isPresent()) {
            retVal = optionalMatchingStatefulInstance.get();
        }

        return retVal;
    }

    //region getters & setters
    public static Map<String, Map<String, AwsStatefulInstance>> getAwsStatefulInstanceBySsiById() {
        return awsStatefulInstanceBySsiById;
    }
    //endregion
}
