package com.juniorhockeysim.ui;

import javafx.scene.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines primary and secondary accent colors for each team.
 * Provides logo images with two-tier loading:
 *  1. Runtime file: ./logos/<slug>.png next to the JAR (user replaceable!)
 *  2. Embedded base64 in TeamLogos.java (fallback)
 *
 * To use custom logos: just drop PNG files with the same slug names into a
 * "logos/" folder next to the JAR. They load automatically at startup.
 * Example: logos/north-hawks.png, logos/east-kings.png, etc.
 */
public class TeamColors {

    public record TeamColor(String primary, String secondary, String logoIcon, String slug) {}

    private static final Map<String, TeamColor> COLORS = new HashMap<>();
    private static final Map<String, Image> LOGO_CACHE = new HashMap<>();

    static {
        COLORS.put("North Hawks",    new TeamColor("#FF6B2B", "#FF9A6C", "🦅", "north-hawks"));
        COLORS.put("East Kings",     new TeamColor("#7B2FBE", "#B57AEB", "♛",  "east-kings"));
        COLORS.put("South Wolves",   new TeamColor("#DC143C", "#FF4D6D", "🐺", "south-wolves"));
        COLORS.put("West Falcons",   new TeamColor("#00B4A0", "#4DD9CC", "🦅", "west-falcons"));
        COLORS.put("Central Bears",  new TeamColor("#FFB800", "#FFD966", "🐻", "central-bears"));
        COLORS.put("River Knights",  new TeamColor("#4169E1", "#7B9FFF", "⚔",  "river-knights"));
        COLORS.put("Metro Rangers",  new TeamColor("#228B22", "#4CAF50", "★",  "metro-rangers"));
        COLORS.put("Coastal Storm",  new TeamColor("#00BFFF", "#66D9FF", "⚡", "coastal-storm"));
        COLORS.put("Prairie Giants", new TeamColor("#FF8C00", "#FFB347", "▲",  "prairie-giants"));
        COLORS.put("Valley Titans",  new TeamColor("#0047AB", "#3D7EFF", "⚡", "valley-titans"));
        COLORS.put("Forest Blades",  new TeamColor("#50C878", "#8FFFA8", "⚔",  "forest-blades"));
        COLORS.put("Mountain Lions", new TeamColor("#FF2400", "#FF6655", "🦁", "mountain-lions"));
    }

    public static TeamColor get(String teamName) {
        return COLORS.getOrDefault(teamName, new TeamColor("#00D4FF", "#66E8FF", "●", "unknown"));
    }

    public static String getPrimary(String teamName)   { return get(teamName).primary();   }
    public static String getSecondary(String teamName) { return get(teamName).secondary(); }
    public static String getLogo(String teamName)      { return get(teamName).logoIcon();  }

    /**
     * Returns the actual logo Image for the given team.
     * Checks runtime ./logos/ folder first, then falls back to embedded base64.
     */
    public static Image getLogoImage(String teamName) {
        if (LOGO_CACHE.containsKey(teamName)) return LOGO_CACHE.get(teamName);

        // Tier 1: runtime file override (user can drop PNGs here)
        String slug = get(teamName).slug();
        try {
            Path runtimePath = Paths.get("logos", slug + ".png");
            if (Files.exists(runtimePath)) {
                Image img = new Image(runtimePath.toUri().toString());
                if (!img.isError()) {
                    LOGO_CACHE.put(teamName, img);
                    return img;
                }
            }
        } catch (Exception ignored) {}

        // Tier 2: embedded base64
        String b64 = TeamLogos.getBase64(teamName);
        if (b64 == null) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(b64);
            Image img = new Image(new ByteArrayInputStream(bytes));
            if (!img.isError()) {
                LOGO_CACHE.put(teamName, img);
                return img;
            }
        } catch (Exception ignored) {}

        return null;
    }
}
