package hudson.plugins.spotinst.queue;

import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.ResourceList;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.SubTask;
import org.acegisecurity.Authentication;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;

public class StatefulInterruptedTask implements Queue.Task {
    //region members
    @Nonnull
    private final        String     ssi;
    @Nonnull
    private final        Queue.Task task;
    //endregion

    //region ctor
    public StatefulInterruptedTask(@Nonnull String ssi, @Nonnull Queue.Task task) {
        this.ssi = ssi;

        if(task instanceof StatefulInterruptedTask){
            this.task = ((StatefulInterruptedTask) task).getTask();
        }
        else {
            this.task = task;
        }
    }
    //endregion

    //region overrides
    @Override
    public String getName() {
        return task.getName();
    }

    @Override
    public ResourceList getResourceList() {
        return task.getResourceList();
    }

    @Override
    public String getDisplayName() {
        return task.getDisplayName();
    }

    @Override
    public String getFullDisplayName() {
        return task.getFullDisplayName();
    }

    @Override
    public String getUrl() {
        return task.getUrl();
    }

    @Override
    public boolean isConcurrentBuild() {
        return task.isConcurrentBuild();
    }

    @Override
    public Collection<? extends SubTask> getSubTasks() {
        return task.getSubTasks();
    }

    @Override
    public String getAffinityKey() {
        return task.getAffinityKey();
    }

    @Override
    public void checkAbortPermission() {
        task.checkAbortPermission();
    }

    @Override
    public CauseOfBlockage getCauseOfBlockage() {
        return task.getCauseOfBlockage();
    }

    @Override
    public boolean isBuildBlocked() {
        return task.isBuildBlocked();
    }

    @Override
    public String getWhyBlocked() {
        return task.getWhyBlocked();
    }

    @Nonnull
    @Override
    public Authentication getDefaultAuthentication() {
        return task.getDefaultAuthentication();
    }

    @Nonnull
    @Override
    public Authentication getDefaultAuthentication(Queue.Item item) {
        return task.getDefaultAuthentication(item);
    }

    @Override
    public boolean hasAbortPermission() {
        return task.hasAbortPermission();
    }


    @Override
    public Label getAssignedLabel() {
        return task.getAssignedLabel();
    }

    @Override
    public Node getLastBuiltOn() {
        return task.getLastBuiltOn();
    }

    @Override
    public long getEstimatedDuration() {
        return task.getEstimatedDuration();
    }

    @CheckForNull
    @Override
    public Queue.Executable createExecutable() throws IOException {
        return task.createExecutable();
    }

    @Nonnull
    @Override
    public Queue.Task getOwnerTask() {
        return task.getOwnerTask();
    }

    @Override
    public Object getSameNodeConstraint() {
        return task.getSameNodeConstraint();
    }
    //endregion

    //region getters & setters
    @Nonnull
    public Queue.Task getTask() {
        return task;
    }

    @Nonnull
    public String getSsi() {
        return ssi;
    }
    //endregion
}
