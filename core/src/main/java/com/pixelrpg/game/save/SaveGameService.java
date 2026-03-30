package com.pixelrpg.game.save;

import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public final class SaveGameService {
    public static final String AUTOSAVE_SLOT_ID = "autosave";

    private static final String SAVE_DIRECTORY = "saves";
    private static final String SAVE_EXTENSION = ".json";

    private SaveGameService() {
    }

    public static Array<SaveGameData> listSaves() {
        Array<SaveGameData> saves = new Array<>();
        FileHandle directory = Gdx.files.local(SAVE_DIRECTORY);
        if (!directory.exists()) {
            return saves;
        }

        for (FileHandle file : directory.list()) {
            if (!SAVE_EXTENSION.equalsIgnoreCase("." + file.extension())) {
                continue;
            }
            SaveGameData save = readSave(file);
            if (save != null) {
                saves.add(save);
            }
        }

        saves.sort(new Comparator<SaveGameData>() {
            @Override
            public int compare(SaveGameData left, SaveGameData right) {
                return Long.compare(right.savedAtEpochMillis, left.savedAtEpochMillis);
            }
        });
        return saves;
    }

    public static String createNewSlotId(String baseName) {
        String normalizedBase = normalizeSlotId(baseName == null || baseName.isBlank() ? "save" : baseName);
        if (normalizedBase == null || normalizedBase.isBlank()) {
            normalizedBase = "save";
        }
        return normalizedBase + "-" + System.currentTimeMillis();
    }

    public static SaveGameData load(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            return null;
        }
        FileHandle file = slotFile(slotId);
        if (!file.exists()) {
            return null;
        }
        return readSave(file);
    }

    public static void save(SaveGameData save) {
        if (save == null || save.slotId == null || save.slotId.isBlank()) {
            return;
        }

        save.slotId = normalizeSlotId(save.slotId);
        if (save.displayName == null || save.displayName.isBlank()) {
            save.displayName = defaultDisplayName(save.slotId);
        }
        save.savedAtEpochMillis = System.currentTimeMillis();

        FileHandle directory = Gdx.files.local(SAVE_DIRECTORY);
        directory.mkdirs();
        slotFile(save.slotId).writeString(new Json().prettyPrint(save), false, "UTF-8");
    }

    public static boolean delete(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            return false;
        }
        FileHandle file = slotFile(slotId);
        return file.exists() && file.delete();
    }

    private static SaveGameData readSave(FileHandle file) {
        try {
            SaveGameData save = new Json().fromJson(SaveGameData.class, file.readString("UTF-8"));
            if (save == null) {
                return null;
            }
            if (save.slotId == null || save.slotId.isBlank()) {
                save.slotId = stripExtension(file.name());
            }
            if (save.displayName == null || save.displayName.isBlank()) {
                save.displayName = defaultDisplayName(save.slotId);
            }
            return save;
        } catch (Exception error) {
            Gdx.app.error("SaveGameService", "Failed to read save file: " + file.path(), error);
            return null;
        }
    }

    private static FileHandle slotFile(String slotId) {
        return Gdx.files.local(SAVE_DIRECTORY + "/" + normalizeSlotId(slotId) + SAVE_EXTENSION);
    }

    private static String normalizeSlotId(String slotId) {
        return slotId.trim().toLowerCase().replaceAll("[^a-z0-9_-]+", "-");
    }

    private static String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private static String defaultDisplayName(String slotId) {
        if (AUTOSAVE_SLOT_ID.equals(slotId)) {
            return "Autosave";
        }
        return slotId;
    }
}
