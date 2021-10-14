package hudson.plugins.spotinst.common;



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