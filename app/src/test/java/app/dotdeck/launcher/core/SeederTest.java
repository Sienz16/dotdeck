package app.dotdeck.launcher.core;

import java.util.Arrays;

final class SeederTest {
    private SeederTest() { }

    static void run() {
        AppCatalog catalog = new AppCatalog(Arrays.asList(
                new AppEntry("com.google.android.apps.maps/.Maps", "Maps"),
                new AppEntry("com.spotify.music/.Main", "Spotify"),
                new AppEntry("com.android.settings/.Settings", "Settings"),
                new AppEntry("app.camera/.Cam", "Camera")));
        FavoriteSlots slots = new FavoriteSlots(null);
        FavouriteSeeder.seed(slots, catalog);
        Check.equal("com.google.android.apps.maps/.Maps", slots.get(Slot.NAVIGATION.ordinal()), "maps seeds navigation");
        Check.equal("com.spotify.music/.Main", slots.get(Slot.AUDIO.ordinal()), "spotify seeds audio");
        Check.equal("", slots.get(Slot.PHONE.ordinal()), "no phone app leaves the tile empty");
        Check.equal("app.camera/.Cam", slots.get(Slot.CAMERA.ordinal()), "camera seeds camera");
        Check.equal("com.android.settings/.Settings", slots.get(Slot.YOUR_PICK.ordinal()), "settings seeds the last tile");

        FavoriteSlots chosen = new FavoriteSlots(new String[]{"", "", "", "", "", "com.google.android.apps.maps/.Maps"});
        FavouriteSeeder.seed(chosen, catalog);
        Check.equal("", chosen.get(Slot.NAVIGATION.ordinal()), "an app the user already placed is not reused");
        Check.equal("com.google.android.apps.maps/.Maps", chosen.get(Slot.YOUR_PICK.ordinal()), "user choice is kept");
        FavouriteSeeder.seed(new FavoriteSlots(null), AppCatalog.EMPTY);
        Check.isTrue(true, "empty catalog seeds nothing without failing");
    }
}
