package hudson.plugins.spotinst.slave;

import hudson.model.*;
import hudson.model.Queue;
import hudson.model.queue.ScheduleResult;
import hudson.model.queue.SubTask;
import hudson.plugins.spotinst.common.stateful.StatefulInstanceManager;
import hudson.slaves.SlaveComputer;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Created by Shibel Karmi Mansour on 31/12/2020.*
 * A helper class for handling common callbacks (like afterDisconnect) for different types of
 * ComputerLaunchers.
 */
class SpotLauncherHelper {
    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotLauncherHelper.class);
    private static final Object lock   = new Object();
    //endregion

    //region Methods
    static void handleDisconnect(final SlaveComputer computer, Boolean shouldRetriggerBuilds) {

        shouldRetriggerBuilds = resolveShouldRetriggerBuilds(shouldRetriggerBuilds);

        // according to jenkins docs could be null in edge cases, check ComputerLauncher.afterDisconnect
        if (computer != null && computer.isOffline() && computer instanceof SpotinstComputer) {
            SpotinstComputer spotinstComputer = (SpotinstComputer) computer;
            SpotinstSlave    slave            = spotinstComputer.getNode();

            if (shouldRetriggerBuilds && (slave == null || BooleanUtils.isFalse(slave.isSlavePending()))) {
                LOGGER.info(String.format("Start retriggering executors for %s", spotinstComputer.getDisplayName()));

                final List<Executor> executors = spotinstComputer.getExecutors();

                for (Executor executor : executors) {
                    final Queue.Executable executable = executor.getCurrentExecutable();

                    if (executable != null) {
                        executor.interrupt(Result.ABORTED);

                        final SubTask    subTask = executable.getParent();
                        final Queue.Task task    = subTask.getOwnerTask();

                        List<Action> actions = new LinkedList<>();

                        if (executable instanceof Actionable) {
                            actions = ((Actionable) executable).getActions();
                        }

                        synchronized (lock) {
                            String         ssiByTaskName = StatefulInstanceManager.getSsiByTask(task, executor);
                            ScheduleResult scheduleResult;

                            if (ssiByTaskName != null) {
                                String statefulInterruptedTask =
                                        StatefulInstanceManager.getReTriggeringStatefulTaskByTask(task);

                                if (statefulInterruptedTask == null) {
                                    statefulInterruptedTask = ssiByTaskName;
                                    StatefulInstanceManager.handleReTriggeringStatefulTaskByTask(task,
                                                                                                 statefulInterruptedTask);
                                }

                                LOGGER.info(String.format("RETRIGGERING Stateful Task: %s - WITH ACTIONS: %s on SSI %s",
                                                          statefulInterruptedTask, actions, ssiByTaskName));
                                scheduleResult = Queue.getInstance().schedule2(task, 10, actions);
                            }
                            else {
                                LOGGER.info(String.format("RETRIGGERING: %s - WITH ACTIONS: %s", task, actions));
                                scheduleResult = Queue.getInstance().schedule2(task, 10, actions);
                            }

                            if (scheduleResult.isRefused()) {
                                LOGGER.info("task '{}' refused", task);
                            }
                            if (scheduleResult.isAccepted()) {
                                LOGGER.info("task '{}' accepted", task);
                            }
                            if (scheduleResult.isCreated()) {
                                LOGGER.info("task '{}' created", task);
                            }

                            Queue.Item item = scheduleResult.getItem(), createdItem = scheduleResult.getCreateItem();
                            LOGGER.info("interrupted task : '{}'. item task '{}', created item task '{}'", task,
                                        item == null ? null : item.task, createdItem == null ? null : createdItem.task);

                        }
                    }
                }

                LOGGER.info(String.format("Finished retriggering executors for %s", spotinstComputer.getDisplayName()));
            }
            else if (BooleanUtils.isFalse(shouldRetriggerBuilds)) {
                LOGGER.info(String.format("Retrigger Build disabled for %s, not retriggering executors",
                                          spotinstComputer.getDisplayName()));
            }
        }
        else if (computer != null) {
            LOGGER.info(
                    String.format("Skipping executable resubmission for %s - offline: %s", computer.getDisplayName(),
                                  computer.isOffline()));
        }
        else {
            LOGGER.info("Skipping executable resubmission, computer is null");
        }
    }

    /**
     * shouldRetriggerBuilds was introduced after some clouds have already been set-up, so until those are saved
     * again (only then Jenkins calls their constructor), we need to set the default behaviour for them.
     */
    private static Boolean resolveShouldRetriggerBuilds(Boolean shouldRetriggerBuilds) {
        if (shouldRetriggerBuilds == null) {
            shouldRetriggerBuilds = true;
        }

        return shouldRetriggerBuilds;
    }
    //endregion
}
