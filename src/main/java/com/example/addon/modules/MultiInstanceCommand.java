package com.example.addon.modules;

import com.example.addon.CinnaUtil;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MultiInstanceCommand extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgCommands = settings.createGroup("Commands");

    private final Setting<Role> roleSetting = sgGeneral.add(new EnumSetting.Builder<Role>()
        .name("role")
        .description("Whether this instance is the parent or child")
        .defaultValue(Role.PARENT)
        .build()
    );

    private final Setting<Integer> portSetting = sgGeneral.add(new IntSetting.Builder()
        .name("port")
        .description("Port to use for socket connection")
        .defaultValue(25555)
        .range(1024, 65535)
        .sliderRange(1024, 65535)
        .build()
    );

    private final Setting<String> hostSetting = sgGeneral.add(new StringSetting.Builder()
        .name("host")
        .description("Host to connect to (child mode only)")
        .defaultValue("localhost")
        .visible(() -> roleSetting.get() == Role.CHILD)
        .build()
    );

    private final Setting<Integer> delaySetting = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay between commands in milliseconds")
        .defaultValue(1000)
        .min(0)
        .sliderRange(0, 5000)
        .build()
    );

    private final Setting<List<String>> commandsSetting = sgCommands.add(new StringListSetting.Builder()
        .name("commands")
        .description("Commands to execute")
        .defaultValue(new ArrayList<>())
        .visible(() -> roleSetting.get() == Role.PARENT)
        .build()
    );

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected;
    private int currentCommandIndex;
    private boolean isExecuting;
    private long lastExecuteTime;

    public MultiInstanceCommand() {
        super(CinnaUtil.CATEGORY, "multi-instance-command", "Execute commands on two instances simultaneously");
    }

    @Override
    public void onActivate() {
        if (roleSetting.get() == Role.PARENT) {
            startServer();
        } else {
            startClient();
        }
    }

    @Override
    public void onDeactivate() {
        closeConnection();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(portSetting.get());
                info("Waiting for child connection on port " + portSetting.get());
                clientSocket = serverSocket.accept();
                setupConnection();
                info("Child connected!");

                String message;
                while (isConnected && (message = in.readLine()) != null) {
                    if (message.equals("DONE")) {
                        currentCommandIndex++;
                    }
                }
            } catch (IOException e) {
                if (isConnected) error("Server error: " + e.getMessage());
            }
        }).start();
    }

    private void startClient() {
        new Thread(() -> {
            try {
                clientSocket = new Socket(hostSetting.get(), portSetting.get());
                setupConnection();
                info("Connected to parent!");

                String message;
                while (isConnected && (message = in.readLine()) != null) {
                    if (message.startsWith("CMD:")) {
                        String cmd = message.substring(4);
                        mc.execute(() -> ChatUtils.sendPlayerMsg(cmd));
                        out.println("DONE");
                    }
                }
            } catch (IOException e) {
                if (isConnected) error("Client error: " + e.getMessage());
            }
        }).start();
    }

    private void setupConnection() throws IOException {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        isConnected = true;
    }

    private void closeConnection() {
        isConnected = false;
        isExecuting = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            error("Error closing connection: " + e.getMessage());
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!isConnected || !isExecuting || roleSetting.get() != Role.PARENT) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastExecuteTime < delaySetting.get()) return;

        if (currentCommandIndex < commandsSetting.get().size()) {
            String cmd = commandsSetting.get().get(currentCommandIndex);
            mc.execute(() -> ChatUtils.sendPlayerMsg(cmd));
            out.println("CMD:" + cmd);
            lastExecuteTime = currentTime;
        } else {
            isExecuting = false;
            info("Finished executing commands");
        }
    }

    public void startCommandExecution() {
        if (!isConnected) {
            error("Not connected!");
            return;
        }
        if (roleSetting.get() == Role.CHILD) {
            error("Only parent can start execution!");
            return;
        }
        isExecuting = true;
        currentCommandIndex = 0;
        lastExecuteTime = 0;
    }

    public void stopCommandExecution() {
        isExecuting = false;
    }

    public enum Role {
        PARENT,
        CHILD
    }
}
