package app.dotdeck.launcher.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import app.dotdeck.launcher.core.Glyph;
import app.dotdeck.launcher.core.LayoutStyle;
import app.dotdeck.launcher.core.Theme;
import app.dotdeck.launcher.core.Themes;
import java.util.ArrayList;
import java.util.List;

import static app.dotdeck.launcher.ui.Ui.MATCH;
import static app.dotdeck.launcher.ui.Ui.WRAP;

/** Theme and layout pickers, the launcher's switches, and shortcuts into Android's own settings. */
final class SettingsScreen implements Screen {
    private final ScreenContext ctx;
    private final Ui ui;

    SettingsScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.ui = ctx.ui;
    }

    @Override public void build(LinearLayout root) {
        ui.addPageHeader(root, "Make it yours", "DOTDECK / SETTINGS", v -> ctx.navigator.home());
        ScrollView scroll = new ScrollView(ui.context);
        scroll.setFillViewport(false);
        LinearLayout content = ui.vertical();
        content.setPadding(0, ui.dp(12), 0, ui.dp(20));
        scroll.addView(content);
        addThemePicker(content);
        addLayoutPicker(content);
        addRow(content, "Animations", ctx.settings.motion()
                ? "On / tap to turn off" : "Off / tap to turn on",
                () -> { ctx.settings.toggleMotion(); ctx.navigator.rerender(); });
        addRow(content, "Default home screen", "Choose DotDeck or switch back to your previous launcher.",
                ctx.actions::chooseHome);
        addRow(content, "Clock format", ctx.settings.clock24() ? "24 hour / tap for 12 hour" : "12 hour / tap for 24 hour",
                () -> { ctx.settings.toggleClock24(); ctx.navigator.rerender(); });
        addRow(content, "App icons", ctx.settings.monoIcons()
                ? "Monochrome / tap for original colours" : "Original colours / tap for monochrome",
                () -> { ctx.settings.toggleMonoIcons(); ctx.navigator.rerender(); });
        addSystemRow(content, "Brightness & display", "Open Android display settings.", Settings.ACTION_DISPLAY_SETTINGS);
        addSystemRow(content, "Wi-Fi", "Open Android Wi-Fi settings.", Settings.ACTION_WIFI_SETTINGS);
        addSystemRow(content, "Bluetooth", "Open Android Bluetooth settings.", Settings.ACTION_BLUETOOTH_SETTINGS);
        addSystemRow(content, "System settings", "Your player's original Android settings.", Settings.ACTION_SETTINGS);
        addSystemRow(content, "Device", deviceSummary(), Settings.ACTION_DEVICE_INFO_SETTINGS);
        TextView about = ui.text("DOTDECK " + versionName() + "\nBuilt to do less. Designed to get you there.\n\n"
                + "No ads. No analytics. No network permission.\n\n"
                + "Some NWD firmware overrides the standard home choice.\n"
                + "Device-specific setup can be checked later through ADB.", 12, ui.theme.dim);
        about.setLineSpacing(ui.dp(3), 1f);
        about.setPadding(ui.dp(6), ui.dp(20), ui.dp(6), ui.dp(20));
        content.addView(about);
        root.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1));
    }

    private void addThemePicker(LinearLayout parent) {
        Theme current = ui.theme;
        List<View> cells = new ArrayList<>();
        for (Theme theme : Themes.all()) {
            boolean selected = theme == current;
            cells.add(choice(new ThemeSwatch(ui.context, theme, selected, current.ink), theme.name, selected,
                    "Theme " + theme.name, () -> { ctx.settings.setTheme(theme); ctx.navigator.rerender(); }));
        }
        addChoices(parent, "THEME / " + current.name, cells);
    }

    private void addLayoutPicker(LinearLayout parent) {
        Theme current = ui.theme;
        LayoutStyle chosen = ctx.settings.layout();
        List<View> cells = new ArrayList<>();
        for (LayoutStyle style : LayoutStyle.values()) {
            boolean selected = style == chosen;
            cells.add(choice(new LayoutPreview(ui.context, style, current, selected, current.ink), style.label, selected,
                    "Layout " + style.label, () -> { ctx.settings.setLayout(style); ctx.navigator.rerender(); }));
        }
        addChoices(parent, "LAYOUT / " + chosen.label + " · " + chosen.blurb.toUpperCase(), cells);
    }

    /** A titled grid of picker cells, as many per row as the screen comfortably holds. */
    private void addChoices(LinearLayout parent, String title, List<View> cells) {
        TextView heading = ui.mono(title, 10, ui.theme.dim);
        heading.setPadding(ui.dp(4), 0, 0, ui.dp(10));
        parent.addView(heading);
        int columns = ui.portrait ? 3 : 5;
        for (int start = 0; start < cells.size(); start += columns) {
            LinearLayout row = ui.horizontal();
            for (int col = 0; col < columns; col++) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, WRAP, 1);
                if (col < columns - 1) { params.rightMargin = ui.dp(10); }
                int index = start + col;
                row.addView(index < cells.size() ? cells.get(index) : new View(ui.context), params);
            }
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(MATCH, WRAP);
            rowParams.bottomMargin = ui.dp(10);
            parent.addView(row, rowParams);
        }
    }

    /** One picker cell: a preview, its name, and a click that applies it. */
    private LinearLayout choice(View preview, String name, boolean selected, String description, Runnable apply) {
        Theme current = ui.theme;
        LinearLayout cell = ui.vertical();
        cell.addView(preview, new LinearLayout.LayoutParams(MATCH, ui.dp(ui.compact ? 56 : 68)));
        TextView caption = ui.mono(name, 10, selected ? current.ink : current.dim);
        caption.setPadding(ui.dp(2), ui.dp(6), 0, ui.dp(2));
        caption.setSingleLine(true);
        cell.addView(caption);
        cell.setContentDescription(description + (selected ? ", selected" : ""));
        cell.setOnClickListener(v -> apply.run());
        return cell;
    }

    private void addSystemRow(LinearLayout parent, String title, String detail, String action) {
        addRow(parent, title, detail, () -> ctx.actions.openSystem(action));
    }

    private void addRow(LinearLayout parent, String title, String detail, Runnable action) {
        Theme theme = ui.theme;
        LinearLayout row = ui.horizontal();
        row.setPadding(ui.dp(18), ui.dp(16), ui.dp(18), ui.dp(16));
        row.setMinimumHeight(ui.dp(72));
        row.setBackground(ui.panel(16));
        LinearLayout copy = ui.vertical();
        copy.addView(ui.text(title, 17, theme.ink));
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(MATCH, WRAP);
        detailParams.topMargin = ui.dp(5);
        copy.addView(ui.text(detail, 12, theme.dim), detailParams);
        row.addView(copy, new LinearLayout.LayoutParams(0, WRAP, 1));
        row.addView(new GlyphView(ui.context, Glyph.ARROW, theme.dim), new LinearLayout.LayoutParams(ui.dp(20), ui.dp(20)));
        row.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH, WRAP);
        params.bottomMargin = ui.dp(8);
        parent.addView(row, params);
    }

    private String deviceSummary() {
        String android = "Android " + Build.VERSION.RELEASE;
        ActivityManager manager = (ActivityManager) ui.context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) { return android; }
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(info);
        return android + " · " + Math.round(info.totalMem / 1048576d) + " MB RAM\n"
                + Math.round(info.availMem / 1048576d) + " MB available (system snapshot)";
    }

    private String versionName() {
        try {
            String name = ui.context.getPackageManager().getPackageInfo(ui.context.getPackageName(), 0).versionName;
            return name == null ? "" : name;
        } catch (PackageManager.NameNotFoundException | RuntimeException unavailable) {
            return "";
        }
    }
}
