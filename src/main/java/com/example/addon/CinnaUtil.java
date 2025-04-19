package com.example.addon;

import com.example.addon.commands.MultiDupeCommand;
import com.example.addon.commands.WaitCommand;
import com.example.addon.modules.CmiDupe;
import com.example.addon.modules.ChatColorModule;
import com.example.addon.systems.DupeSystem;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import com.example.addon.modules.DupeSequencesModule;
import com.example.addon.modules.MultiInstanceDupe;
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
        DupeSystem.INSTANCE = new DupeSystem();

        // Register modules
        Modules.get().add(new DupeSequencesModule());
        Modules.get().add(new MultiInstanceDupe());
        Modules.get().add(new CmiDupe());
        Modules.get().add(new ChatColorModule());


        // Register commands
        Commands.add(new WaitCommand());
        Commands.add(new MultiDupeCommand());

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
