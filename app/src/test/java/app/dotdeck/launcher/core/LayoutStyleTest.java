package app.dotdeck.launcher.core;

import java.util.HashSet;
import java.util.Set;

final class LayoutStyleTest {
    private LayoutStyleTest() { }

    static void run() {
        Set<String> ids = new HashSet<>();
        for (LayoutStyle style : LayoutStyle.values()) {
            Check.isTrue(ids.add(style.id), "unique id " + style.id);
            Check.equal(style, LayoutStyle.byId(style.id), "lookup " + style.id);
        }
        Check.equal(LayoutStyle.DECK, LayoutStyle.byId("nonsense"), "unknown id falls back to deck");
        Check.equal(LayoutStyle.DECK, LayoutStyle.byId(null), "null id falls back to deck");
    }
}
