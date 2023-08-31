package hudson.plugins.spotinst.queue;

import hudson.Extension;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;
import hudson.plugins.spotinst.model.aws.AwsStatefulInstance;
import hudson.plugins.spotinst.model.aws.AwsStatefulInstancesManager;
import hudson.plugins.spotinst.slave.SpotinstSlave;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by sitay on 30/08/2013.
 */
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
            String                  statefulTaskSsi         = statefulInterruptedTask.getSsi();
            AwsStatefulInstance matchingStatefulInstance =
                    AwsStatefulInstancesManager.getStatefulInstanceBySSi(statefulTaskSsi);
            boolean isSsiExist = matchingStatefulInstance != null;

            if (isSsiExist) {
                switch (matchingStatefulInstance.getState()) {
                    case PAUSE:
                    case PAUSING:
                    case PAUSED:
                    case DEALLOCATE:
                    case DEALLOCATING:
                    case DEALLOCATED:
                    case ERROR:
                        //SSI isn't activated, can run on any node
                        break;

                    case ACTIVE:
                        //can only run on specific SSI
                        retVal = checkMatchingSsis(node, statefulTaskSsi);
                        break;

                    case RECYCLE:
                    case RECYCLING:
                    case RESUME:
                    case RESUMING:
                    default:
                        //await for SSI to be active
                        retVal = new AwaitingSsiCauseOfBlockage();
                        break;
                }
            }
        }

        return retVal;
    }
    //endregion

    //region private methods
    private CauseOfBlockage checkMatchingSsis(Node node, String statefulTaskSsi) {
        CauseOfBlockage retVal = null;

        if (node instanceof SpotinstSlave) {
            SpotinstSlave slave           = (SpotinstSlave) node;
            String        ssi             = slave.getSsiId();
            boolean       isStatefulSlave = StringUtils.isNotEmpty(ssi);

            if (isStatefulSlave) {
                boolean canNodeTakeTask = ssi.equals(statefulTaskSsi);

                if (canNodeTakeTask) {
                    LOGGER.info("node {} with ssi {} can take task", slave.getNodeName(), ssi);
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

        return retVal;
    }

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
