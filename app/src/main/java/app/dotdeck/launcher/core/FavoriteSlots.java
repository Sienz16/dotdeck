package app.dotdeck.launcher.core;

public final class FavoriteSlots {
    public static final int COUNT = Slot.values().length;
    private final String[] keys = new String[COUNT];

    public FavoriteSlots(String[] saved) {
        for (int i = 0; i < COUNT; i++) {
            String value = saved != null && i < saved.length && saved[i] != null ? saved[i] : "";
            for (int previous = 0; previous < i; previous++) {
                if (value.equals(keys[previous])) { value = ""; break; }
            }
            keys[i] = value;
        }
    }

    public String get(int slot) { return keys[slot]; }

    public String[] snapshot() { return keys.clone(); }

    public void assign(int slot, String key) {
        String previous = keys[slot];
        String value = key == null ? "" : key;
        if (!value.isEmpty()) {
            for (int i = 0; i < COUNT; i++) {
                if (i != slot && value.equals(keys[i])) { keys[i] = previous; break; }
            }
        }
        keys[slot] = value;
    }
}
