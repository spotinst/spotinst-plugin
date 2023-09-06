package hudson.plugins.spotinst.model.aws;

import hudson.model.Executor;
import hudson.model.Queue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sitay on 30/08/2013.
 */
public class AwsStatefulInstancesManager {
    //region members
    private static final Map<String, String>                           ssiByStatefulTask                 =
            new ConcurrentHashMap<>();
    private static final Map<String, Map<String, AwsStatefulInstance>> awsStatefulInstanceBySsiByGroupId =
            new ConcurrentHashMap<>();
    private static final Set<String>                                   statefulTasksToReTrigger          =
            new HashSet<>();
    //endregion

    //region methods
    public static void putSsiByTask(Queue.Task task, Executor executor, String ssiId) {
        String key = generateKey(task, executor);
        ssiByStatefulTask.put(key, ssiId);
    }

    public static String removeSsiByTask(Queue.Task task, Executor executor) {
        String retVal;
        String key = generateKey(task, executor);
        retVal = ssiByStatefulTask.remove(key);
        return retVal;
    }

    public static AwsStatefulInstance getStatefulInstanceBySSi(String ssi) {
        AwsStatefulInstance                          retVal           = null;
        Collection<Map<String, AwsStatefulInstance>> allGroupsSsiById = awsStatefulInstanceBySsiByGroupId.values();

        Optional<AwsStatefulInstance> optionalMatchingStatefulInstance =
                allGroupsSsiById.stream().filter(groupSsiById -> groupSsiById.containsKey(ssi))
                                .map(groupSsiById -> groupSsiById.get(ssi)).findFirst();

        if (optionalMatchingStatefulInstance.isPresent()) {
            retVal = optionalMatchingStatefulInstance.get();
        }

        return retVal;
    }


    public static Boolean isReTriggeringStatefulTask(Queue.Task task, Executor executor) {
        boolean retVal;
        String  key = generateKey(task, executor);
        retVal = statefulTasksToReTrigger.contains(key);
        return retVal;
    }

    public static void handleReTriggeringStatefulTask(Queue.Task task, Executor executor) {
        String key = generateKey(task, executor);
        statefulTasksToReTrigger.add(key);
    }

    public static Boolean handleReTriggeredStatefulTask(Queue.Task task, Executor executor) {
        boolean retVal;
        String  key = generateKey(task, executor);
        retVal = statefulTasksToReTrigger.remove(key);
        return retVal;
    }
    //endregion

    //region private methods
    private static String generateKey(Queue.Task task, Executor executor) {
        return String.format("%s_%s", task, executor.getId());
    }
    //endregion

    //region getters & setters
    public static Map<String, Map<String, AwsStatefulInstance>> getAwsStatefulInstanceBySsiByGroupId() {
        return awsStatefulInstanceBySsiByGroupId;
    }
    //endregion
}
