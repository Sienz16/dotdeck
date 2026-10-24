package app.dotdeck.launcher.core;

final class SlotsTest {
    private SlotsTest() { }

    static void run() {
        FavoriteSlots slots = new FavoriteSlots(new String[]{"maps", "music", "phone"});
        Check.equal(6, slots.snapshot().length, "six slots even for older saved data");
        slots.assign(0, "music");
        Check.equal("music", slots.get(0), "assignment replaces target");
        Check.equal("maps", slots.get(1), "existing favourite swaps instead of duplicating");
        slots.assign(2, "");
        Check.equal("", slots.get(2), "clear persists empty slot");
        String[] copy = slots.snapshot();
        copy[0] = "corruption";
        Check.equal("music", slots.get(0), "saved state cannot mutate slots");
        FavoriteSlots broken = new FavoriteSlots(new String[]{null, "a", "a"});
        Check.equal("", broken.get(0), "null saved slot becomes empty");
        Check.equal("", broken.get(2), "duplicate saved slot removed");
        try { slots.assign(6, "bad"); throw new AssertionError("invalid index accepted"); }
        catch (IndexOutOfBoundsException expected) { Check.isTrue(true, "invalid index rejected"); }
        Check.equal(Slot.values().length, FavoriteSlots.COUNT, "slot count follows the Slot enum");
    }
}
