package com.example.addon.systems;

public enum ActionType {
    COMMAND("Command"),
    PACKET("Packet"),
    WAIT("Wait"),
    CLOSE_GUI("Close Gui");

    private final String title;

    ActionType(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
