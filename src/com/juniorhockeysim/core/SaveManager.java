package com.juniorhockeysim.core;

import java.io.*;

public class SaveManager {

    private static final String SAVE_FILE = "franchise_save.dat";

    public static void save(Object obj) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(SAVE_FILE))) {
            out.writeObject(obj);
        }
    }

    public static Object load() throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(SAVE_FILE))) {
            return in.readObject();
        }
    }

    public static boolean saveExists() {
        return new File(SAVE_FILE).exists();
    }
}
