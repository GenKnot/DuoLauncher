package com.duo.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.duo.launcher.I18n.S;
import static com.duo.launcher.I18n.t;

/** Lower screen: home folders and the zoomable icon grid, driven by touch or gamepad. */
final class ShelfView extends View {

    interface Listener {
        void onFocus(Object item);
        void onDisplayModeChanged(Library.Game g);
        void onCheats(Library.Game g);
        void onActivate(Library.Game g);
        void onChoose(Object item, String cmd);
        String chosenCmd(Object item);
        void onSettings();
        void onSearch();
        void onAddFolder();
        void onEditFolder(Library.Platform folder);
        void onAddToFolder(Library.Game g, Menu parent);
        void onRemoveFromFolder(Library.Platform folder, Library.Game g);
        void onHideFolder(Library.Platform folder);
        void onHideGame(Library.Game g);
        /** Takes the NEW badge off these games. */
        void onMarkSeen(List<Library.Game> games);
        /** Lets the player say which console a file is for. */
        void onPickSystem(Library.Game g);
        boolean isFavorite(Library.Game g);
        void onToggleFavorite(Library.Game g);
        /** Last-played time (mode SORT_RECENT) or total play time (SORT_PLAYTIME), for sorting. */
        long sortValue(Library.Game g, int mode);
        /** Sound cue for a UI action, see {@link Sfx}. */
        void onCue(int cue);
        boolean isDefaultHome();
        void onOpenDesktop();
        /** Y on a built-in home tile: rename, recolour or hide it. */
        void onEditTool(String id, String label, int color);
        void onRenamePlatform(Library.Platform p);
        void onStats();
        /** The storage tile: free space and what takes it. */
        void onStorageTile();
        /** The "what to play today" tile: pick a game and show it. */
        void onRandom();
        /** Y → Details on a game: file, size, play time, screenshots. */
        void onDetails(Library.Game g);
        /** Play time and last-played text for the list view, or null. */
        String playedLine(Library.Game g);
        /** First letter of a game for quick jumps: A–Z (Chinese titles by their pinyin), or '#'. */
        char initial(Library.Game g);
        /** The home page order changed (keys from {@link ShelfView#homeKey}, one per line). */
        void onHomeOrder(String order);
        /** The player's own order of a folder's games changed (game keys, one per line; empty for the default). */
        void onGameOrder(String platform, String order);
        /** The home page switched between platforms and genres. */
        void onHomeMode(int mode);
        /** A game's note, or null. */
        String noteOf(Library.Game g);
        /** Y → Note on a game. */
        void onNote(Library.Game g, Menu parent);
        /** Y → Ask AI on a game. */
        void onAskAi(Library.Game g, Menu parent);
        /** Y → Status on a game (playing, done…) and its rating. */
        void onStatus(Library.Game g, Menu parent);
        /** Y → Game info → Play history: the game's play time on the upper screen, and clearing it. */
        void onPlayHistory(Library.Game g, Menu parent);
        String playLine(Library.Game g);
        /** The status row's label, e.g. "Playing · ★★★★☆". */
        String statusLine(Library.Game g);
        /** Y → How to play. */
        void onHowTo(Library.Game g);
        /** Y → Saves of one game. */
        void onSaves(Library.Game g, Menu parent);
        /** Y → Genre on a game. */
        void onPickGenre(Library.Game g, Menu parent);
        /** Y → Merge on a console folder. */
        void onMergePlatform(Library.Platform p, Menu parent);
        /** Y on a genre folder: its picture. */
        void onEditGenre(Library.Platform p);
        /** Y → More → Delete the game file: asks first. */
        void onDeleteGame(Library.Game g, Menu parent);
        /** Y → Game info → AI pictures for this game: cover, backdrop, title logo. */
        void onAiGamePictures(Library.Game g, Menu parent);
    }

    /** The user's own names and colours for the built-in home tiles, by tool id; filled from Settings. */
    static final Map<String, String> TOOL_NAMES = new HashMap<>();
    static final Map<String, Integer> TOOL_COLORS = new HashMap<>();

    /** Id of a built-in home tile ("continue", "recent", "favorites", "add", "search", "settings", "desktop"), else null. */
    String toolId(Object o) {
        if (o == CONTINUE) return "continue";
        if (o == st.recent) return "recent";
        if (o == st.favorites) return "favorites";
        if (o == ADD) return "add";
        if (o == SEARCH) return "search";
        if (o == SETTINGS) return "settings";
        if (o == DESKTOP) return "desktop";
        if (o == STATS) return "stats";
        if (o == RANDOM) return "random";
        if (o == STORAGE) return "storage";
        return null;
    }

    static String toolName(String id, String def) {
        String s = TOOL_NAMES.get(id);
        return s != null ? s : def;
    }

    /** Own colour, else the theme's "folder.<id>" colour, else the built-in one. */
    static int toolColor(String id, int def) {
        Integer c = TOOL_COLORS.get(id);
        return c != null ? c : Palette.folder(id.equals("add") ? "custom" : id, def);
    }

    /** Marker items for the Search, Settings, "new folder" and "continue playing" tiles on the home page. */
    static final Object SETTINGS = new Object(), SEARCH = new Object(), ADD = new Object(), CONTINUE = new Object(),
            DESKTOP = new Object(), STATS = new Object(), RANDOM = new Object(), STORAGE = new Object();

    static final int SORT_DEFAULT = 0, SORT_NAME = 1, SORT_RECENT = 2, SORT_PLAYTIME = 3;
    /** Language tags in game names that the filter understands. */
    static final String[] LANG_TAGS = {"简", "繁", "英"};

    /** A list overlay; each entry runs its action when picked. B goes back to the parent menu. */
    static final class Menu {
        final String title, subtitle;
        final List<String> labels = new ArrayList<>();
        final List<Boolean> marks = new ArrayList<>();
        final List<Runnable> actions = new ArrayList<>();
        final List<Integer> swatches = new ArrayList<>();   // optional colour dot per row
        final List<Bitmap> images = new ArrayList<>();      // optional picture per row
        /** What a row does, shown under the list while the cursor is on it. */
        final Map<Integer, String> hints = new HashMap<>();
        Menu parent;
        /** Instead of returning to the parent as it was, B runs this (to show a refreshed parent). */
        Runnable onBack;
        /** Shows this menu again with fresh labels (a door menu whose submenus change what its rows say). */
        Runnable reopen;
        /** Called with the new row whenever the cursor moves (e.g. to preview a picture on the upper screen). */
        java.util.function.IntConsumer onSelect;
        int sel, top;
        /** A full-screen page of categories (Settings): their names on the left, the rows of one on the right. */
        final List<Menu> tabs = new ArrayList<>();
        /** The category shown, and whether the cursor is in its rows (else in the category list). */
        int tab;
        boolean inRows;
        /** For a category: the page it belongs to. */
        Menu page;
        /** For a page: called with the category whenever it changes. */
        java.util.function.IntConsumer onTab;
        /** For a category: its colour dot on the settings page. */
        int color = 0xFF6D8299;
        /** Rows that switch something on or off, drawn as a switch (its state is the row's mark). */
        final java.util.Set<Integer> toggles = new java.util.HashSet<>();

        Menu toggle(String label, boolean on, Runnable action) {
            toggles.add(labels.size());
            labels.add(label);
            marks.add(on);
            actions.add(action);
            swatches.add(null);
            images.add(null);
            return this;
        }

        Menu(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }

        boolean isPage() { return !tabs.isEmpty(); }

        /** Adds a category to this page. */
        Menu tab(String name, int color) {
            Menu t = new Menu(name, null);
            t.page = this;
            t.color = color;
            tabs.add(t);
            return t;
        }

        Menu add(String label, boolean mark, Runnable action) { return add(label, mark, null, null, action); }

        /** Explains the row added last. */
        Menu hint(String h) {
            if (!labels.isEmpty() && h != null) hints.put(labels.size() - 1, h);
            return this;
        }

        Menu add(String label, boolean mark, Integer swatch, Bitmap image, Runnable action) {
            labels.add(label);
            marks.add(mark);
            actions.add(action);
            swatches.add(swatch);
            images.add(image);
            if (mark && marks.indexOf(true) == marks.size() - 1) sel = marks.size() - 1;
            return this;
        }
    }

    /** Survives the view being recreated each time the presentation is shown. */
    static final class State {
        Library lib;
        final Library.Platform recent = new Library.Platform();
        Library.Platform current;
        final List<Object> items = new ArrayList<>();
        final Map<String, Integer> lastSel = new HashMap<>();
        int sel, zoom = 1;
        int sortMode = SORT_DEFAULT;
        String langFilter;           // one of LANG_TAGS, or null for all
        /** Games the user starred, shown as their own folder on the home page. */
        final Library.Platform favorites = new Library.Platform();
        float scroll, scrollTarget;
        Menu menu;
        String message;

        /** Search results are shown as a pseudo-platform. */
        final Library.Platform results = new Library.Platform();
        String query = "";
        /** Title of the results page when it shows a list instead of a search. */
        String listTitle;
        /** B on a list opened from a menu (the problem centre): back to that menu instead of the home page. */
        Runnable listBack;
        /** Names of platform folders the user hid from the home page. */
        final java.util.Set<String> hidden = new java.util.HashSet<>();
        /** The user's own order of the home tiles, by {@link ShelfView#homeKey}; empty for the built-in order. */
        final List<String> homeOrder = new ArrayList<>();
        /** Index the lifted tile came from while it is being moved, or -1. */
        int moveFrom = -1;
        /** The player's own order of games per folder (folder name -> game keys), over the Pegasus or file order. */
        final Map<String, List<String>> gameOrder = new HashMap<>();
        /** Home page by genre instead of by platform. */
        int homeMode;
        /** Space around a game's picture in its tile: 0 fills the tile, 1-3 leave a small, medium or large margin. */
        int tilePad;
        /** An AI key is set, so holding R asks the AI (shown in the footer). */
        boolean askAi;
        /** One folder per series with at least two games. */
        final List<Library.Platform> series = new ArrayList<>();
        /** The search keyboard is open on the top screen. */
        boolean typing;
        /** One folder per genre (and one for games without a genre), rebuilt with the library. */
        final List<Library.Platform> genres = new ArrayList<>();

        State() {
            recent.name = "recent";
            results.name = "search";
            favorites.name = "favorites";
        }
    }

    /** Columns per zoom step; the last step (0 columns) is the list view. */
    private static final int[] ZOOM = {4, 5, 6, 8, 0};
    private static final int BLUE = 0xFF2F8BFF;
    private static final int MENU_ROWS = 8;

    final State st;
    private final Icons icons;
    private final Listener listener;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF(), r2 = new RectF();
    private final Rect src = new Rect();
    private final Path path = new Path();
    private final GestureDetector gd;
    private final OverScroller scroller;
    private final RectF hitHome = new RectF(), hitL = new RectF(), hitR = new RectF(), hitZoom = new RectF(), hitSort = new RectF(),
            hitSearch = new RectF();
    private final Runnable redraw = this::invalidate;

    ShelfView(Context c, State st, Icons icons, Listener l) {
        super(c);
        this.st = st;
        this.icons = icons;
        this.listener = l;
        scroller = new OverScroller(c);
        gd = new GestureDetector(c, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent e) { scroller.forceFinished(true); return true; }

            @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
                if (st.menu != null) return true;
                st.scroll = st.scrollTarget = clamp(st.scroll + dy);
                invalidate();
                return true;
            }

