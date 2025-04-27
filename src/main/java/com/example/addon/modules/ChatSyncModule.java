package com.example.addon.modules;

import com.example.addon.CinnaUtil;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatSyncModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Role> roleSetting = sgGeneral.add(new EnumSetting.Builder<Role>()
        .name("role")
        .description("Whether this instance is the parent or child")
        .defaultValue(Role.PARENT)
        .build()
    );

    private final Setting<Integer> portSetting = sgGeneral.add(new IntSetting.Builder()
        .name("port")
        .description("Port to use for socket connection")
        .defaultValue(25556)
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

    private final Setting<String> messageSetting = sgGeneral.add(new StringSetting.Builder()
        .name("message")
        .description("Message to send")
        .defaultValue("Hello!")
        .visible(() -> roleSetting.get() == Role.PARENT)
        .build()
    );

    private final Setting<Integer> delaySetting = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay before sending message (ms)")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 5000)
        .visible(() -> roleSetting.get() == Role.PARENT)
        .build()
    );

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected;

    public ChatSyncModule() {
        super(CinnaUtil.CATEGORY, "chat-sync", "Send chat messages on two instances simultaneously");
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

    public void sendMessage() {
        if (!isConnected) {
            error("Not connected!");
            return;
        }
        if (roleSetting.get() == Role.CHILD) {
            error("Only parent can send messages!");
            return;
        }

        String message = messageSetting.get();
        out.println("MSG:" + message);

        new Thread(() -> {
            try {
                Thread.sleep(delaySetting.get());
                mc.execute(() -> ChatUtils.sendPlayerMsg(message));
            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(portSetting.get());
                info("Waiting for child connection on port " + portSetting.get());
                clientSocket = serverSocket.accept();
                setupConnection();
                info("Child connected!");
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
                    if (message.startsWith("MSG:")) {
                        String msg = message.substring(4);
                        mc.execute(() -> ChatUtils.sendPlayerMsg(msg));
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
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            error("Error closing connection: " + e.getMessage());
        }
    }

    public enum Role {
        PARENT,
        CHILD
    }
}
