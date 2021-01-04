package hudson.plugins.spotinst.common;

/**
 * Created by Shibel Karmi Mansour on 30/12/2021.
 */
public enum ConnectionMethodEnum {
    SSH_OR_COMMAND("SSH or Command on Master"),
    JNLP("JNLP");

    private String name;

    ConnectionMethodEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ConnectionMethodEnum fromName(String name) {
        ConnectionMethodEnum retVal = null;
        for (ConnectionMethodEnum usageEnum : ConnectionMethodEnum.values()) {
            if (usageEnum.name.equals(name)) {
                retVal = usageEnum;
                break;
            }
        }

        return retVal;
    }

}
