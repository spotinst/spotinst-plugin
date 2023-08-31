package hudson.plugins.spotinst.queue;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.plugins.spotinst.model.aws.AwsStatefulInstance;
import hudson.plugins.spotinst.model.aws.AwsStatefulInstancesManager;
import hudson.plugins.spotinst.slave.SpotinstSlave;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.Optional;

/**
 * Created by sitay on 30/08/2013.
 */
@Extension
public class StatefulQueueDecisionHandler extends Queue.QueueDecisionHandler {
    //region override method
    @Override
    public boolean shouldSchedule(Queue.Task task, List<Action> actions) {
        boolean retVal = true;

        if (task instanceof StatefulInterruptedTask) {
            StatefulInterruptedTask statefulInterruptedTask = (StatefulInterruptedTask) task;
            String                  ssi                     = statefulInterruptedTask.getSsi();
            AwsStatefulInstance matchingStatefulInstance = AwsStatefulInstancesManager.getStatefulInstanceBySSi(ssi);
            boolean isSsiStillExist = matchingStatefulInstance != null;

            if (isSsiStillExist) {
                switch (matchingStatefulInstance.getState()) {
                    case PAUSE:
                    case PAUSING:
                    case PAUSED:
                    case DEALLOCATE:
                    case DEALLOCATING:
                    case DEALLOCATED:
                    case ERROR:
                        //Ssi isn't activated, no need to wait for it
                        break;

                    case ACTIVE:
                        retVal = isActiveStatefulInstanceAcceptingTasks(ssi);
                        break;

                    case RECYCLE:
                    case RECYCLING:
                    case RESUME:
                    case RESUMING:
                    default:
                        //await for ssi to be active
                        retVal = false;
                        break;
                }
            }
        }

        return retVal;
    }
    //endregion

    //region private methods
    private Boolean isActiveStatefulInstanceAcceptingTasks(String ssi) {
        boolean retVal = false;

        Jenkins jenkinsInstance = Jenkins.getInstanceOrNull();

        if (jenkinsInstance != null) {
            List<Node> nodes = jenkinsInstance.getNodes();
            Optional<SpotinstSlave> optionalSsi =
                    nodes.stream().filter(node -> node instanceof SpotinstSlave).map(node -> (SpotinstSlave) node)
                         .filter(spotinstSlave -> ssi.equals(spotinstSlave.getSsiId())).findFirst();

            if (optionalSsi.isPresent()) {
                SpotinstSlave matchedSlave = optionalSsi.get();
                retVal = matchedSlave.isAcceptingTasks();
            }
        }

        return retVal;
    }
}
