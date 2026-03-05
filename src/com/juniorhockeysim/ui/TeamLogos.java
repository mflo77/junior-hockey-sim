package com.juniorhockeysim.ui;

import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class TeamLogos {

    private static final Map<String, String> FILE_NAMES = new HashMap<>();

    static {
        FILE_NAMES.put("North Hawks", "north-hawks.png");
        FILE_NAMES.put("East Kings",  "east-kings.png");
        FILE_NAMES.put("Central Bears", "central-bears.png");
        FILE_NAMES.put("Coastal Storm", "coastal-storm.png");
        FILE_NAMES.put("Forest Blades", "forest-blades.png");
        FILE_NAMES.put("Metro Rangers", "metro-rangers.png");
        FILE_NAMES.put("Mountain Lions", "mountain-lions.png");
        FILE_NAMES.put("Prairie Giants", "prairie-giants.png");
        FILE_NAMES.put("River Knights", "river-knights.png");
        FILE_NAMES.put("South Wolves", "south-wolves.png");
        FILE_NAMES.put("Valley Titans", "valley-titans.png");
        FILE_NAMES.put("West Falcons", "west-falcons.png");
    }

    public static String getBase64(String teamName) {
        String fileName = FILE_NAMES.get(teamName);
        if (fileName == null) return null;

        String path = "/logos/" + fileName;
        try (InputStream is = TeamLogos.class.getResourceAsStream(path)) {
            if (is == null) return null;
            byte[] bytes = is.readAllBytes();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}