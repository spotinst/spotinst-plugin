package hudson.plugins.spotinst.queue;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.plugins.spotinst.slave.SpotinstSlave;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.Optional;

@Extension
public class StatefulQueueDecisionHandler extends Queue.QueueDecisionHandler {
    //region override method
    @Override
    public boolean shouldSchedule(Queue.Task task, List<Action> actions) {
        boolean retVal = true;

        if (task instanceof StatefulInterruptedTask) {
            StatefulInterruptedTask statefulInterruptedTask = (StatefulInterruptedTask) task;
            String                  ssi                     = statefulInterruptedTask.getSsi();
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
                else {
                    retVal = false;
                }
            }
        }

        return retVal;
    }
    //endregion
}
