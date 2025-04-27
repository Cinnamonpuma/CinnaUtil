package com.example.addon;

import com.example.addon.commands.ChatSyncCmd;
import com.example.addon.commands.MultiInstanceCmd;
import com.example.addon.commands.WaitCommand;
import com.example.addon.modules.*;
import com.example.addon.systems.DupeSystem;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class
CinnaUtil extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("CinnaUtil");

    // Define the category here
    public static final Category CATEGORY = new Category("CinnaUtil");
    @Override
    public void onInitialize() {
        LOG.info("Initializing CinnaUtil");

        // Initialize DupeSystem
        DupeSystem dupeSystem = new DupeSystem();
        meteordevelopment.meteorclient.systems.Systems.add(dupeSystem);
        DupeSystem.INSTANCE = dupeSystem;

        // Register modules
        Modules.get().add(new DupeSequencesModule());
        Modules.get().add(new MultiInstanceCommand());
        Modules.get().add(new MultiInstanceMovement());
        Modules.get().add(new ChatColorModule());
        Modules.get().add(new ChatSyncModule());


        // Register commands
        Commands.add(new WaitCommand());
        Commands.add(new MultiInstanceCmd());
        Commands.add(new ChatSyncCmd());

        // Register HUD elements

    }
    @Override
    public void onRegisterCategories() {
        // Register the category
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }
}
