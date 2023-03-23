package ru.proton.service.enums;

public enum ServiceCommand {
    HELP("/help"),
    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("/start");
    private final String cmd;


    ServiceCommand(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public String toString() {
        return cmd;
    }


    public static ServiceCommand fromValue(String value) {
        for (ServiceCommand command: ServiceCommand.values()) {
            if (command.cmd.equals(value)) {
                return command;
            }
        }
        return null;
    }
}
