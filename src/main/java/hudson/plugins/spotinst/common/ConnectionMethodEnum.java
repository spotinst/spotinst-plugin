package hudson.plugins.spotinst.common;

/**
 * Created by Shibel Karmi Mansour on 30/12/2021.
 */
public enum ConnectionMethodEnum {
    //todo x shibel - what 'Command on Master' means?
    // The ComputerConnector provided by the plugin we rely on also supports launching an agent
    // by running a command on the master. Most examples I've seen just run an ssh command.
    SSH_OR_COMMAND("SSH or Command on Master"),
    JNLP("JNLP");

    private String name;

    ConnectionMethodEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    //todo x shibel - remove?
    // shibel done.

}
