package com.extracraftx.minecraft.togglewalk.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Config {
    private static File configDir = new File("config");
    private static File configFile = new File("config/togglewalk_mk_ii.json");
    private static Gson gson = new GsonBuilder().setPrettyPrinting().
        setLenient().create();
    private static Config INSTANCE = null;

    public Toggle[] toggles = new Toggle[]{
        new Toggle("forward", "back"),
        new Toggle("sprint",  "sneak"),
        new Toggle("sneak",   "jump")
    };

    public long keyTapDelay = 1;

    public static synchronized Config getInstance() {
        if (INSTANCE != null)
            return INSTANCE;

        INSTANCE = new Config();
        try{
            configDir.mkdirs();
            if (configFile.createNewFile()) {
                FileWriter fw = new FileWriter(configFile);
                //fw.append(gson.toJson(INSTANCE.toggles));
                fw.append(gson.toJson(INSTANCE));
                fw.close();
            }
            else {
                FileReader fr = new FileReader(configFile);
                INSTANCE = gson.fromJson(fr, Config.class);
                // INSTANCE.toggles = gson.fromJson(fr, Toggle[].class);
                fr.close();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            INSTANCE = null;
        }

        return INSTANCE;
    }

    public static void saveConfigs() {
        try {
            configDir.mkdirs();
            FileWriter fw = new FileWriter(configFile);
            fw.append(gson.toJson(INSTANCE.toggles));
            fw.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////////////////////////


    // NOTE: this needs to be a local class (with static) rather than
    // an inner class (without static) to work properly with GSON
    public static class Toggle {
        public String toggle;
        public String untoggle;

        public Toggle(String toggle, String untoggle) {
            this.toggle = toggle;
            this.untoggle = untoggle;
        }

        public Toggle() {}
    }
}
