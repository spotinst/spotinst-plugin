package hudson.plugins.spotinst.queue;

import hudson.model.Executor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SsiByTaskMapper {
    //region members
    private static final Map<String, String> ssiByStatefulTask = new ConcurrentHashMap<>();
    //endregion

    //region methods
    public static void putSsiByTask(String taskName, Executor executor, String ssiId){
        String key = generateKey(taskName, executor);
        ssiByStatefulTask.put(key, ssiId);
    }

    public static String removeSsiByTask(String taskName, Executor executor){
        String retVal;
        String key = generateKey(taskName, executor);
        retVal = ssiByStatefulTask.remove(key);
        return retVal;
    }
    //endregion

    //region private methods
    private static String generateKey(String taskName, Executor executor){
        return String.format("%s_%s", taskName, executor.getId());
    }
}
