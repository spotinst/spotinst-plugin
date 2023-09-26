package hudson.plugins.spotinst.common.stateful;

import hudson.model.Executor;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.plugins.spotinst.model.aws.stateful.AwsStatefulInstance;
import hudson.plugins.spotinst.model.common.BaseStatefulInstance;
import hudson.plugins.spotinst.slave.SpotinstSlave;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StatefulInstanceManager {
    //region members
    private static final Logger                                            LOGGER                       =
            LoggerFactory.getLogger(StatefulInstanceManager.class);
    private static final Map<String, List<? extends BaseStatefulInstance>> statefulInstanceIdsByGroupId =
            new ConcurrentHashMap<>();
    private static final Map<String, String>                               ssiByStatefulTask            =
            new ConcurrentHashMap<>();
    private static final Set<String>                                       statefulTasksToReTrigger     =
            new HashSet<>();
    //endregion

    //region methods
    public static CauseOfBlockage canNodeTakeStatefulTask(Node node, String statefulTaskSsi) {
        CauseOfBlockage retVal = null;
        Optional<List<? extends BaseStatefulInstance>> optionalMatchingGroupStatefulInstances =
                statefulInstanceIdsByGroupId.values().stream().filter(statefulInstances -> statefulInstances.stream()
                                                                                                            .anyMatch(
                                                                                                                    statefulInstance -> statefulInstance.getId()
                                                                                                                                                        .equals(statefulTaskSsi)))
                                            .findFirst();
        boolean isStatefulInstanceExist = optionalMatchingGroupStatefulInstances.isPresent();

        if (isStatefulInstanceExist) {

            Optional<? extends BaseStatefulInstance> optionalMatchingStatefulInstance =
                    optionalMatchingGroupStatefulInstances.get().stream()
                                                          .filter(statefulInstance -> statefulInstance.getId()
                                                                                                      .equals(statefulTaskSsi))
                                                          .findFirst();

            if (optionalMatchingStatefulInstance.isPresent()) {
                retVal = canStatefulInstanceTakeStatefulTask(node, optionalMatchingStatefulInstance.get(),
                                                             statefulTaskSsi);
            }
        }
        else {
            LOGGER.info(
                    "SSI '{}' does not exist any more. reserved tasks for the stateful instance can be taken by any node",
                    statefulTaskSsi);
        }

        return retVal;
    }

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

    public static void handleReTriggeredStatefulTask(Queue.Task task, Executor executor) {
        String  key = generateKey(task, executor);
        statefulTasksToReTrigger.remove(key);
    }
    //endregion

    //region private methods
    private static String generateKey(Queue.Task task, Executor executor) {
        return String.format("%s_%s", task, executor.getId());
    }

    private static CauseOfBlockage canStatefulInstanceTakeStatefulTask(Node node, BaseStatefulInstance statefulInstance,
                                                                       String statefulTaskSsi) {
        CauseOfBlockage retVal;

        if (statefulInstance instanceof AwsStatefulInstance) {
            retVal = canAwsStatefulInstanceTakeStatefulTask(node, (AwsStatefulInstance) statefulInstance,
                                                            statefulTaskSsi);
        }
        else {
            LOGGER.warn("stateful instance of unsupported cloud '{}'", statefulTaskSsi);
            retVal = new InconsistentSsiCauseOfBlockage();
        }

        return retVal;
    }

    private static CauseOfBlockage canAwsStatefulInstanceTakeStatefulTask(Node node,
                                                                          AwsStatefulInstance statefulInstance,
                                                                          String statefulTaskSsi) {
        CauseOfBlockage              retVal   = null;
        AwsStatefulInstanceStateEnum ssiState = statefulInstance.getState();

        switch (ssiState) {
            case ACTIVE:
                boolean isNodeMatchingTaskSsi = checkMatchingSsis(node, statefulTaskSsi);

                if (isNodeMatchingTaskSsi) {
                    LOGGER.info("found slave for stateful task with SSI '{}'", statefulTaskSsi);
                }
                else {
                    retVal = new InconsistentSsiCauseOfBlockage();
                }

                break;

            case RECYCLE:
            case RECYCLING:
            case RESUME:
            case RESUMING:
                LOGGER.info("stateful task awaits for SSI {} to be active. no node can take it", statefulTaskSsi);
                retVal = new AwaitingSsiCauseOfBlockage();
                break;

            case PAUSE:
            case PAUSING:
            case PAUSED:
            case DEALLOCATE:
            case DEALLOCATING:
            case DEALLOCATED:
            case ERROR:
                LOGGER.info("SSI {} is in state {} and isn't activated, stateful task can run on any node",
                            statefulTaskSsi, ssiState);
                break;
        }

        return retVal;
    }

    private static Boolean checkMatchingSsis(Node node, String statefulTaskSsi) {
        boolean retVal = false;

        if (node instanceof SpotinstSlave) {
            SpotinstSlave slave           = (SpotinstSlave) node;
            String        ssi             = slave.getSsiId();
            boolean       isStatefulSlave = StringUtils.isNotEmpty(ssi);

            if (isStatefulSlave) {
                retVal = ssi.equals(statefulTaskSsi);
            }
        }

        return retVal;
    }

    //endregion

    //region getters & setters
    public static Map<String, List<? extends BaseStatefulInstance>> getStatefulInstanceIdsByGroupId() {
        return statefulInstanceIdsByGroupId;
    }
    //endregion

    //region classes
    private static class InconsistentSsiCauseOfBlockage extends CauseOfBlockage {
        @Override
        public String getShortDescription() {
            return "task reserved for slave with ssi different from current node";
        }
    }

    private static class AwaitingSsiCauseOfBlockage extends CauseOfBlockage {
        @Override
        public String getShortDescription() {
            return "awaiting for stateful node to recover";
        }
    }
    //endregion
}
