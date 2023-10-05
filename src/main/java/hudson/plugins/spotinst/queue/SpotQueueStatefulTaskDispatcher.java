package hudson.plugins.spotinst.queue;

import hudson.Extension;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;
import hudson.plugins.spotinst.common.stateful.StatefulInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;


/**
 * Created by sitay on 30/08/2013.
 */
@Extension
public class SpotQueueStatefulTaskDispatcher extends QueueTaskDispatcher {
    //region members
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotQueueStatefulTaskDispatcher.class);
    //endregion

    //region override methods
    @Override
    @CheckForNull
    public CauseOfBlockage canTake(Node node, Queue.BuildableItem item) {
        CauseOfBlockage retVal                         = null;
        Queue.Task      task                           = item.task;
        String          ssiForReTriggeringTask         =
                StatefulInstanceManager.getReTriggeringStatefulTaskByTask(task);
        boolean         isTaskReservedForStatefulSlave = ssiForReTriggeringTask != null;

        if (isTaskReservedForStatefulSlave) {
            LOGGER.info("can node {} take item {}", node, item);//TODO: remove
            retVal = StatefulInstanceManager.canNodeTakeStatefulTask(node, ssiForReTriggeringTask);
        }

        return retVal;
    }
    //endregion
}
