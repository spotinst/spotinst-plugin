package hudson.plugins.spotinst.queue;

import hudson.model.Queue;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;

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
            this.task = ((StatefulInterruptedTask) task).task;
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
    public String getFullDisplayName() {
        return task.getFullDisplayName();
    }

    @Override
    public void checkAbortPermission() {
        task.checkAbortPermission();
    }

    @Override
    public boolean hasAbortPermission() {
        return task.hasAbortPermission();
    }

    @Override
    public String getUrl() {
        return task.getUrl();
    }

    @Override
    public String getDisplayName() {
        return task.getDisplayName();
    }

    @CheckForNull
    @Override
    public Queue.Executable createExecutable() throws IOException {
        return task.createExecutable();
    }
    //endregion

    //region getters & setters
    @Nonnull
    public String getSsi() {
        return ssi;
    }
    //endregion
}