            @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                if (st.menu != null) return true;
                scroller.fling(0, (int) st.scroll, 0, (int) -vy, 0, 0, 0, (int) maxScroll());
                postInvalidateOnAnimation();
                return true;
            }

            @Override public boolean onSingleTapUp(MotionEvent e) { tap(e.getX(), e.getY()); return true; }

            @Override public void onLongPress(MotionEvent e) { longPress(e.getX(), e.getY()); }
        });
        // the library may have arrived before this view existed (start-up, or back from a game)
        if (st.lib != null && st.genres.isEmpty()) rebuildGenres();
    }

    // ---- state changes ----

    void setLibrary(Library lib, List<Library.Game> recent) {
        st.lib = lib;
        st.message = null;
        rebuildGenres();
        if (st.current != null && isGenre(st.current) && !st.genres.contains(st.current) && !st.series.contains(st.current)) st.current = null;
        setRecent(recent);
        if (st.current == null) goHome(); else fireFocus();
    }

    void setRecent(List<Library.Game> recent) {
        st.recent.games.clear();
        st.recent.games.addAll(recent);
        if (st.current == null && st.lib != null) {
            Object keep = selected();
            rebuildHome();
            st.sel = Math.max(0, st.items.indexOf(keep));
            invalidate();
        }
    }

    private void addTool(Object o) {
        if (!st.hidden.contains("tool:" + toolId(o))) st.items.add(o);
    }

    /** True for the pages that are lists of games from several platforms rather than a platform. */
    boolean isPseudo(Library.Platform p) { return p == st.recent || p == st.results || p == st.favorites || isGenre(p); }

    static boolean isGenre(Library.Platform p) { return p != null && p.system.equals("GENRE"); }

    /** Sorts every game of the library into the genre folders (after the library or a genre changed). */
    void rebuildGenres() {
        st.genres.clear();
        if (st.lib == null) return;
        Map<String, Library.Platform> by = new HashMap<>();
        List<String> ids = new ArrayList<>(java.util.Arrays.asList(Genres.IDS));
        ids.add("none");
        for (String id : ids) {
            Library.Platform g = new Library.Platform();
            g.name = "genre-" + id;
            g.system = "GENRE";
            g.label = Genres.name(id);
            g.color = Genres.color(id);
            g.art = "custom";
            g.bgArt = "custom";
            // a bundled clay icon for the genre, unless the user picked their own
            if (Theme.asset("consoles/genre-" + id + ".png") != null) g.art = "genre-" + id;
            g.ownIcon = !g.art.equals("custom") || Theme.isCustom(g.name, Theme.CONSOLE);
            by.put(id, g);
            st.genres.add(g);
        }
        java.util.Set<Library.Game> seen = new java.util.HashSet<>();
        for (Library.Platform p : st.lib.platforms) {
            if (p.isCustom() || isNative(p)) continue;
            for (Library.Game g : p.games) if (seen.add(g)) by.get(g.genre == null || Genres.index(g.genre) < 0 ? "none" : g.genre).games.add(g);
        }
        st.genres.removeIf(g -> g.games.isEmpty());
        rebuildSeries();
        if (st.current != null && isGenre(st.current)) {
            for (Library.Platform g : st.genres) if (g.name.equals(st.current.name)) { st.current = g; return; }
            for (Library.Platform g : st.series) if (g.name.equals(st.current.name)) { st.current = g; return; }
        }
    }

    /** Groups the games of every platform by series (a folder needs two games or more). */
    private void rebuildSeries() {
        st.series.clear();
        Map<String, Library.Platform> by = new java.util.LinkedHashMap<>();
        boolean zh = I18n.code().startsWith("zh");
        java.util.Set<Library.Game> seen = new java.util.HashSet<>();
        for (Library.Platform p : st.lib.platforms) {
            if (p.isCustom() || isNative(p)) continue;
            for (Library.Game g : p.games) {
                if (g.series == null || g.series.equals(Series.NONE) || !seen.add(g)) continue;
                String k = Series.norm(g.series);
                if (k.isEmpty()) continue;
                Library.Platform s = by.get(k);
                if (s == null) {
                    s = new Library.Platform();
                    s.name = "series-" + k;
                    s.system = "GENRE";
                    s.label = zh && g.seriesZh != null && !g.seriesZh.equals(Series.NONE) ? g.seriesZh : g.series;
                    s.color = Genres.COLORS[Math.floorMod(k.hashCode(), Genres.COLORS.length)];
                    s.art = "custom";
                    s.bgArt = "custom";
                    s.ownIcon = Theme.isCustom(s.name, Theme.CONSOLE);
                    by.put(k, s);
                }
                s.games.add(g);
            }
        }
        for (Library.Platform s : by.values()) if (s.games.size() >= 2) st.series.add(s);
        st.series.sort((a, b) -> b.games.size() != a.games.size() ? b.games.size() - a.games.size() : a.label.compareTo(b.label));
    }

    /** The folders the home page shows, in its own order: platforms, or genres in genre mode. */
    private List<Library.Platform> homePlatforms() {
        List<Library.Platform> out = new ArrayList<>();
        if (st.lib == null) return out;
        if (st.homeMode == 0) {
            for (Library.Platform p : st.lib.platforms) if (!st.hidden.contains(p.name)) out.add(p);
            return out;
        }
        for (Library.Platform p : st.lib.platforms) if (p.isCustom() && !st.hidden.contains(p.name)) out.add(p);
        for (Library.Platform p : st.homeMode == 2 ? st.series : st.genres) if (!st.hidden.contains(p.name)) out.add(p);
        for (Library.Platform p : st.lib.platforms) if (isNative(p) && !st.hidden.contains(p.name)) out.add(p);
        return out;
    }

    /** SELECT on the home page: platforms ⇄ genres. */
    void toggleHomeMode() {
        st.homeMode = (st.homeMode + 1) % 3;
        if (st.homeMode != 0 && st.genres.isEmpty()) rebuildGenres();
        st.lastSel.remove("#home");
        listener.onHomeMode(st.homeMode);
        goHome();
    }

    private void rebuildHome() {
        st.items.clear();
        // every built-in tile can be hidden (hidden set holds "tool:<id>"); START still opens Settings
        if (!st.recent.games.isEmpty()) {
            addTool(CONTINUE);
            addTool(st.recent);
        }
        if (!st.favorites.games.isEmpty()) addTool(st.favorites);
        st.items.addAll(homePlatforms());
        addTool(ADD);
        addTool(SEARCH);
        addTool(STATS);
        addTool(STORAGE);
        addTool(RANDOM);
        addTool(SETTINGS);
        addTool(DESKTOP);
        List<Object> ordered = inHomeOrder(st.items);
        st.items.clear();
        st.items.addAll(ordered);
    }

    /** Stable key of a home tile: "tool:<id>" for the built-in ones, "p:<name>" for platforms and folders. */
    String homeKey(Object o) {
        String id = toolId(o);
        if (id != null) return "tool:" + id;
        return o instanceof Library.Platform ? "p:" + ((Library.Platform) o).name : null;
    }

    /** The tiles in the user's order; tiles it does not mention yet (a new folder) keep their built-in place. */
    private List<Object> inHomeOrder(List<Object> def) {
        if (st.homeOrder.isEmpty()) return new ArrayList<>(def);
        Map<String, Object> byKey = new HashMap<>();
        for (Object o : def) byKey.put(homeKey(o), o);
        List<Object> out = new ArrayList<>();
        for (String k : st.homeOrder) {
            Object o = byKey.remove(k);
            if (o != null) out.add(o);
        }
        for (int i = 0; i < def.size(); i++) {
            Object o = def.get(i);
            if (byKey.containsKey(homeKey(o))) out.add(Math.min(i, out.size()), o);
        }
        return out;
    }

    // ---- moving home tiles: hold A (or long-press) to lift a tile, move it, A or release to drop, B to cancel ----

    private static final long HOLD_MS = 550;
    private boolean aHeld;
    private List<Object> beforeMove;
    private final Runnable lift = () -> { if (aHeld) startMove(); };

    boolean moving() { return st.moveFrom >= 0; }

    private void startMove() {
        if ((st.current != null && !canReorder()) || st.menu != null || selected() == null) return;
        st.moveFrom = st.sel;
        beforeMove = new ArrayList<>(st.items);
        listener.onCue(Sfx.OPEN);
        invalidate();
    }

    /** Moves the lifted tile to index {@code to}, the others close up around it. */
    private void moveTileTo(int to) {
        to = clampSel(to);
        if (to == st.sel) return;
        Object o = st.items.remove(st.sel);
        st.items.add(to, o);
        st.sel = to;
        listener.onCue(Sfx.MOVE);
        ensureVisible();
        invalidate();
    }

    private void endMove(boolean keep) {
        if (!moving()) return;
        Object o = selected();
        if (!keep && beforeMove != null) {
            st.items.clear();
            st.items.addAll(beforeMove);
            st.sel = clampSel(st.items.indexOf(o));
        }
        st.moveFrom = -1;
        beforeMove = null;
        listener.onCue(keep ? Sfx.OPEN : Sfx.BACK);
        if (keep && st.current != null) {
            // a folder's games: the whole order is kept, by game key
            List<String> order = new ArrayList<>();
            for (Object it : st.items) if (it instanceof Library.Game) order.add(((Library.Game) it).key());
            st.gameOrder.put(st.current.name, order);
            listener.onGameOrder(st.current.name, String.join("\n", order));
        } else if (keep) {
            // visible tiles in their new order, then the hidden or absent ones where they were
            List<String> order = new ArrayList<>();
            for (Object it : st.items) order.add(homeKey(it));
            for (String k : st.homeOrder) if (!order.contains(k)) order.add(k);
            st.homeOrder.clear();
            st.homeOrder.addAll(order);
            listener.onHomeOrder(String.join("\n", order));
        }
        ensureVisible();
        fireFocus();
    }

    /** A key released: on the home page A opens on release, so that holding it can lift the tile instead. */
    boolean onKeyUp(int code) {
        if (code != KeyEvent.KEYCODE_BUTTON_A && code != KeyEvent.KEYCODE_ENTER && code != KeyEvent.KEYCODE_DPAD_CENTER) return false;
        if (!aHeld) return false;
        aHeld = false;
        removeCallbacks(lift);
        if (!moving()) activate();
        return true;
    }

    /** True while A is held down on the home page (its key repeats are swallowed). */
    boolean holdingA() { return aHeld; }

    void goHome() {
        if (st.lib == null) return;
        if (st.current != null) st.lastSel.put(st.current.name, st.sel);
        st.current = null;
        rebuildHome();
        Integer s = st.lastSel.get("#home");
        st.sel = clampSel(s != null ? s : 0);
        jumpTo();
        fireFocus();
    }

    void open(Library.Platform pl) {
        st.lastSel.put(st.current == null ? "#home" : st.current.name, st.sel);
        st.current = pl;
        st.items.clear();
        st.items.addAll(arranged(pl));
        Integer s = st.lastSel.get(pl.name);
        st.sel = clampSel(s != null ? s : 0);
        jumpTo();
        fireFocus();
    }

    /** Opens the platform with this name (after the library was replaced), cursor at {@code sel}. */
    void openByName(String name, int sel) {
        for (Library.Platform p : st.lib.platforms) {
            if (!p.name.equals(name)) continue;
            open(p);
            st.sel = clampSel(sel);
            jumpTo();
            fireFocus();
            return;
        }
    }

    /** The platform's games after the language filter and the chosen sort order. */
    private List<Library.Game> arranged(Library.Platform pl) {
        List<Library.Game> gs = new ArrayList<>();
        for (Library.Game g : pl.games) if (st.langFilter == null || pl == st.recent || langOf(g).equals(st.langFilter)) gs.add(g);
        if (pl == st.recent || pl == st.results) return gs;
        switch (st.sortMode) {
            case SORT_NAME: {
                Collator col = Collator.getInstance(Locale.forLanguageTag(I18n.code()));
                gs.sort((a, b) -> col.compare(a.title, b.title));
                break;
            }
            case SORT_RECENT:
            case SORT_PLAYTIME: {
                Map<Library.Game, Long> v = new HashMap<>();
                for (Library.Game g : gs) v.put(g, listener.sortValue(g, st.sortMode));
                gs.sort((a, b) -> Long.compare(v.get(b), v.get(a))); // stable: unplayed keep their order
                break;
            }
            default:
                List<String> own = st.gameOrder.get(pl.name);
                if (own != null && !own.isEmpty()) return inOwnOrder(gs, own);
                break;
        }
        return gs;
    }

    /** Games in the player's order; games it does not mention yet (copied in later) keep their place among the rest. */
    private static List<Library.Game> inOwnOrder(List<Library.Game> def, List<String> order) {
        Map<String, Library.Game> byKey = new HashMap<>();
        for (Library.Game g : def) byKey.putIfAbsent(g.key(), g);
        List<Library.Game> out = new ArrayList<>();
        // by identity: two games can share a key (the same file on two storages), and neither may get lost
        java.util.Set<Library.Game> placed = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (String k : order) {
            Library.Game g = byKey.remove(k);
            if (g != null) { out.add(g); placed.add(g); }
        }
        for (int i = 0; i < def.size(); i++) {
            Library.Game g = def.get(i);
            if (!placed.contains(g)) out.add(Math.min(i, out.size()), g);
        }
        return out;
    }

    /** Whether the open folder's games can be put in the player's own order (its default order, nothing filtered). */
    boolean canReorder() {
        Library.Platform pl = st.current;
        return pl != null && !isPseudo(pl) && st.sortMode == SORT_DEFAULT && st.langFilter == null && st.items.size() > 1;
    }

    /** Whether the folder has an order of the player's own. */
    boolean hasOwnOrder(Library.Platform pl) {
        List<String> o = st.gameOrder.get(pl.name);
        return o != null && !o.isEmpty();
    }

    /** Back to the folder's own order (Pegasus or file names). */
    void resetGameOrder(Library.Platform pl) {
        st.gameOrder.remove(pl.name);
        listener.onGameOrder(pl.name, "");
        if (st.current == pl) reopenKeeping();
    }

    /** "简", "繁" or "英" from a tag in the title or file name such as "(简)" or "汉化", else "". */
    static String langOf(Library.Game g) {
        String s = g.title + " " + (g.file == null ? "" : g.file);
        if (s.contains("(简)") || s.contains("（简）") || s.contains("[简]") || s.contains("汉化") || s.contains("中文")) return "简";
        if (s.contains("(繁)") || s.contains("（繁）") || s.contains("[繁]") || s.contains("漢化")) return "繁";
        if (s.contains("(英)") || s.contains("（英）") || s.contains("[英]")) return "英";
        return "";
    }

    private void cycle(int d) {
        if (st.lib == null || st.lib.platforms.isEmpty()) return;
        List<Object> all = new ArrayList<>(homePlatforms());
        List<Library.Platform> ps = new ArrayList<>();
        for (Object o : inHomeOrder(all)) ps.add((Library.Platform) o);
        if (ps.isEmpty()) return;
        int i = st.current == null ? -1 : ps.indexOf(st.current);
        int n = i + d;
        if (n < -1) n = ps.size() - 1;
        if (n >= ps.size()) n = -1;
        if (n == -1) goHome(); else open(ps.get(n));
    }

    private boolean listView() { return st.current != null && ZOOM[st.zoom] == 0; }

    private int cols() { return st.current == null ? 3 : listView() ? 1 : ZOOM[st.zoom]; }

    private int clampSel(int i) { return Math.max(0, Math.min(i, st.items.size() - 1)); }

    Object selected() { return st.sel >= 0 && st.sel < st.items.size() ? st.items.get(st.sel) : null; }

    boolean menuOpen() { return st.menu != null; }

    private void setSel(int i) {
        i = clampSel(i);
        if (i == st.sel) return;
        listener.onCue(Sfx.MOVE);
        st.sel = i;
        ensureVisible();
        fireFocus();
    }

    private void move(int dx, int dy) {
        if (st.items.isEmpty()) return;
        if (listView() && dx != 0) {
            // one column: left/right turn a page
            int page = Math.max(1, (int) (viewH() / cellH()));
            setSel(st.sel + dx * page);
            return;
        }
        int c = cols(), n = st.items.size(), i = st.sel + dx;
        if (dy != 0) {
            int ni = i + dy * c;
            if (ni >= n) ni = (i / c < (n - 1) / c) ? n - 1 : i; // partial last row
            if (ni < 0) ni = i;
            i = ni;
        }
        setSel(i);
    }

    private void activate() {
        Object o = selected();
        listener.onCue(o instanceof Library.Game || o == CONTINUE ? Sfx.LAUNCH : Sfx.OPEN);
        if (o == CONTINUE) { if (!st.recent.games.isEmpty()) listener.onActivate(st.recent.games.get(0)); }
        else if (o == SETTINGS) listener.onSettings();
        else if (o == DESKTOP) listener.onOpenDesktop();
        else if (o == STATS) listener.onStats();
        else if (o == STORAGE) listener.onStorageTile();
        else if (o == RANDOM) listener.onRandom();
        else if (o == SEARCH) listener.onSearch();
        else if (o == ADD) listener.onAddFolder();
        else if (o instanceof Library.Platform) open((Library.Platform) o);
        else if (o instanceof Library.Game) listener.onActivate((Library.Game) o);
    }

    private void zoom() {
        if (st.current == null) return;
        st.zoom = (st.zoom + 1) % ZOOM.length;
        jumpTo();
        invalidate();
    }

    private static final S[] SORT_LABELS = {S.SORT_DEFAULT, S.SORT_NAME, S.SORT_RECENT, S.SORT_PLAYTIME};

    /** SELECT: sort order (applies to every platform) and language filter. */
    // ---- L2 / R2: jump to the previous / next first letter (pinyin initial for Chinese names) ----

    private char shownLetter;
    private long letterUntil;
    private final Runnable hideLetter = this::invalidate;

    private void jumpLetter(int dir) {
        if (st.current == null || st.items.isEmpty()) return;
        Object cur = selected();
        if (!(cur instanceof Library.Game)) return;
        char here = listener.initial((Library.Game) cur);
        // the nearest letter after (or before) this one that some game starts with; works in any sort order
        char best = 0;
        int at = -1;
        for (int i = 0; i < st.items.size(); i++) {
            Object o = st.items.get(i);
            if (!(o instanceof Library.Game)) continue;
            char c = listener.initial((Library.Game) o);
            boolean ok = dir > 0 ? c > here && (best == 0 || c < best) : c < here && (best == 0 || c > best);
            if (ok) { best = c; at = i; }
        }
        if (at < 0) {
            // past the last letter: wrap around to the first (or last)
            for (int i = 0; i < st.items.size(); i++) {
                Object o = st.items.get(i);
                if (!(o instanceof Library.Game)) continue;
                char c = listener.initial((Library.Game) o);
                boolean ok = dir > 0 ? (best == 0 || c < best) : (best == 0 || c > best);
                if (ok) { best = c; at = i; }
            }
        }
        if (at < 0) return;
        setSel(at);
        shownLetter = best;
        letterUntil = SystemClock.uptimeMillis() + 700;
        removeCallbacks(hideLetter);
        postDelayed(hideLetter, 720);
        invalidate();
    }

    /** The letter just jumped to, big in the middle of the grid for a moment. */
    private void drawLetter(Canvas c) {
        if (shownLetter == 0 || SystemClock.uptimeMillis() > letterUntil) return;
        float u = u(), s = 150 * u, cx = getWidth() / 2f, cy = headerH() + viewH() / 2;
        r2.set(cx - s / 2, cy - s / 2, cx + s / 2, cy + s / 2);
        p.setColor(0xB0202A36);
        c.drawRoundRect(r2, 30 * u, 30 * u, p);
        text(c, String.valueOf(shownLetter), cx, cy + 34 * u, 96 * u, 0xFFFFFFFF, true, Paint.Align.CENTER);
    }

    private void sort() {
        if (st.current == null) { toggleHomeMode(); return; }
        if (st.current == st.recent) return;
        Menu m = new Menu(t(S.SORT_FILTER), null);
        for (int i = 0; i < SORT_LABELS.length; i++) {
            int mode = i;
            m.add(t(S.SORT) + " · " + t(SORT_LABELS[i]), st.sortMode == i, () -> { st.sortMode = mode; reopenKeeping(); });
        }
        m.add(t(S.LANGUAGE) + " · " + t(S.ALL), st.langFilter == null, () -> { st.langFilter = null; reopenKeeping(); });
        for (int i = 0; i < LANG_TAGS.length; i++) {
            String tag = LANG_TAGS[i];
            m.add(t(S.LANGUAGE) + " · " + tag + "  " + I18n.NAMES[i], tag.equals(st.langFilter), () -> { st.langFilter = tag; reopenKeeping(); });
        }
        m.sel = st.sortMode;
        showMenu(m);
    }

    /** Rebuilds the current page after a sort/filter change, keeping the selected game when it is still there. */
    private void reopenKeeping() {
        Object keep = selected();
        Library.Platform cur = st.current;
        st.menu = null;
        st.lastSel.put(cur.name, 0);
        st.current = null;
        open(cur);
        int i = st.items.indexOf(keep);
        if (i >= 0) { st.sel = i; jumpTo(); }
        fireFocus();
    }

    // ---- menus ----

    void showMenu(Menu m) {
        if (m.page != null) m = m.page;   // a category is shown as its page
        st.menu = m;
        menuScrollToSel();
        invalidate();
    }

    void closeMenu() {
        st.menu = null;
        fireFocus();
    }

    private void menuBack() {
        if (st.menu.onBack != null) { st.menu.onBack.run(); return; }
        Menu up = st.menu.parent;
        if (up != null && up.page != null) up = up.page;   // back from a submenu lands on its settings page
        st.menu = up;
        if (st.menu == null) fireFocus(); else invalidate();
    }

    private void menuPick() {
        Menu m = st.menu;
        if (m.isPage()) {
            if (!m.inRows) { if (!m.tabs.get(m.tab).labels.isEmpty()) m.inRows = true; invalidate(); return; }
            m = m.tabs.get(m.tab);
            if (m.labels.isEmpty()) return;
        }
        Runnable a = m.actions.get(m.sel);
        if (a != null) a.run();
        invalidate();
    }

    private void menuScrollToSel() {
        Menu m = st.menu;
        if (m == null) return;
        if (m.isPage()) {
            Menu t = m.tabs.get(m.tab);
            int rows = pageRows();
            if (t.sel < t.top) t.top = t.sel;
            if (t.sel >= t.top + rows) t.top = t.sel - rows + 1;
            return;
        }
        if (m.sel < m.top) m.top = m.sel;
        if (m.sel >= m.top + MENU_ROWS) m.top = m.sel - MENU_ROWS + 1;
    }

    /** Keys on a settings page: the category list on the left, its rows on the right. */
    private boolean pageKey(int code) {
        Menu m = st.menu;
        Menu t = m.tabs.get(m.tab);
        switch (code) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN: {
                int d = code == KeyEvent.KEYCODE_DPAD_UP ? -1 : 1;
                if (m.inRows) {
                    int n = Math.max(0, Math.min(t.labels.size() - 1, t.sel + d));
                    if (n != t.sel) listener.onCue(Sfx.MOVE);
                    t.sel = n;
                } else {
                    setTab(m, m.tab + d);
                }
                menuScrollToSel();
                invalidate();
                return true;
            }
            case KeyEvent.KEYCODE_BUTTON_L1:
            case KeyEvent.KEYCODE_BUTTON_R1:
                setTab(m, m.tab + (code == KeyEvent.KEYCODE_BUTTON_L1 ? -1 : 1));
                menuScrollToSel();
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!m.inRows && !t.labels.isEmpty()) { m.inRows = true; listener.onCue(Sfx.MOVE); invalidate(); }
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (m.inRows) { m.inRows = false; listener.onCue(Sfx.MOVE); invalidate(); }
                return true;
            case KeyEvent.KEYCODE_BUTTON_A: case KeyEvent.KEYCODE_ENTER: case KeyEvent.KEYCODE_DPAD_CENTER: case KeyEvent.KEYCODE_BUTTON_START:
                listener.onCue(Sfx.OPEN);
                menuPick();
                return true;
            case KeyEvent.KEYCODE_BUTTON_B: case KeyEvent.KEYCODE_BACK: case KeyEvent.KEYCODE_ESCAPE:
                listener.onCue(Sfx.BACK);
                if (m.inRows) { m.inRows = false; invalidate(); } else menuBack();
                return true;
            default:
                return true;
        }
    }

    private void setTab(Menu m, int i) {
        int n = Math.max(0, Math.min(m.tabs.size() - 1, i));
        if (n == m.tab) return;
        listener.onCue(Sfx.MOVE);
        m.tab = n;
        if (m.onTab != null) m.onTab.accept(n);
    }

    /** Rows of a category that fit on the page. */
    private int pageRows() {
        RectF a = new RectF();
        pageRowsArea(a);
        return Math.max(1, (int) (a.height() / menuRowH()));
    }

    private void pageRowsArea(RectF out) {
        float u = u();
        out.set(pageSplit() + 18 * u, 128 * u, getWidth() - 40 * u, getHeight() - footerH() - 28 * u);
        // a category with explained rows keeps a line at the bottom of its card for the explanation
        Menu m = st.menu;
        if (m != null && m.isPage() && !m.tabs.get(m.tab).hints.isEmpty()) out.bottom -= 62 * u;
    }

    private float pageSplit() { return 40 * u() + 340 * u(); }

    /** Settings: the categories on a card at the left, the rows of one on a card at the right, on the home backdrop. */
    private void drawPage(Canvas c) {
        float u = u(), w = getWidth(), h = getHeight();
        Menu m = st.menu;
        p.setShader(new LinearGradient(0, 0, 0, h, Palette.shelfTop(), Palette.shelfBottom(), Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        p.setShader(null);
        Menu t = m.tabs.get(m.tab);
        // title: "Settings" and the category shown
        text(c, m.title, 44 * u, 74 * u, 38 * u, Palette.text(), true, Paint.Align.LEFT);
        float tx = 44 * u + measure(m.title, 38 * u) + 22 * u;
        p.setColor(t.color);
        c.drawCircle(tx + 9 * u, 61 * u, 9 * u, p);
        text(c, ellipsize(t.title, w * 0.45f, 28 * u), tx + 28 * u, 72 * u, 28 * u, Palette.text2(), true, Paint.Align.LEFT);
        if (m.subtitle != null)
            text(c, m.subtitle, w - 44 * u, 72 * u, 22 * u, Palette.text3(), false, Paint.Align.RIGHT);

        float rowH = menuRowH(), split = pageSplit(), bottom = h - footerH() - 20 * u;
        // the categories
        RectF left = new RectF(28 * u, 112 * u, split, bottom);
        card(c, left, 26 * u, Palette.card());
        for (int i = 0; i < m.tabs.size(); i++) {
            Menu tb = m.tabs.get(i);
            float y = left.top + 14 * u + i * rowH;
            r2.set(left.left + 12 * u, y, left.right - 12 * u, y + rowH - 8 * u);
            boolean on = i == m.tab;
            if (on) {
                p.setColor(m.inRows ? (tb.color & 0x00FFFFFF) | 0x2A000000 : tb.color);
                c.drawRoundRect(r2, 18 * u, 18 * u, p);
            }
            p.setColor(on && !m.inRows ? 0xFFFFFFFF : tb.color);
            c.drawCircle(r2.left + 30 * u, r2.centerY(), 10 * u, p);
            int fg = on && !m.inRows ? 0xFFFFFFFF : Palette.text();
            text(c, ellipsize(tb.title, r2.width() - 70 * u, 27 * u), r2.left + 56 * u, r2.centerY() + 10 * u, 27 * u, fg, on, Paint.Align.LEFT);
        }
        // the rows, as one card with lines between them
        RectF area = new RectF();
        pageRowsArea(area);
        int rows = Math.min(pageRows(), t.labels.size() - t.top);
        RectF right = new RectF(area.left - 6 * u, left.top, area.right + 12 * u, bottom);
        card(c, right, 26 * u, Palette.card());
        for (int k = 0; k < rows; k++) {
            int i = t.top + k;
            float y = area.top + k * rowH - 2 * u;
            r2.set(area.left + 6 * u, y, area.right, y + rowH - 8 * u);
            boolean on = m.inRows && i == t.sel;
            if (on) {
                p.setColor(Palette.accent());
                c.drawRoundRect(r2, 18 * u, 18 * u, p);
            } else if (k > 0 && !(m.inRows && i - 1 == t.sel)) {
                p.setColor(Palette.footerLine());
                c.drawRect(r2.left + 20 * u, y - 5 * u, r2.right - 20 * u, y - 3 * u, p);
            }
            String label = t.labels.get(i), value = null;
            int dot = label.indexOf("  ·  ");
            if (dot > 0) { value = label.substring(dot + 5); label = label.substring(0, dot); }
            int fg = on ? 0xFFFFFFFF : Palette.text();
            float lx = r2.left + 26 * u, rx = r2.right - 24 * u;
            if (t.toggles.contains(i)) {
                // a switch instead of "On ✓"
                boolean v = t.marks.get(i);
                float sw = 64 * u, sh = 36 * u;
                RectF tr = new RectF(rx - sw, r2.centerY() - sh / 2, rx, r2.centerY() + sh / 2);
                p.setColor(v ? (on ? 0xFFFFFFFF : Palette.accent()) : on ? 0x66FFFFFF : Palette.footerLine());
                c.drawRoundRect(tr, sh / 2, sh / 2, p);
                p.setColor(v ? (on ? Palette.accent() : 0xFFFFFFFF) : 0xFFFFFFFF);
                c.drawCircle(v ? tr.right - sh / 2 : tr.left + sh / 2, tr.centerY(), sh / 2 - 5 * u, p);
                rx = tr.left - 18 * u;
                value = null;
            } else if (t.marks.get(i)) {
                text(c, "✓", rx, r2.centerY() + 11 * u, 28 * u, on ? 0xFFFFFFFF : Palette.accent(), true, Paint.Align.RIGHT);
                rx -= 40 * u;
            } else if (t.actions.get(i) != null) {
                text(c, "›", rx, r2.centerY() + 12 * u, 36 * u, on ? 0xFFFFFFFF : Palette.text3(), false, Paint.Align.RIGHT);
                rx -= 34 * u;
            }
            float lw = Math.min(measure(label, 27 * u), (rx - lx) * (value == null ? 1f : 0.6f));
            text(c, ellipsize(label, lw + 1, 27 * u), lx, r2.centerY() + 10 * u, 27 * u, fg, on, Paint.Align.LEFT);
            if (value != null)
                text(c, ellipsize(value, rx - lx - lw - 30 * u, 24 * u), rx, r2.centerY() + 9 * u, 24 * u, on ? 0xDDFFFFFF : Palette.text3(), false, Paint.Align.RIGHT);
        }
        if (t.top > 0) text(c, "▲", (area.left + area.right) / 2, right.top + 14 * u, 16 * u, Palette.text3(), false, Paint.Align.CENTER);
        if (t.top + rows < t.labels.size()) text(c, "▼", (area.left + area.right) / 2, t.hints.isEmpty() ? right.bottom - 4 * u : area.bottom + 16 * u, 16 * u, Palette.text3(), false, Paint.Align.CENTER);
        // what the row under the cursor does, at the bottom of the card
        if (!t.hints.isEmpty()) {
            String hint = m.inRows ? t.hints.get(t.sel) : null;
            p.setColor(Palette.footerLine());
            c.drawRect(area.left + 20 * u, area.bottom + 22 * u, area.right - 8 * u, area.bottom + 24 * u, p);
            if (hint != null)
                text(c, ellipsize(hint, area.width() - 40 * u, 22 * u), area.left + 26 * u, area.bottom + 60 * u, 22 * u, Palette.text3(), false, Paint.Align.LEFT);
        }
        // the keys, like the home footer
        float top = h - footerH(), cy = top + footerH() / 2, x = pad();
        p.setColor(Palette.footer());
        c.drawRect(0, top, w, h, p);
        p.setColor(Palette.footerLine());
        c.drawRect(0, top, w, top + 2 * u, p);
        x = hintItem(c, x, cy, "A", t(S.OPEN));
        x = hintItem(c, x, cy, "B", t(S.BACK));
        hintItem(c, x, cy, "L/R", t(S.PAGE_TABS));
    }

    /** Y: emulator choice (and folder actions) for a game, emulator default for a platform, or edit a custom folder. */
    private void openEmulatorMenu() {
        Object o = selected();
        String tool = st.current == null ? toolId(o) : null;
        if (tool != null) {
            String label = o == st.recent ? toolName(tool, t(S.RECENT)) : o == st.favorites ? toolName(tool, t(S.FAVORITES))
                    : o == CONTINUE ? toolName(tool, t(S.CONTINUE)) : o == ADD ? toolName(tool, t(S.NEW_FOLDER))
                    : o == SEARCH ? toolName(tool, t(S.SEARCH)) : o == SETTINGS ? toolName(tool, t(S.SETTINGS))
                    : o == STATS ? toolName(tool, t(S.STATS)) : o == RANDOM ? toolName(tool, t(S.RANDOM_GAME))
                    : o == STORAGE ? toolName(tool, t(S.STORAGE)) : toolName(tool, t(S.ANDROID_DESKTOP));
            int def = o == st.recent ? RECENT_COLOR : o == st.favorites ? FAVORITES_COLOR : o == CONTINUE ? CONTINUE_COLOR
                    : o == ADD ? ADD_COLOR : o == SEARCH ? SEARCH_COLOR : o == SETTINGS ? SETTINGS_COLOR : o == STATS ? STATS_COLOR : o == RANDOM ? RANDOM_COLOR : o == STORAGE ? STORAGE_COLOR : DESKTOP_COLOR;
            listener.onEditTool(tool, label, toolColor(tool, def));
            return;
        }
        if (o instanceof Library.Platform) {
            Library.Platform pl = (Library.Platform) o;
            if (pl.isCustom()) { listener.onEditFolder(pl); return; }
            if (isGenre(pl)) { listener.onEditGenre(pl); return; }
            if (isPseudo(pl)) return;
            Menu m = new Menu(pl.title(), isNative(pl) ? null : I18n.f(S.GAMES_COUNT, pl.games.size()));
            if (!isNative(pl)) {
                String cur = listener.chosenCmd(o);
                m.add(door(t(S.PLATFORM_EMULATOR) + "  ·  " + Library.emulatorLabel(cur == null ? pl.launch : cur)), false, () -> {
                    Menu em = new Menu(t(S.CHOOSE_EMULATOR), I18n.f(S.PLATFORM_DEFAULT, pl.name));
                    em.parent = m;
                    em.add(I18n.f(S.PEGASUS_DEFAULT, Library.emulatorLabel(pl.launch)), cur == null, () -> { listener.onChoose(o, null); closeMenu(); });
                    for (String[] opt : Library.options(pl, getContext().getPackageManager()))
                        em.add(opt[0], opt[1].equals(cur), () -> { listener.onChoose(o, opt[1]); closeMenu(); });
                    showMenu(em);
                }).hint(t(S.H_PLATFORM_EMULATOR));
            }
            m.add(t(S.RENAME), false, () -> listener.onRenamePlatform(pl)).hint(t(S.H_RENAME_PLATFORM));
            if (!isNative(pl)) m.add(door(t(S.MERGE_FOLDER)), false, () -> listener.onMergePlatform(pl, m)).hint(t(S.H_MERGE));
            if (hasOwnOrder(pl)) m.add(t(S.RESET_ORDER), false, () -> { closeMenu(); resetGameOrder(pl); }).hint(t(S.H_RESET_ORDER));
            List<Library.Game> fresh = newIn(pl);
            if (!fresh.isEmpty()) m.add(I18n.f(S.MARK_ALL_SEEN, fresh.size()), false, () -> { closeMenu(); listener.onMarkSeen(fresh); }).hint(t(S.H_MARK_ALL_SEEN));
            m.add(t(S.HIDE_FOLDER), false, () -> { closeMenu(); listener.onHideFolder(pl); }).hint(t(S.H_HIDE_FOLDER));
            showMenu(m);
            return;
        }
        if (!(o instanceof Library.Game)) return;
        showGameMenu((Library.Game) o);
    }

    /** A row that opens a submenu ends with an arrow. */
    private static String door(String label) { return label + "  ›"; }

    /**
     * Y on a game: a handful of doors instead of one long list – emulator, favourite, folder, cheats, AI, the game's
     * details, and more – each with a line under the list saying what it leads to.
     */
    void showGameMenu(Library.Game g) {
        boolean emulated = g.pkg == null;
        Menu m = new Menu(g.title, g.platform.title());
        boolean unsorted = Library.UNSORTED.equals(g.platform.system);
        if (g.path != null && (unsorted || Library.chosenSystems.containsKey(g.path.getAbsolutePath())))
            m.add(door(t(S.PICK_SYSTEM)), false, () -> listener.onPickSystem(g)).hint(t(S.H_PICK_SYSTEM));
        if (emulated && !unsorted) {
            String cur = listener.chosenCmd(g);
            m.add(door(t(S.EMULATOR) + "  ·  " + (cur == null ? t(S.FOLLOW_PLATFORM) : Library.emulatorLabel(cur))), false, () -> {
                Menu em = new Menu(t(S.CHOOSE_EMULATOR), g.title);
                em.parent = m;
                em.add(t(S.FOLLOW_PLATFORM), cur == null, () -> { listener.onChoose(g, null); closeMenu(); }).hint(t(S.H_FOLLOW_PLATFORM));
                for (String[] opt : Library.options(g.platform, getContext().getPackageManager()))
                    em.add(opt[0], opt[1].equals(cur), () -> { listener.onChoose(g, opt[1]); closeMenu(); });
                showMenu(em);
            }).hint(t(S.H_EMULATOR));
        }
        boolean fav = listener.isFavorite(g);
        m.add(t(fav ? S.REMOVE_FAVORITE : S.ADD_FAVORITE), false, () -> { closeMenu(); listener.onToggleFavorite(g); }).hint(t(S.H_FAVORITE));
        m.add(door(t(S.ADD_TO_FOLDER)), false, () -> listener.onAddToFolder(g, m)).hint(t(S.H_FOLDER));
        Library.Platform here = st.current;
        if (here != null && here.isCustom() && here.smart == null)
            m.add(t(S.REMOVE_FROM_FOLDER), false, () -> { closeMenu(); listener.onRemoveFromFolder(here, g); }).hint(t(S.H_REMOVE_FOLDER));
        if (emulated) m.add(door(t(S.CHEATS)), false, () -> listener.onCheats(g)).hint(t(S.H_CHEATS));
        if (g.path != null) m.add(door(t(S.GROUP_AI)), false, () -> {
            Menu am = new Menu(t(S.GROUP_AI), g.title);
            am.parent = m;
            am.add(door(t(S.ASK_AI)), false, () -> listener.onAskAi(g, am)).hint(t(S.H_ASK_AI));
            am.add(t(S.HOW_TO), false, () -> listener.onHowTo(g)).hint(t(S.H_HOW_TO));
            showMenu(am);
        }).hint(t(S.H_AI));
        m.add(door(t(S.GROUP_INFO)), false, () -> showInfoMenu(g, m)).hint(t(S.H_INFO));
        m.add(door(t(S.GROUP_MORE)), false, () -> {
            Menu mm = new Menu(t(S.GROUP_MORE), g.title);
            mm.parent = m;
            if (emulated && g.path != null) mm.add(door(t(S.SAVES)), false, () -> listener.onSaves(g, mm)).hint(t(S.H_SAVES));
            if (isNew(g)) mm.add(t(S.MARK_SEEN), false, () -> { closeMenu(); listener.onMarkSeen(java.util.Collections.singletonList(g)); }).hint(t(S.H_MARK_SEEN));
            mm.add(t(S.HIDE_GAME), false, () -> { closeMenu(); listener.onHideGame(g); }).hint(t(S.H_HIDE_GAME));
            if (emulated && g.path != null) mm.add(t(S.DELETE_GAME), false, () -> listener.onDeleteGame(g, mm)).hint(t(S.H_DELETE_GAME));
            showMenu(mm);
        }).hint(t(S.H_MORE));
        showMenu(m);
    }

    /** The game's own information: details, genre, status, note, and what the upper screen shows for it. */
    private void showInfoMenu(Library.Game g, Menu parent) {
        Menu im = new Menu(t(S.GROUP_INFO), g.title);
        im.parent = parent;
        im.reopen = () -> showInfoMenu(g, parent);
        if (g.path != null) {
            im.add(door(t(S.DETAILS)), false, () -> listener.onDetails(g)).hint(t(S.H_DETAILS));
            im.add(door(t(S.GENRE) + "  ·  " + Genres.name(g.genre)), false, () -> listener.onPickGenre(g, im)).hint(t(S.H_GENRE));
            im.add(door(t(S.STATUS) + "  ·  " + listener.statusLine(g)), false, () -> listener.onStatus(g, im)).hint(t(S.H_STATUS));
            im.add(door(t(S.PLAY_HISTORY) + "  ·  " + listener.playLine(g)), false, () -> listener.onPlayHistory(g, im)).hint(t(S.H_PLAY_HISTORY));
            String note = listener.noteOf(g);
            im.add(door(t(S.NOTE) + (note == null ? "" : "  ·  " + note)), false, () -> listener.onNote(g, im)).hint(t(S.H_NOTE));
            im.add(door(t(S.AI_GAME_PICTURES)), false, () -> listener.onAiGamePictures(g, im)).hint(t(S.H_AI_GAME_PICTURES));
        }
        String[] modes = {"auto", "logo", "cover", "video"};
        S[] modeNames = {S.DISPLAY_AUTO, S.DISPLAY_LOGO, S.DISPLAY_COVER, S.DISPLAY_VIDEO};
        String mode = TopView.displayMode(g);
        int mi = java.util.Arrays.asList(modes).indexOf(mode);
        im.add(door(t(S.TOP_DISPLAY) + "  ·  " + t(modeNames[Math.max(0, mi)])), false, () -> {
            Menu dm = new Menu(t(S.TOP_DISPLAY), g.title);
            dm.parent = im;
            for (int i = 0; i < modes.length; i++) {
                String md = modes[i];
                dm.add(t(modeNames[i]), md.equals(mode), () -> {
                    if (TopView.prefs != null) {
                        if (md.equals("auto")) TopView.prefs.edit().remove("show:" + g.key()).apply();
                        else TopView.prefs.edit().putString("show:" + g.key(), md).apply();
                    }
                    closeMenu();
                    listener.onDisplayModeChanged(g);
                });
            }
            showMenu(dm);
        }).hint(t(S.H_TOP_DISPLAY));
        showMenu(im);
    }

    /** Custom folders are rebuilt as new objects on every change; find them again by name. */
    private int indexOfName(String name) {
        for (int i = 0; i < st.items.size(); i++)
            if (st.items.get(i) instanceof Library.Platform && ((Library.Platform) st.items.get(i)).name.equals(name)) return i;
        return -1;
    }

    /** Moves the selection to an item on the current page, if it is there. */
    void select(Object item) {
        int i = st.items.indexOf(item);
        if (i < 0) return;
        st.sel = i;
        ensureVisible();
        fireFocus();
    }

    /** Rebuilds the current page after folders were created, edited or deleted. */
    void refresh() {
        if (st.lib == null) return;
        rebuildGenres();
        if (st.current != null && isGenre(st.current) && !st.genres.contains(st.current) && !st.series.contains(st.current)) { goHome(); return; }
        if (st.current == null) {
            Object keep = selected();
            rebuildHome();
            int i = keep instanceof Library.Platform && ((Library.Platform) keep).isCustom()
                    ? indexOfName(((Library.Platform) keep).name) : st.items.indexOf(keep);
            st.sel = clampSel(i >= 0 ? i : st.sel);
        } else if (st.current.isCustom()) {
            Library.Platform cur = st.current;
            Library.Platform fresh = null;
            for (Library.Platform p : st.lib.platforms) if (p.name.equals(cur.name)) fresh = p;
            if (fresh == null) { goHome(); return; }
            int sel = st.sel;
            st.current = null;
            open(fresh);
            st.sel = clampSel(sel);
        } else if (st.current != st.recent && st.current != st.results) {
            // a game on this page was hidden: rebuild the page, keeping the cursor near where it was
            Library.Platform cur = st.current;
            int sel = st.sel;
            st.current = null;
            open(cur);
            st.sel = clampSel(sel);
        } else if (st.current == st.recent) {
            st.items.clear();
            st.items.addAll(st.recent.games);
            st.sel = clampSel(st.sel);
        }
        fireFocus();
    }

    private void fireFocus() {
        listener.onFocus(selected());
        invalidate();
    }

    boolean onKey(int code) {
        if (st.lib == null) return false;
        // a tip that has been read for a moment goes with the next key
        if (tip != null && SystemClock.uptimeMillis() - tipShown > TIP_MIN_MS) { tip = null; invalidate(); }
        if (st.menu != null && st.menu.isPage()) return pageKey(code);
        if (st.menu != null) {
            Menu m = st.menu;
            switch (code) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN: {
                    int n = code == KeyEvent.KEYCODE_DPAD_UP ? Math.max(0, m.sel - 1) : Math.min(m.labels.size() - 1, m.sel + 1);
                    if (n != m.sel) listener.onCue(Sfx.MOVE);
                    m.sel = n;
                    if (m.onSelect != null) m.onSelect.accept(n);
                    menuScrollToSel();
                    invalidate();
                    return true;
                }
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_BUTTON_L1:
                case KeyEvent.KEYCODE_BUTTON_R1: {
                    // a page at a time through long lists (hundreds of cheats)
                    boolean back = code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_BUTTON_L1;
                    int n = Math.max(0, Math.min(m.labels.size() - 1, m.sel + (back ? -MENU_ROWS : MENU_ROWS)));
                    if (n != m.sel) listener.onCue(Sfx.MOVE);
                    m.sel = n;
                    if (m.onSelect != null) m.onSelect.accept(n);
                    menuScrollToSel();
                    invalidate();
                    return true;
                }
                case KeyEvent.KEYCODE_BUTTON_A: case KeyEvent.KEYCODE_ENTER: case KeyEvent.KEYCODE_DPAD_CENTER: case KeyEvent.KEYCODE_BUTTON_START:
                    listener.onCue(Sfx.OPEN);
                    menuPick(); return true;
                case KeyEvent.KEYCODE_BUTTON_B: case KeyEvent.KEYCODE_BACK: case KeyEvent.KEYCODE_ESCAPE:
                    listener.onCue(Sfx.BACK);
                    menuBack(); return true;
                default: return true;
            }
        }
        if (moving()) {
            int c = cols();
            switch (code) {
                case KeyEvent.KEYCODE_DPAD_LEFT: moveTileTo(st.sel - 1); return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT: moveTileTo(st.sel + 1); return true;
                case KeyEvent.KEYCODE_DPAD_UP: if (st.sel - c >= 0) moveTileTo(st.sel - c); return true;
                case KeyEvent.KEYCODE_DPAD_DOWN: moveTileTo(Math.min(st.sel + c, st.items.size() - 1)); return true;
                case KeyEvent.KEYCODE_BUTTON_A: case KeyEvent.KEYCODE_ENTER: case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_BUTTON_START:
                    endMove(true); return true;
                case KeyEvent.KEYCODE_BUTTON_B: case KeyEvent.KEYCODE_BACK: case KeyEvent.KEYCODE_ESCAPE:
                    endMove(false); return true;
                default: return true;
            }
        }
        switch (code) {
            case KeyEvent.KEYCODE_DPAD_LEFT: move(-1, 0); return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT: move(1, 0); return true;
            case KeyEvent.KEYCODE_DPAD_UP: move(0, -1); return true;
            case KeyEvent.KEYCODE_DPAD_DOWN: move(0, 1); return true;
            case KeyEvent.KEYCODE_BUTTON_START:
                // START on the home page always reaches Settings, even with its tile hidden
                if (st.current == null) { listener.onCue(Sfx.OPEN); listener.onSettings(); } else activate();
                return true;
            case KeyEvent.KEYCODE_BUTTON_A: case KeyEvent.KEYCODE_ENTER: case KeyEvent.KEYCODE_DPAD_CENTER:
                if (st.current == null || canReorder()) {
                    // home page and folders: open on release; held long enough, the tile lifts to be moved
                    aHeld = true;
                    removeCallbacks(lift);
                    postDelayed(lift, HOLD_MS);
                } else activate();
                return true;
            case KeyEvent.KEYCODE_BUTTON_B: case KeyEvent.KEYCODE_BACK: case KeyEvent.KEYCODE_ESCAPE:
                if (st.current != null) {
                    listener.onCue(Sfx.BACK);
                    Runnable back = st.current == st.results ? st.listBack : null;
                    st.listBack = null;
                    goHome();
                    if (back != null) back.run();
                }
                return true;
            case KeyEvent.KEYCODE_BUTTON_X: zoom(); return true;
            case KeyEvent.KEYCODE_BUTTON_Y: openEmulatorMenu(); return true;
            case KeyEvent.KEYCODE_BUTTON_L1: case KeyEvent.KEYCODE_PAGE_UP: cycle(-1); return true;
            case KeyEvent.KEYCODE_BUTTON_R1: case KeyEvent.KEYCODE_PAGE_DOWN: cycle(1); return true;
            case KeyEvent.KEYCODE_BUTTON_SELECT: sort(); return true;
            case KeyEvent.KEYCODE_BUTTON_L2: jumpLetter(-1); return true;
            case KeyEvent.KEYCODE_BUTTON_R2: jumpLetter(1); return true;
            case KeyEvent.KEYCODE_BUTTON_THUMBR: case KeyEvent.KEYCODE_SEARCH: listener.onSearch(); return true;
            default: return false;
        }
    }

    // ---- geometry (designed on a 1280x960 canvas) ----

    private float u() { return getWidth() / 1280f; }
    private float headerH() { return 96 * u(); }
    private float footerH() { return 76 * u(); }
    private float pad() { return 30 * u(); }
    private float cellW() { return (getWidth() - 2 * pad()) / cols(); }
    private float cellH() { return st.current == null ? cellW() * 0.8f : listView() ? 116 * u() * Palette.textScale : cellW(); }
    private float viewH() { return getHeight() - headerH() - footerH(); }
    private float contentH() { int rows = (st.items.size() + cols() - 1) / cols(); return rows * cellH() + 40 * u(); }
    private float maxScroll() { return Math.max(0, contentH() - viewH()); }
    private float clamp(float s) { return Math.max(0, Math.min(maxScroll(), s)); }

    private void tileRect(int i, RectF out) {
        int c = i % cols(), row = i / cols();
        float x = pad() + c * cellW(), y = headerH() + 18 * u() + row * cellH() - st.scroll;
        float g = listView() ? 8 * u() : cellW() * (st.current == null ? 0.04f : 0.07f);
        out.set(x + g, y + g, x + cellW() - g, y + cellH() - g);
    }

    private void ensureVisible() {
        if (getWidth() == 0) return;
        float top = (st.sel / cols()) * cellH(), bottom = top + cellH() + 40 * u();
        float t = st.scrollTarget;
        if (top < t) t = top;
        if (bottom > t + viewH()) t = bottom - viewH();
        st.scrollTarget = clamp(t);
        postInvalidateOnAnimation();
    }

    private void jumpTo() {
        st.scrollTarget = st.scroll = 0;
        ensureVisible();
        st.scroll = st.scrollTarget;
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) { jumpTo(); }

    @Override public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            st.scroll = st.scrollTarget = clamp(scroller.getCurrY());
            postInvalidateOnAnimation();
        }
    }

    // ---- drawing ----

    @Override protected void onDraw(Canvas c) {
        float u = u(), w = getWidth(), h = getHeight();
        p.setShader(new LinearGradient(0, 0, 0, h, Palette.shelfTop(), Palette.shelfBottom(), Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        p.setShader(null);

        if (st.lib == null || st.message != null) {
            String m = st.message != null ? st.message : t(S.LOADING);
            float y = h / 2;
            for (String line : m.split("\n")) {
                text(c, line, w / 2, y, 30 * u, Palette.text2(), false, Paint.Align.CENTER);
                y += 46 * u;
            }
            if (st.menu != null) drawMenu(c);
            return;
        }

        if (Math.abs(st.scroll - st.scrollTarget) > 0.5f) {
            st.scroll += (st.scrollTarget - st.scroll) * 0.28f;
            postInvalidateOnAnimation();
        } else {
            st.scroll = st.scrollTarget;
        }

        c.save();
        c.clipRect(0, headerH(), w, h - footerH());
        int c0 = cols();
        int firstRow = (int) Math.max(0, (st.scroll - 18 * u) / cellH()), lastRow = (int) ((st.scroll + viewH()) / cellH()) + 1;
        for (int i = firstRow * c0; i < Math.min(st.items.size(), (lastRow + 1) * c0); i++) {
            if (i != st.sel) drawItem(c, i, false);
        }
        if (st.sel < st.items.size()) drawItem(c, st.sel, st.menu == null);
        c.restore();
        if (st.lib.platforms.isEmpty() || (st.current == st.results && st.items.isEmpty())) {
            // explain why the page is empty
            float y = headerH() + (st.items.isEmpty() ? 160 * u : cellH() + 90 * u);
            String msg = st.current == st.results ? t(S.NO_RESULTS) : t(S.NO_GAMES);
            for (String line : msg.split("\n")) {
                text(c, line, w / 2, y, 28 * u, Palette.text2(), false, Paint.Align.CENTER);
                y += 44 * u;
            }
        }

        drawLetter(c);
        drawHeader(c);
        drawFooter(c);
        drawTip(c);
        if (st.menu != null) drawMenu(c);
        if (dimmed) c.drawColor(0xFF000000); // screensaver: pure black, so the OLED pixels are off and nothing burns in
    }

    private boolean dimmed;
    /** Duo Lite (set from Settings): no pulsing selection frame. */
    static boolean lite;

    void setDimmed(boolean d) { dimmed = d; invalidate(); }

    // ---- a one-time tip: one line above the key legend, gone after a while or with a key press ----

    private String tip;
    private long tipShown;
    private static final long TIP_MS = 9000, TIP_MIN_MS = 2500;

    void showTip(String s) {
        tip = s;
        tipShown = SystemClock.uptimeMillis();
        invalidate();
        postDelayed(this::invalidate, TIP_MS + 50);
    }

    boolean tipShowing() { return tip != null && SystemClock.uptimeMillis() - tipShown < TIP_MS; }

    private void drawTip(Canvas c) {
        if (!tipShowing() || st.menu != null || moving()) return;
        float u = u(), size = 23 * u, h = 58 * u, maxW = getWidth() - 2 * pad();
        String s = ellipsize(tip, maxW - 90 * u, size);
        float w = Math.min(maxW, measure(s, size) + 96 * u);
        // fades in over the first fifth of a second
        float a = Math.min(1f, (SystemClock.uptimeMillis() - tipShown) / 200f);
        if (a < 1f) postInvalidateOnAnimation();
        RectF tr = new RectF((getWidth() - w) / 2, getHeight() - footerH() - h - 14 * u, (getWidth() + w) / 2, getHeight() - footerH() - 14 * u);
        int save = c.saveLayerAlpha(tr.left - 20 * u, tr.top - 20 * u, tr.right + 20 * u, tr.bottom + 20 * u, (int) (255 * a));
        card(c, tr, h / 2, Palette.card());
        p.setColor(Palette.accent());
        c.drawCircle(tr.left + h / 2 + 4 * u, tr.centerY(), 15 * u, p);
        text(c, "i", tr.left + h / 2 + 4 * u, tr.centerY() + 8 * u, 21 * u, 0xFFFFFFFF, true, Paint.Align.CENTER);
        text(c, s, tr.left + h + 16 * u, tr.centerY() + 8 * u, size, Palette.text(), false, Paint.Align.LEFT);
        c.restoreToCount(save);
    }

    private void drawItem(Canvas c, int i, boolean selected) {
        tileRect(i, r);
        if (r.bottom < headerH() || r.top > getHeight() - footerH()) return;
        Object o = st.items.get(i);
        c.save();
        boolean row = listView() && o instanceof Library.Game;
        boolean lifted = selected && moving();
        if (lifted) {
            // lifted: bigger, gently wobbling, until it is dropped
            c.scale(1.13f, 1.13f, r.centerX(), r.centerY());
            c.rotate(2.2f * (float) Math.sin(SystemClock.uptimeMillis() / 110.0), r.centerX(), r.centerY());
        } else if (selected) c.scale(row ? 1.015f : 1.07f, row ? 1.015f : 1.07f, r.centerX(), r.centerY());
        float rad = row ? 18 * u() : r.width() * (st.current == null ? 0.07f : 0.16f);
        if (o == st.recent) drawFan(c, rad, st.recent, toolColor("recent", RECENT_COLOR), toolName("recent", t(S.RECENT)));
        else if (o == st.favorites) drawFan(c, rad, st.favorites, toolColor("favorites", FAVORITES_COLOR), toolName("favorites", t(S.FAVORITES)));
        else if (o == CONTINUE) drawContinue(c, rad);
        else if (o == SETTINGS) drawSettings(c, rad);
        else if (o == STATS) drawStatsTile(c, rad);
        else if (o == STORAGE) drawStorageTile(c, rad);
        else if (o == RANDOM) drawRandomTile(c, rad);
        else if (o == DESKTOP) {
            folderShape(c, rad, toolColor("desktop", DESKTOP_COLOR));
            Bitmap ic = Theme.toolIcon("desktop");
            folderContent(c, ic != null ? ic : Theme.toolIcon("apps"), "apps", toolName("desktop", t(S.ANDROID_DESKTOP)), null);
        }
        else if (o == SEARCH) drawSearchTile(c, rad);
        else if (o == ADD) drawAddTile(c, rad);
        else if (o instanceof Library.Platform && (isGenre((Library.Platform) o) && !((Library.Platform) o).ownIcon || ((Library.Platform) o).smart != null && !((Library.Platform) o).games.isEmpty()))
            drawFan(c, rad, (Library.Platform) o, ((Library.Platform) o).accent(), ((Library.Platform) o).title());
        else if (o instanceof Library.Platform) drawFolder(c, (Library.Platform) o, rad);
        else if (listView()) drawRow(c, (Library.Game) o);
        else drawGame(c, (Library.Game) o, rad);
        if (selected) {
            // Duo Lite: a steady frame instead of a pulse, so an idle screen is not redrawn 25 times a second
            float a = lite ? 1f : 0.65f + 0.35f * (float) Math.sin(SystemClock.uptimeMillis() / 180.0);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(6 * u());
            p.setColor(Palette.accent());
            p.setAlpha((int) (255 * a));
            r2.set(r);
            r2.inset(-4 * u(), -4 * u());
            c.drawRoundRect(r2, rad + 4 * u(), rad + 4 * u(), p);
            p.setStyle(Paint.Style.FILL);
            p.setAlpha(255);
            if (!dimmed && !lite) postInvalidateDelayed(40);   // the pulse stops under the screensaver
        }
        c.restore();
    }

    private void card(Canvas c, RectF rr, float rad, int color) {
        p.setColor(color);
        p.setShadowLayer(10 * u(), 0, 4 * u(), Palette.shadow());
        c.drawRoundRect(rr, rad, rad, p);
        p.clearShadowLayer();
    }

    private void drawGame(Canvas c, Library.Game g, float rad) {
        card(c, r, rad, Palette.card());
        Bitmap b = icons.tile(g, redraw);
        path.reset();
        path.addRoundRect(r, rad, rad, Path.Direction.CW);
        c.save();
        c.clipPath(path);
        if (b != null) {
            r2.set(r);
            // every picture the same way, by the setting (not by whether the file format has transparency)
            float f = TILE_PAD[Math.max(0, Math.min(3, st.tilePad))];
            if (f > 0) r2.inset(r.width() * f, r.height() * f);
            src.set(0, 0, b.getWidth(), b.getHeight());
            c.drawBitmap(b, src, r2, p);
        } else {
            p.setColor(0xFF000000 | (g.title.hashCode() & 0x7F7F7F) | 0x404040);
            c.drawRect(r, p);
            String s = g.title.length() > 4 ? g.title.substring(0, 4) : g.title;
            text(c, s, r.centerX(), r.centerY() + r.height() * 0.08f, r.height() * 0.2f, 0xFFFFFFFF, true, Paint.Align.CENTER);
        }
        c.restore();
        if (isNew(g)) newBadge(c);
    }

    private static final long NEW_MS = 7L * 24 * 3600_000;
    /** Margin around game pictures for Settings → Cover margin (fill, small, medium, large). */
    static final float[] TILE_PAD = {0f, 0.05f, 0.09f, 0.13f};

    /** Settings → NEW badges; and the games the player marked as seen. */
    static boolean newBadges = true;
    static final java.util.Set<String> seenNew = new java.util.HashSet<>();

    /** Added in the last week, not played yet and not marked as seen. */
    boolean isNew(Library.Game g) {
        return newBadges && g.added > 0 && System.currentTimeMillis() - g.added < NEW_MS && !seenNew.contains(g.key())
                && listener.playedLine(g) == null;
    }

    /** The games of a folder that still show a NEW badge. */
    List<Library.Game> newIn(Library.Platform pl) {
        List<Library.Game> out = new ArrayList<>();
        for (Library.Game g : pl.games) if (isNew(g)) out.add(g);
        return out;
    }

    private void newBadge(Canvas c) {
        float u = u(), h = 30 * u, w = 68 * u;
        r2.set(r.left + 8 * u, r.top + 8 * u, r.left + 8 * u + w, r.top + 8 * u + h);
        p.setShadowLayer(4 * u, 0, 2 * u, 0x44000000);
        p.setColor(0xFFE8463E);
        c.drawRoundRect(r2, h / 2, h / 2, p);
        p.clearShadowLayer();
        text(c, "NEW", r2.centerX(), r2.centerY() + 7 * u, 19 * u, 0xFFFFFFFF, true, Paint.Align.CENTER);
    }

    /** List view row: icon, name, platform and play time. */
    private void drawRow(Canvas c, Library.Game g) {
        float u = u(), h = r.height(), rad = 18 * u;
        card(c, r, rad, Palette.card());
        float s = h - 20 * u;
        r2.set(r.left + 10 * u, r.top + 10 * u, r.left + 10 * u + s, r.top + 10 * u + s);
        Bitmap b = icons.tile(g, redraw);
        c.save();
        path.reset();
        path.addRoundRect(r2, 12 * u, 12 * u, Path.Direction.CW);
        c.clipPath(path);
        if (b != null) {
            src.set(0, 0, b.getWidth(), b.getHeight());
            c.drawBitmap(b, src, r2, p);
        } else {
            p.setColor(Palette.cardSoft());
            c.drawRect(r2, p);
        }
        c.restore();
        float tx = r2.right + 24 * u, maxW = r.right - tx - 24 * u;
        text(c, ellipsize(g.title, maxW, 30 * u), tx, r.top + h * 0.45f, 30 * u, Palette.text(), true, Paint.Align.LEFT);
        String sub = (isNew(g) ? "● NEW  ·  " : "") + g.platform.title();
        String played = listener.playedLine(g);
        if (played != null && !played.isEmpty()) sub += "  ·  " + played;
        text(c, ellipsize(sub, maxW, 22 * u), tx, r.top + h * 0.78f, 22 * u, Palette.text2(), false, Paint.Align.LEFT);
    }

    /** Colours of the home-page tool folders. */
    static final int RECENT_COLOR = 0xFFF0943A, SEARCH_COLOR = 0xFF2F8BFF, SETTINGS_COLOR = 0xFF5E7186, ADD_COLOR = 0xFF9B7BE0,
            FAVORITES_COLOR = 0xFFE8B52E, CONTINUE_COLOR = 0xFF2BAE66, DESKTOP_COLOR = 0xFF3D8B8B, STATS_COLOR = 0xFF6366F1, RANDOM_COLOR = 0xFFE07A3F, STORAGE_COLOR = 0xFF14A38B;

    /** Folder body (tab + front) in {@code color}, drawn into {@link #r}. */
    private void folderShape(Canvas c, float rad, int color) {
        float w = r.width(), h = r.height();
        r2.set(r.left, r.top, r.left + w * 0.42f, r.top + h * 0.3f);
        p.setColor(mul(color, 0.72f));
        c.drawRoundRect(r2, rad, rad, p);
        r2.set(r.left, r.top + h * 0.12f, r.right, r.bottom);
        p.setShadowLayer(10 * u(), 0, 4 * u(), Palette.shadow());
        p.setShader(new LinearGradient(0, r2.top, 0, r2.bottom, lighten(color), color, Shader.TileMode.CLAMP));
        c.drawRoundRect(r2, rad, rad, p);
        p.setShader(null);
        p.clearShadowLayer();
    }

    /** Picture in the folder's top-right area, label bottom-left, small note bottom-right. */
    private void folderContent(Canvas c, Bitmap pic, String fallback, String label, String note) {
        float w = r.width(), h = r.height();
        ConsoleDrawable d = new ConsoleDrawable(fallback, pic, false);
        d.setBounds((int) (r.left + w * 0.4f), (int) (r.top + h * 0.18f), (int) (r.right - w * 0.05f), (int) (r.bottom - h * 0.28f));
        d.draw(c);
        text(c, ellipsize(label, w * 0.7f, h * 0.16f), r.left + w * 0.07f, r.bottom - h * 0.12f, h * 0.16f, 0xFFFFFFFF, true, Paint.Align.LEFT);
        if (note != null)
            text(c, note, r.right - w * 0.07f, r.bottom - h * 0.12f, h * 0.12f, 0xDDFFFFFF, true, Paint.Align.RIGHT);
    }

    private void drawFolder(Canvas c, Library.Platform pl, float rad) {
        folderShape(c, rad, pl.accent());
        float w = r.width(), h = r.height();
        ConsoleDrawable d = ConsoleDrawable.of(pl, false);
        d.setBounds((int) (r.left + w * 0.4f), (int) (r.top + h * 0.18f), (int) (r.right - w * 0.05f), (int) (r.bottom - h * 0.28f));
        d.draw(c);
        String label = pl.label != null || isNative(pl) || pl.isCustom() || Library.UNSORTED.equals(pl.name) ? pl.title() : Library.shortLabel(pl.name);
        text(c, ellipsize(label, w * 0.7f, h * 0.16f), r.left + w * 0.07f, r.bottom - h * 0.12f, h * 0.16f, 0xFFFFFFFF, true, Paint.Align.LEFT);
        text(c, String.valueOf(pl.games.size()), r.right - w * 0.07f, r.bottom - h * 0.12f, h * 0.12f, 0xDDFFFFFF, true, Paint.Align.RIGHT);
    }

    /** The last game played, large, with a play mark: A jumps straight back into it. */
    private void drawContinue(Canvas c, float rad) {
        folderShape(c, rad, toolColor("continue", CONTINUE_COLOR));
        if (st.recent.games.isEmpty()) return;
        Library.Game g = st.recent.games.get(0);
        float w = r.width(), h = r.height(), s = h * 0.44f;
        r2.set(r.right - w * 0.07f - s, r.top + h * 0.19f, r.right - w * 0.07f, r.top + h * 0.19f + s);
        p.setColor(0xFFFFFFFF);
        p.setShadowLayer(6 * u(), 0, 3 * u(), 0x40000000);
        c.drawRoundRect(r2, s * 0.16f, s * 0.16f, p);
        p.clearShadowLayer();
        Bitmap b = icons.tile(g, redraw);
        if (b != null) {
            c.save();
            path.reset();
            path.addRoundRect(r2, s * 0.16f, s * 0.16f, Path.Direction.CW);
            c.clipPath(path);
            RectF ir = new RectF(r2);
            if (b.hasAlpha()) ir.inset(s * 0.1f, s * 0.1f);
            src.set(0, 0, b.getWidth(), b.getHeight());
            c.drawBitmap(b, src, ir, p);
            c.restore();
        }
        // play icon in front of the label, on its own line below the picture
        float br = h * 0.075f, bx = r.left + w * 0.07f + br, by = r.bottom - h * 0.12f - h * 0.055f;
        p.setColor(0xFFFFFFFF);
        c.drawCircle(bx, by, br, p);
        p.setColor(toolColor("continue", CONTINUE_COLOR));
        path.reset();
        path.moveTo(bx - br * 0.32f, by - br * 0.45f);
        path.lineTo(bx + br * 0.5f, by);
        path.lineTo(bx - br * 0.32f, by + br * 0.45f);
        path.close();
        c.drawPath(path, p);
        float lx = bx + br + w * 0.03f;
        text(c, ellipsize(toolName("continue", t(S.CONTINUE)), r.right - w * 0.07f - lx, h * 0.15f), lx, r.bottom - h * 0.12f, h * 0.15f, 0xFFFFFFFF, true, Paint.Align.LEFT);
        text(c, ellipsize(g.title, r2.left - r.left - w * 0.12f, h * 0.09f), r.left + w * 0.07f, r.top + h * 0.3f, h * 0.09f, 0xDDFFFFFF, true, Paint.Align.LEFT);
    }

    private void drawFan(Canvas c, float rad, Library.Platform pl, int color, String label) {
        folderShape(c, rad, color);
        float w = r.width(), h = r.height();
        // the first three games fanned out in the picture area
        int n = Math.min(3, pl.games.size());
        float s = h * 0.36f, cx = r.left + w * 0.66f, cy = r.top + h * 0.42f;
        float[] dx = {-0.42f, 0.42f, 0f}, rot = {-12, 12, 0};
        int[] order = n == 3 ? new int[]{2, 1, 0} : n == 2 ? new int[]{1, 0} : new int[]{0};
        for (int i : order) {
            Bitmap b = icons.tile(pl.games.get(i), redraw);
            int slot = n == 1 ? 2 : i == 0 ? 2 : i == 1 ? 0 : 1;
            c.save();
            c.translate(cx + dx[slot] * s, cy);
            c.rotate(rot[slot]);
            r2.set(-s / 2, -s / 2, s / 2, s / 2);
            p.setColor(0xFFFFFFFF);
            p.setShadowLayer(6 * u(), 0, 3 * u(), 0x40000000);
            c.drawRoundRect(r2, s * 0.16f, s * 0.16f, p);
            p.clearShadowLayer();
            if (b != null) {
                path.reset();
                path.addRoundRect(r2, s * 0.16f, s * 0.16f, Path.Direction.CW);
                c.clipPath(path);
                float f = TILE_PAD[Math.max(0, Math.min(3, st.tilePad))];
                if (f > 0) r2.inset(s * f, s * f);
                src.set(0, 0, b.getWidth(), b.getHeight());
                c.drawBitmap(b, src, r2, p);
            } else {
                // no picture: the same coloured card with the first letters of the title as in the game grid
                Library.Game g = pl.games.get(i);
                path.reset();
                path.addRoundRect(r2, s * 0.16f, s * 0.16f, Path.Direction.CW);
                c.clipPath(path);
                p.setColor(0xFF000000 | (g.title.hashCode() & 0x7F7F7F) | 0x404040);
                c.drawRect(r2, p);
                String t = g.title.length() > 4 ? g.title.substring(0, 4) : g.title;
                text(c, t, 0, s * 0.08f, s * 0.2f, 0xFFFFFFFF, true, Paint.Align.CENTER);
            }
            c.restore();
        }
        text(c, ellipsize(label, w * 0.7f, h * 0.16f), r.left + w * 0.07f, r.bottom - h * 0.12f, h * 0.16f, 0xFFFFFFFF, true, Paint.Align.LEFT);
        text(c, String.valueOf(pl.games.size()), r.right - w * 0.07f, r.bottom - h * 0.12f, h * 0.12f, 0xDDFFFFFF, true, Paint.Align.RIGHT);
    }

    /** Statistics tile: three rising bars drawn in white. */
    private void drawRandomTile(Canvas c, float rad) {
        folderShape(c, rad, toolColor("random", RANDOM_COLOR));
        Bitmap ic = Theme.toolIcon("random");
        if (ic != null) {
            folderContent(c, ic, "apps", toolName("random", t(S.RANDOM_GAME)), null);
            return;
        }
        // a white die showing five
        float w = r.width(), h = r.height(), s = h * 0.36f, x = r.right - w * 0.1f - s, y = r.top + h * 0.2f;
        r2.set(x, y, x + s, y + s);
        p.setColor(0xFFFFFFFF);
        c.drawRoundRect(r2, s * 0.2f, s * 0.2f, p);
        p.setColor(toolColor("random", RANDOM_COLOR));
        float d = s * 0.09f;
        float[][] pips = {{0.27f, 0.27f}, {0.73f, 0.27f}, {0.5f, 0.5f}, {0.27f, 0.73f}, {0.73f, 0.73f}};
        for (float[] q : pips) c.drawCircle(x + s * q[0], y + s * q[1], d, p);
        text(c, ellipsize(toolName("random", t(S.RANDOM_GAME)), w * 0.7f, h * 0.16f), r.left + w * 0.07f, r.bottom - h * 0.12f, h * 0.16f, 0xFFFFFFFF, true, Paint.Align.LEFT);
    }

    /** Opens the game's platform with the cursor on it. */
    void reveal(Library.Game g) {
        if (g == null || g.platform == null) return;
        open(g.platform);
        int i = st.items.indexOf(g);
        if (i >= 0) {
            st.sel = i;
            jumpTo();
            fireFocus();
        }
    }

    /** The storage tile: its picture, or a drawn gauge of how full the card is. */
    private void drawStorageTile(Canvas c, float rad) {
        folderShape(c, rad, toolColor("storage", STORAGE_COLOR));
        Bitmap ic = Theme.toolIcon("storage");
        String name = toolName("storage", t(S.STORAGE));
        if (ic != null) {
            folderContent(c, ic, "apps", name, null);
            return;
        }
        float w = r.width(), h = r.height();
        r2.set(r.left + w * 0.5f, r.top + h * 0.3f, r.right - w * 0.1f, r.top + h * 0.44f);
        p.setColor(0x55FFFFFF);
        c.drawRoundRect(r2, r2.height() / 2, r2.height() / 2, p);
        float used = storageUsed < 0 ? 0.6f : storageUsed;
        r2.right = r2.left + r2.width() * Math.max(0.08f, Math.min(1f, used));
        p.setColor(0xFFFFFFFF);
        c.drawRoundRect(r2, r2.height() / 2, r2.height() / 2, p);
        text(c, ellipsize(name, w * 0.7f, h * 0.16f), r.left + w * 0.07f, r.bottom - h * 0.12f, h * 0.16f, 0xFFFFFFFF, true, Paint.Align.LEFT);
    }

    /** How full the games' card is (0-1), for the drawn storage gauge; negative while unknown. */
    static float storageUsed = -1;

    private void drawStatsTile(Canvas c, float rad) {
        folderShape(c, rad, toolColor("stats", STATS_COLOR));
        Bitmap ic = Theme.toolIcon("stats");
        if (ic != null) {
            folderContent(c, ic, "apps", toolName("stats", t(S.STATS)), null);
            return;
        }
        float w = r.width(), h = r.height(), bw = w * 0.085f, gap = w * 0.045f, base = r.top + h * 0.66f;
        float x = r.right - w * 0.1f - 3 * bw - 2 * gap;
        float[] hs = {0.18f, 0.3f, 0.42f};
        for (int i = 0; i < 3; i++) {
            r2.set(x, base - h * hs[i], x + bw, base);
            p.setColor(i == 2 ? 0xFFFFFFFF : 0xCCFFFFFF);
            p.setShadowLayer(5 * u(), 0, 2 * u(), 0x33000000);
            c.drawRoundRect(r2, bw * 0.3f, bw * 0.3f, p);
            p.clearShadowLayer();
            x += bw + gap;
        }
        text(c, ellipsize(toolName("stats", t(S.STATS)), w * 0.7f, h * 0.16f), r.left + w * 0.07f, r.bottom - h * 0.12f, h * 0.16f, 0xFFFFFFFF, true, Paint.Align.LEFT);
    }

    private void drawSettings(Canvas c, float rad) {
        folderShape(c, rad, toolColor("settings", SETTINGS_COLOR));
        folderContent(c, Theme.toolIcon("settings"), "gear", toolName("settings", t(S.SETTINGS)), null);
    }

    private void drawAddTile(Canvas c, float rad) {
        folderShape(c, rad, toolColor("add", ADD_COLOR));
        // dashed inner outline, so it reads as "make a new one"
        float inset = 8 * u();
        r2.set(r.left + inset, r.top + r.height() * 0.12f + inset, r.right - inset, r.bottom - inset);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.5f * u());
        p.setPathEffect(new android.graphics.DashPathEffect(new float[]{12 * u(), 9 * u()}, 0));
        p.setColor(0x99FFFFFF);
        c.drawRoundRect(r2, rad * 0.8f, rad * 0.8f, p);
        p.setPathEffect(null);
        p.setStyle(Paint.Style.FILL);
        folderContent(c, Theme.toolIcon("custom"), "apps", toolName("add", t(S.NEW_FOLDER)), "+");
    }

    /** Installed Android games and apps, which run without an emulator. */
    private static boolean isNative(Library.Platform p) { return p.system.equals("ANDROID") || p.system.equals("APPS"); }

    private void drawSearchTile(Canvas c, float rad) {
        folderShape(c, rad, toolColor("search", SEARCH_COLOR));
        folderContent(c, Theme.toolIcon("search"), "search", toolName("search", t(S.SEARCH)), "R3");
    }

    /** A list of games under its own title (e.g. most played), shown on the results page. */
    void showList(String title, List<Library.Game> games) { showList(title, games, null); }

    /** @param back what B does on the list instead of going home, or null */
    void showList(String title, List<Library.Game> games, Runnable back) {
        showResults("", games);
        st.listTitle = title;
        st.listBack = back;
        invalidate();
    }

    /** Shows search results as their own page; called again as the query changes. */
    void showResults(String query, List<Library.Game> games) {
        st.listTitle = null;
        st.listBack = null;
        st.query = query;
        st.results.games.clear();
        st.results.games.addAll(games);
        if (st.current != st.results) {
            open(st.results);
        } else {
            st.items.clear();
            st.items.addAll(games);
            st.sel = clampSel(0);
            jumpTo();
            fireFocus();
        }
    }

    private void pill(Canvas c, RectF rr, String s, boolean on) {
        p.setColor(on ? Palette.accent() : Palette.pill());
        p.setShadowLayer(6 * u(), 0, 2 * u(), 0x22000000);
        c.drawRoundRect(rr, rr.height() / 2, rr.height() / 2, p);
        p.clearShadowLayer();
        text(c, s, rr.centerX(), rr.centerY() + rr.height() * 0.17f, rr.height() * 0.45f, on ? 0xFFFFFFFF : Palette.pillText(), true, Paint.Align.CENTER);
    }

    private void drawHeader(Canvas c) {
        float u = u(), y = 22 * u, hh = 52 * u, x = pad();
        hitL.set(x, y + 6 * u, x + 54 * u, y + hh - 6 * u);
        pill(c, hitL, "L", false);
        x = hitL.right + 14 * u;
        String home = "⌂ " + t(S.HOME);
        hitHome.set(x, y, x + Math.max(150 * u, measure(home, hh * 0.45f) + 60 * u), y + hh);
        pill(c, hitHome, home, st.current == null);
        x = hitHome.right + 14 * u;
        if (st.current != null) {
            String s = st.current == st.recent ? t(S.RECENT) : st.current == st.favorites ? t(S.FAVORITES)
                    : st.current == st.results ? (st.listTitle != null ? st.listTitle : I18n.f(S.SEARCH_RESULTS, st.query)) : st.current.title();
            s = ellipsize(s, 420 * u, hh * 0.45f);
            r2.set(x, y, x + measure(s, hh * 0.45f) + 60 * u, y + hh);
            pill(c, r2, s, true);
            x = r2.right + 14 * u;
        }
        hitR.set(x, y + 6 * u, x + 54 * u, y + hh - 6 * u);
        pill(c, hitR, "R", false);
        // search button at the right edge, count just left of it
        float sw = measure("⌕ " + t(S.SEARCH), hh * 0.45f) + 50 * u;
        hitSearch.set(getWidth() - pad() - sw, y, getWidth() - pad(), y + hh);
        pill(c, hitSearch, "⌕ " + t(S.SEARCH), false);
        int shown = homePlatforms().size();
        String right = st.current == null ? I18n.f(st.homeMode == 1 ? S.GENRES_COUNT : st.homeMode == 2 ? S.SERIES_COUNT : S.PLATFORMS_COUNT, shown) : I18n.f(S.GAMES_COUNT, st.items.size());
        float rx = hitSearch.left - 20 * u;
        if (rx - measure(right, hh * 0.42f) > hitR.right + 10 * u)
            text(c, right, rx, y + hh * 0.68f, hh * 0.42f, Palette.text2(), true, Paint.Align.RIGHT);
    }

    private void drawFooter(Canvas c) {
        float u = u(), top = getHeight() - footerH(), cy = top + footerH() / 2;
        p.setColor(Palette.footer());
        c.drawRect(0, top, getWidth(), getHeight(), p);
        p.setColor(Palette.footerLine());
        c.drawRect(0, top, getWidth(), top + 2 * u, p);
        float x = pad();
        Object sel = selected();
        if (moving()) {
            x = hintItem(c, x, cy, "✥", t(S.MOVE_TILE));
            x = hintItem(c, x, cy, "A", t(S.DROP_TILE));
            hintItem(c, x, cy, "B", t(S.CANCEL));
            hitZoom.setEmpty();
            hitSort.setEmpty();
            return;
        }
        if (st.typing) {
            x = hintItem(c, x, cy, "A", t(S.KB_TYPE));
            x = hintItem(c, x, cy, "B", t(S.KB_DELETE));
            x = hintItem(c, x, cy, "X", t(S.SPACE_KEY));
            x = hintItem(c, x, cy, "Y", t(S.SYSTEM_KEYBOARD));
            x = hintItem(c, x, cy, "L", t(S.VOICE));
            x = hintItem(c, x, cy, "START", t(S.KB_DONE));
            askHint(c, x, cy);
            hitZoom.setEmpty();
            hitSort.setEmpty();
            return;
        }
        x = hintItem(c, x, cy, "A", sel instanceof Library.Game ? t(S.PLAY) : t(S.OPEN));
        if (st.current != null) x = hintItem(c, x, cy, "B", t(S.BACK));
        if (st.current == null && toolId(sel) != null) {
            x = hintItem(c, x, cy, "Y", t(S.EDIT_FOLDER));
        } else if (sel instanceof Library.Game) {
            x = hintItem(c, x, cy, "Y", t(S.GAME_MENU));
        } else if (sel instanceof Library.Platform && ((Library.Platform) sel).isCustom()) {
            x = hintItem(c, x, cy, "Y", t(S.EDIT_FOLDER));
        } else if (sel instanceof Library.Platform && isGenre((Library.Platform) sel)) {
            x = hintItem(c, x, cy, "Y", t(S.ICON));
        } else if (sel instanceof Library.Platform && !isPseudo((Library.Platform) sel)) {
            x = hintItem(c, x, cy, "Y", t(S.GAME_MENU));
        }
        hitZoom.setEmpty();
        hitSort.setEmpty();
        if (st.current == null) {
            float x0 = x;
            x = hintItem(c, x, cy, "SEL", st.homeMode == 0 ? t(S.HOME_BY_GENRE) : st.homeMode == 1 ? t(S.HOME_BY_SERIES) : t(S.HOME_BY_PLATFORM));
            hitSort.set(x0, top, x, getHeight());
        }
        if (st.current != null) {
            String[] z = {t(S.ZOOM_L), t(S.ZOOM_M), t(S.ZOOM_S), t(S.ZOOM_XS), t(S.LIST_VIEW)};
            float x0 = x;
            x = hintItem(c, x, cy, "X", t(S.SIZE) + " · " + z[st.zoom]);
            hitZoom.set(x0, top, x, getHeight());
            if (st.current != st.recent) {
                x0 = x;
                x = hintItem(c, x, cy, "SEL", t(S.SORT) + " · " + t(SORT_LABELS[st.sortMode]) + (st.langFilter == null ? "" : " · " + st.langFilter));
                hitSort.set(x0, top, x, getHeight());
            }
        }
        x = askHint(c, x, cy);
        if (st.current == null) hintItem(c, x, cy, "A", t(S.HOLD_TO_MOVE));   // the first to go when space runs out
    }

    /** "R  Hold: ask AI" at the end of the footer, when an AI key is set and it fits. */
    private float askHint(Canvas c, float x, float cy) {
        if (!st.askAi) return x;
        x = hintItem(c, x, cy, "R", t(S.R_ASK_AI));
        if (!st.typing) x = hintItem(c, x, cy, "L", t(S.L_VOICE_SEARCH));
        return x;
    }

    /** One key and what it does; left out (with all after it) when it would run past the edge, as with large text. */
    private float hintItem(Canvas c, float x, float cy, String key, String label) {
        float u = u(), rad = 17 * u, kw = key.length() > 1 ? 56 * u : rad * 2;
        if (x + kw + 10 * u + measure(label, 25 * u) > getWidth() - pad()) return getWidth();
        r2.set(x, cy - rad, x + kw, cy + rad);
        p.setColor(Palette.key());
        c.drawRoundRect(r2, rad, rad, p);
        text(c, key, r2.centerX(), cy + 7 * u, key.length() > 1 ? 16 * u : 20 * u, Palette.keyText(), true, Paint.Align.CENTER);
        float tx = r2.right + 10 * u;
        text(c, label, tx, cy + 9 * u, 25 * u, Palette.pillText(), false, Paint.Align.LEFT);
        return tx + measure(label, 25 * u) + 34 * u;
    }

    private float menuRowH() { return 74 * u(); }

    private void menuCard(RectF out) {
        float u = u(), mw = 860 * u;
        int rows = Math.min(MENU_ROWS, st.menu.labels.size());
        float mh = 110 * u + rows * menuRowH() + 24 * u + (st.menu.hints.isEmpty() ? 0 : 58 * u);
        out.set((getWidth() - mw) / 2, (getHeight() - mh) / 2, (getWidth() + mw) / 2, (getHeight() + mh) / 2);
    }

    private void drawMenu(Canvas c) {
        if (st.menu.isPage()) { drawPage(c); return; }
        float u = u();
        Menu m = st.menu;
        // a menu opened from the settings page stays on top of it
        Menu base = m.parent;
        while (base != null && !base.isPage()) base = base.page != null ? base.page : base.parent;
        if (base != null) {
            st.menu = base;
            drawPage(c);
            st.menu = m;
        }
        p.setColor(0x88000000);
        c.drawRect(0, 0, getWidth(), getHeight(), p);
        RectF cardR = new RectF();
        menuCard(cardR);
        card(c, cardR, 28 * u, Palette.card());
        // the title first; the grey note gets what is left of the line (none when the title fills it)
        String title = ellipsize(m.title, cardR.width() - 72 * u, 32 * u);
        text(c, title, cardR.left + 36 * u, cardR.top + 56 * u, 32 * u, Palette.text(), true, Paint.Align.LEFT);
        float room = Math.min(cardR.width() * 0.45f, cardR.width() - 72 * u - measure(title, 32 * u) - 28 * u);
        if (m.subtitle != null && room > 90 * u)
            text(c, ellipsize(m.subtitle, room, 23 * u), cardR.right - 36 * u, cardR.top + 56 * u, 23 * u, Palette.text3(), false, Paint.Align.RIGHT);
        int rows = Math.min(MENU_ROWS, m.labels.size());
        for (int k = 0; k < rows; k++) {
            int i = m.top + k;
            float y = cardR.top + 94 * u + k * menuRowH();
            r2.set(cardR.left + 20 * u, y, cardR.right - 20 * u, y + menuRowH() - 8 * u);
            boolean on = i == m.sel;
            if (on) {
                p.setColor(Palette.accent());
                c.drawRoundRect(r2, 16 * u, 16 * u, p);
            }
            int fg = on ? 0xFFFFFFFF : Palette.text();
            float tx = r2.left + 28 * u;
            Integer sw = m.swatches.get(i);
            Bitmap img = m.images.get(i);
            if (sw != null) {
                p.setColor(sw);
                c.drawCircle(tx + 18 * u, r2.centerY(), 18 * u, p);
                // white ring, so the dot stays visible on a highlight of the same colour
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(3 * u);
                p.setColor(0xFFFFFFFF);
                c.drawCircle(tx + 18 * u, r2.centerY(), 18 * u, p);
                p.setStyle(Paint.Style.FILL);
                tx += 52 * u;
            }
            if (img != null) {
                float ih = menuRowH() - 22 * u, iw = ih * img.getWidth() / img.getHeight();
                RectF ir = new RectF(tx, r2.centerY() - ih / 2, tx + iw, r2.centerY() + ih / 2);
                src.set(0, 0, img.getWidth(), img.getHeight());
                c.drawBitmap(img, src, ir, p);
                tx = ir.right + 16 * u;
            }
            text(c, ellipsize(m.labels.get(i), r2.right - tx - 80 * u, 27 * u), tx, r2.centerY() + 10 * u, 27 * u, fg, on, Paint.Align.LEFT);
            if (m.marks.get(i)) text(c, "✓", r2.right - 28 * u, r2.centerY() + 11 * u, 30 * u, on ? 0xFFFFFFFF : Palette.accent(), true, Paint.Align.RIGHT);
        }
        if (m.top > 0) text(c, "▲", cardR.centerX(), cardR.top + 92 * u, 18 * u, 0xFF8090A0, false, Paint.Align.CENTER);
        float listEnd = cardR.top + 94 * u + rows * menuRowH();
        if (m.top + rows < m.labels.size()) text(c, "▼", cardR.centerX(), listEnd + 10 * u, 18 * u, 0xFF8090A0, false, Paint.Align.CENTER);
        if (!m.hints.isEmpty()) {
            String h = m.hints.get(m.sel);
            if (h != null) {
                p.setColor(Palette.text3());
                c.drawRect(cardR.left + 36 * u, listEnd + 18 * u, cardR.right - 36 * u, listEnd + 19.5f * u, p);
                text(c, ellipsize(h, cardR.width() - 72 * u, 24 * u), cardR.left + 36 * u, listEnd + 52 * u, 24 * u, Palette.text3(), false, Paint.Align.LEFT);
            }
        }
    }

    private void text(Canvas c, String s, float x, float y, float size, int color, boolean bold, Paint.Align a) {
        tp.setTextSize(size * Palette.textScale);
        tp.setColor(color);
        tp.setTypeface(Theme.font(bold));
        tp.setTextAlign(a);
        c.drawText(s, x, y, tp);
    }

    private float measure(String s, float size) {
        tp.setTextSize(size * Palette.textScale);
        tp.setTypeface(Theme.font(true));
        return tp.measureText(s);
    }

    private String ellipsize(String s, float maxW, float size) {
        tp.setTextSize(size * Palette.textScale);
        tp.setTypeface(Theme.font(true));
        if (tp.measureText(s) <= maxW) return s;
        while (s.length() > 1 && tp.measureText(s + "…") > maxW) s = s.substring(0, s.length() - 1);
        return s + "…";
    }

    private static int mul(int c, float f) {
        return 0xFF000000 | (int) (((c >> 16) & 255) * f) << 16 | (int) (((c >> 8) & 255) * f) << 8 | (int) ((c & 255) * f);
    }

    private static int lighten(int c) {
        int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
        return 0xFF000000 | (r + (255 - r) / 4) << 16 | (g + (255 - g) / 4) << 8 | (b + (255 - b) / 4);
    }

    // ---- touch ----

    @Override public boolean onTouchEvent(MotionEvent e) {
        if (moving() && touchMoving) {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_MOVE: {
                    int i = tileAt(e.getX(), e.getY());
                    if (i >= 0) moveTileTo(i);
                    // near the top or bottom edge the page scrolls along
                    float edge = 60 * u();
                    if (e.getY() < headerH() + edge) { st.scrollTarget = clamp(st.scrollTarget - 12 * u()); invalidate(); }
                    else if (e.getY() > getHeight() - footerH() - edge) { st.scrollTarget = clamp(st.scrollTarget + 12 * u()); invalidate(); }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    touchMoving = false;
                    endMove(true);
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    touchMoving = false;
                    endMove(false);
                    return true;
                default:
                    return true;
            }
        }
        return gd.onTouchEvent(e) || super.onTouchEvent(e);
    }

    private boolean touchMoving;

    /** Index of the tile under a point of the grid, or -1. */
    private int tileAt(float x, float y) {
        if (y < headerH() || y > getHeight() - footerH()) return -1;
        for (int i = 0; i < st.items.size(); i++) {
            tileRect(i, r);
            if (r.contains(x, y)) return i;
        }
        return -1;
    }

    private void longPress(float x, float y) {
        if (st.menu != null || st.lib == null || (st.current != null && !canReorder()) || moving()) return;
        int i = tileAt(x, y);
        if (i < 0) return;
        st.sel = i;
        fireFocus();
        startMove();
        touchMoving = true;
        performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
    }

    private void tap(float x, float y) {
        if (st.menu != null && st.menu.isPage()) {
            Menu m = st.menu;
            float u = u(), rowH = menuRowH();
            if (y > getHeight() - footerH()) { menuBack(); return; }
            if (x < pageSplit()) {
                int i = (int) ((y - 126 * u) / rowH);
                if (y >= 126 * u && i < m.tabs.size()) { setTab(m, i); m.inRows = false; invalidate(); }
                return;
            }
            RectF a = new RectF();
            pageRowsArea(a);
            Menu t = m.tabs.get(m.tab);
            int k = (int) ((y - a.top) / rowH), i = t.top + k;
            if (y >= a.top && k < pageRows() && i < t.labels.size()) { m.inRows = true; t.sel = i; menuPick(); }
            return;
        }
        if (st.menu != null) {
            RectF cardR = new RectF();
            menuCard(cardR);
            if (!cardR.contains(x, y)) { menuBack(); return; }
            int k = (int) ((y - cardR.top - 94 * u()) / menuRowH());
            int i = st.menu.top + k;
            if (k >= 0 && k < MENU_ROWS && i < st.menu.labels.size()) { st.menu.sel = i; menuPick(); }
            return;
        }
        if (st.lib == null) return;
        if (hitHome.contains(x, y)) { goHome(); return; }
        if (hitL.contains(x, y)) { cycle(-1); return; }
        if (hitR.contains(x, y)) { cycle(1); return; }
        if (hitZoom.contains(x, y)) { zoom(); return; }
        if (hitSort.contains(x, y)) { sort(); return; }
        if (hitSearch.contains(x, y)) { listener.onSearch(); return; }
        if (y < headerH() || y > getHeight() - footerH()) return;
        for (int i = 0; i < st.items.size(); i++) {
            tileRect(i, r);
            if (r.contains(x, y)) {
                if (i == st.sel) activate(); else setSel(i);
                return;
            }
        }
    }
}
