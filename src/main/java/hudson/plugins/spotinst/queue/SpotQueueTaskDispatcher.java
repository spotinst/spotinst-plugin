package hudson.plugins.spotinst.queue;

import hudson.Extension;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;
import hudson.plugins.spotinst.slave.SpotinstSlave;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
public class SpotQueueTaskDispatcher extends QueueTaskDispatcher {
    //region members
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotQueueTaskDispatcher.class);
    //endregion

    //region override methods
    @Override
    public CauseOfBlockage canTake(Node node, Queue.Task task) {
        CauseOfBlockage retVal                         = null;
        boolean         isTaskReservedForStatefulSlave = task instanceof StatefulInterruptedTask;

        if (isTaskReservedForStatefulSlave) {
            StatefulInterruptedTask statefulInterruptedTask = (StatefulInterruptedTask) task;

            if (node instanceof SpotinstSlave) {
                SpotinstSlave slave           = (SpotinstSlave) node;
                String        ssi             = slave.getSsiId();
                boolean       isStatefulSlave = StringUtils.isNotEmpty(ssi);

                if (isStatefulSlave) {
                    boolean canNodeTakeTask = ssi.equals(statefulInterruptedTask.getSsi());

                    if (canNodeTakeTask) {
                        LOGGER.info("node {} with ssi {} can take task {}", slave.getNodeName(), ssi,
                                    statefulInterruptedTask);
                    }
                    else {
                        retVal = new InconsistentSsiCauseOfBlockage();
                    }
                }
                else {
                    retVal = new InconsistentSsiCauseOfBlockage();
                }
            }
            else {
                retVal = new InconsistentSsiCauseOfBlockage();
            }
        }

        return retVal;
    }
    //endregion

    //region classes
    private static class InconsistentSsiCauseOfBlockage extends CauseOfBlockage {
        @Override
        public String getShortDescription() {
            return "task reserved for slave with specific ssi";
        }
    }
    //endregion
}
