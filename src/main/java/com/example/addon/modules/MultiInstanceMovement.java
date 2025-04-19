package com.example.addon.modules;

import com.example.addon.CinnaUtil;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiInstanceMovement extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSync = settings.createGroup("Sync");

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

    private final Setting<Integer> delaySetting = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay between syncs in milliseconds")
        .defaultValue(50)
        .min(0)
        .sliderRange(0, 500)
        .build()
    );

    private final Setting<Boolean> syncMovement = sgSync.add(new BoolSetting.Builder()
        .name("sync-movement")
        .description("Sync movement keys")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> syncRotation = sgSync.add(new BoolSetting.Builder()
        .name("sync-rotation")
        .description("Sync head rotation")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> syncInteract = sgSync.add(new BoolSetting.Builder()
        .name("sync-interact")
        .description("Sync interactions (right click)")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> syncAttack = sgSync.add(new BoolSetting.Builder()
        .name("sync-attack")
        .description("Sync attacks (left click)")
        .defaultValue(true)
        .build()
    );

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected;
    private boolean processInputs = true;
    private long lastSyncTime = 0;

    public MultiInstanceMovement() {
        super(CinnaUtil.CATEGORY, "multi-instance-movement", "Synchronized movement between two instances");
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
                info("§aWaiting for child connection on port " + portSetting.get());
                clientSocket = serverSocket.accept();
                setupConnection();
                info("§aChild connected successfully!");

                String message;
                while (isConnected && (message = in.readLine()) != null) {
                    // Handle any responses from child if needed
                }
            } catch (IOException e) {
                if (isConnected) error("§cServer error: " + e.getMessage());
            }
        }).start();
    }

    private void startClient() {
        new Thread(() -> {
            try {
                clientSocket = new Socket(hostSetting.get(), portSetting.get());
                setupConnection();
                info("§aConnected to parent!");

                while (isConnected) {
                    final String input = in.readLine();
                    if (input == null) break;

                    if (input.startsWith("INPUT:")) {
                        mc.execute(() -> {
                            try {
                                String[] parts = input.substring(6).split(":");
                                if (parts.length < 3) {
                                    error("§cInvalid input format");
                                    return;
                                }

                                String keys = parts[0];
                                if (keys.length() < 8) {
                                    error("§cInvalid keys format");
                                    return;
                                }

                                if (mc.player == null) return;

                                // Movement
                                if (syncMovement.get()) {
                                    try {
                                        mc.options.forwardKey.setPressed(keys.charAt(0) == '1');
                                        mc.options.backKey.setPressed(keys.charAt(1) == '1');
                                        mc.options.leftKey.setPressed(keys.charAt(2) == '1');
                                        mc.options.rightKey.setPressed(keys.charAt(3) == '1');
                                        mc.options.jumpKey.setPressed(keys.charAt(4) == '1');
                                        mc.options.sneakKey.setPressed(keys.charAt(5) == '1');
                                    } catch (IndexOutOfBoundsException e) {
                                        error("§cInvalid movement input");
                                    }
                                }

                                // Interact
                                if (syncInteract.get()) {
                                    try {
                                        boolean usePressed = keys.charAt(6) == '1';
                                        if (usePressed && !mc.options.useKey.isPressed()) {
                                            mc.options.useKey.setPressed(true);
                                            if (mc.crosshairTarget instanceof BlockHitResult hit) {
                                                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                                            }
                                        } else {
                                            mc.options.useKey.setPressed(usePressed);
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        error("§cInvalid interact input");
                                    }
                                }

                                // Attack
                                if (syncAttack.get()) {
                                    try {
                                        mc.options.attackKey.setPressed(keys.charAt(7) == '1');
                                    } catch (IndexOutOfBoundsException e) {
                                        error("§cInvalid attack input");
                                    }
                                }

                                // Rotation
                                if (syncRotation.get()) {
                                    try {
                                        float yaw = Float.parseFloat(parts[1]);
                                        float pitch = Float.parseFloat(parts[2]);
                                        mc.player.setYaw(yaw);
                                        mc.player.setPitch(pitch);
                                        mc.player.setHeadYaw(yaw);
                                    } catch (NumberFormatException e) {
                                        error("§cInvalid rotation values");
                                    }
                                }
                            } catch (Exception e) {
                                error("§cError processing input: " + e.getMessage());
                            }
                        });
                    }
                }
            } catch (IOException e) {
                if (isConnected) error("§cClient error: " + e.getMessage());
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
            error("§cError closing connection: " + e.getMessage());
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!isConnected) return;

        if (roleSetting.get() == Role.PARENT && processInputs) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSyncTime < delaySetting.get()) return;
            lastSyncTime = currentTime;

            if (mc.player != null) {
                StringBuilder inputs = new StringBuilder("INPUT:");
                inputs.append(mc.options.forwardKey.isPressed() ? "1" : "0");
                inputs.append(mc.options.backKey.isPressed() ? "1" : "0");
                inputs.append(mc.options.leftKey.isPressed() ? "1" : "0");
                inputs.append(mc.options.rightKey.isPressed() ? "1" : "0");
                inputs.append(mc.options.jumpKey.isPressed() ? "1" : "0");
                inputs.append(mc.options.sneakKey.isPressed() ? "1" : "0");
                inputs.append(mc.options.useKey.isPressed() ? "1" : "0");
                inputs.append(mc.options.attackKey.isPressed() ? "1" : "0");
                inputs.append(":" + mc.player.getYaw());
                inputs.append(":" + mc.player.getPitch());
                out.println(inputs.toString());
            }
        }
    }

    public enum Role {
        PARENT,
        CHILD
    }
}
