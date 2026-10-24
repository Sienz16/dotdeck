package app.dotdeck.launcher.ui;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import app.dotdeck.launcher.R;
import app.dotdeck.launcher.core.AppEntry;
import app.dotdeck.launcher.core.Theme;
import java.util.ArrayList;
import java.util.List;

import static app.dotdeck.launcher.ui.Ui.MATCH;
import static app.dotdeck.launcher.ui.Ui.WRAP;

/** Searchable, alphabetical grid of every launchable app. Also the picker for a favourite tile. */
final class DrawerScreen implements Screen {
    private static final ColorMatrixColorFilter GRAYSCALE = grayscale();

    private final ScreenContext ctx;
    private final Ui ui;
    private AppAdapter adapter;
    private TextView appCount;

    DrawerScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.ui = ctx.ui;
    }

    @Override public void build(LinearLayout root) {
        Theme theme = ui.theme;
        boolean picking = ctx.nav.isPicking();
        ui.addPageHeader(root, picking ? "Choose your app" : "All apps",
                picking ? "FAVOURITE 0" + (ctx.nav.picking() + 1) : "EVERYTHING, IN ONE PLACE",
                v -> ctx.navigator.home());
        root.addView(buildSearch(root), new LinearLayout.LayoutParams(MATCH, ui.dp(48)));
        appCount = ui.mono("", 10, theme.dim);
        appCount.setPadding(0, ui.dp(12), 0, ui.dp(8));
        root.addView(appCount);
        root.addView(buildResults(), new LinearLayout.LayoutParams(MATCH, 0, 1));
        updateResults();
    }

    @Override public void onDataChanged() { updateResults(); }

    private EditText buildSearch(LinearLayout root) {
        Theme theme = ui.theme;
        EditText search = new EditText(ui.context);
        search.setSingleLine(true);
        search.setTextColor(theme.ink);
        search.setHintTextColor(theme.dim);
        search.setTextSize(16);
        search.setHint("Search apps");
        search.setPadding(ui.dp(18), 0, ui.dp(18), 0);
        search.setBackground(ui.shape(theme.panel, 16, theme.outlined));
        search.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        search.setImeOptions(EditorInfo.IME_ACTION_DONE);
        search.setOnEditorActionListener((v, action, event) -> { ctx.actions.hideKeyboard(); return true; });
        search.setText(ctx.nav.query());
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                ctx.nav.setQuery(s.toString());
                updateResults();
            }
            @Override public void afterTextChanged(Editable value) { }
        });
        return search;
    }

    private FrameLayout buildResults() {
        FrameLayout results = new FrameLayout(ui.context);
        GridView grid = new GridView(ui.context);
        grid.setNumColumns(Math.max(2, Math.min(6, ui.context.getResources().getConfiguration().screenWidthDp / 140)));
        grid.setHorizontalSpacing(ui.dp(10));
        grid.setVerticalSpacing(ui.dp(10));
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        grid.setClipToPadding(false);
        grid.setPadding(0, ui.dp(4), 0, ui.dp(8));
        grid.setSelector(android.R.color.transparent);
        adapter = new AppAdapter();
        grid.setAdapter(adapter);
        results.addView(grid, new FrameLayout.LayoutParams(MATCH, MATCH));
        TextView empty = ui.text("No matching apps.\nTry another search.", 18, ui.theme.dim);
        empty.setGravity(Gravity.CENTER);
        results.addView(empty, new FrameLayout.LayoutParams(MATCH, MATCH));
        grid.setEmptyView(empty);
        return results;
    }

    private void updateResults() {
        if (adapter == null || appCount == null) { return; }
        boolean loadingFirstList = ctx.repository.isLoading() && ctx.repository.catalog().isEmpty();
        adapter.filtered = ctx.repository.catalog().search(ctx.nav.query());
        adapter.notifyDataSetChanged();
        appCount.setText(loadingFirstList ? "LOADING APPS…" : adapter.getCount() + " APPS / "
                + (ctx.nav.isPicking() ? "TAP TO ASSIGN" : "TAP TO OPEN · HOLD FOR OPTIONS"));
    }

    private static ColorMatrixColorFilter grayscale() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        return new ColorMatrixColorFilter(matrix);
    }

    private final class AppAdapter extends BaseAdapter {
        List<AppEntry> filtered = new ArrayList<>();

        @Override public int getCount() { return filtered.size(); }
        @Override public AppEntry getItem(int position) { return filtered.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override public View getView(int position, View reusable, ViewGroup parent) {
            LinearLayout cell = reusable == null ? newCell() : (LinearLayout) reusable;
            AppEntry app = getItem(position);
            ImageView image = (ImageView) cell.getChildAt(0);
            ((TextView) cell.getChildAt(1)).setText(app.label);
            cell.setContentDescription(app.label);
            image.setColorFilter(ctx.settings.monoIcons() ? GRAYSCALE : null);
            Bitmap icon = ctx.icons.get(app.key);
            if (icon != null) { image.setImageBitmap(icon); } else { image.setImageResource(R.drawable.ic_launcher); }
            cell.setOnClickListener(v -> {
                if (ctx.nav.isPicking()) { ctx.assignAndGoHome(ctx.nav.picking(), app.key); }
                else { ctx.launch(app); }
            });
            cell.setOnLongClickListener(v -> { Dialogs.appOptions(ctx, app); return true; });
            return cell;
        }

        private LinearLayout newCell() {
            LinearLayout cell = ui.vertical();
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(ui.dp(10), ui.dp(14), ui.dp(10), ui.dp(14));
            cell.setBackground(ui.panel(18));
            ImageView image = new ImageView(ui.context);
            image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            cell.addView(image, new LinearLayout.LayoutParams(ui.dp(38), ui.dp(38)));
            TextView label = ui.text("", 13, ui.theme.ink);
            label.setGravity(Gravity.CENTER);
            label.setSingleLine(true);
            label.setEllipsize(TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(MATCH, WRAP);
            labelParams.topMargin = ui.dp(12);
            cell.addView(label, labelParams);
            cell.setLayoutParams(new AbsListView.LayoutParams(MATCH, ui.dp(110)));
            return cell;
        }
    }
}
