package hudson.plugins.spotinst.common;


/**
 * Created by Liron Arad on 14/10/2021.
 */
public enum TerminationStrategyEnum {
    IdleTerminationMinutes("IdleTerminationMinutes"),
    TerminateAfterExecution("TerminateAfterExecution");

    private String name;

    TerminationStrategyEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}