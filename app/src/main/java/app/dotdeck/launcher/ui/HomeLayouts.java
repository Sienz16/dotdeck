package app.dotdeck.launcher.ui;

import app.dotdeck.launcher.core.LayoutStyle;

final class HomeLayouts {
    private HomeLayouts() { }

    static HomeLayout create(LayoutStyle style) {
        switch (style) {
            case DOCK: return new DockLayout();
            case LIST: return new ListLayout();
            case BENTO: return new BentoLayout();
            case STRIP: return new StripLayout();
            default: return new DeckLayout();
        }
    }
}
