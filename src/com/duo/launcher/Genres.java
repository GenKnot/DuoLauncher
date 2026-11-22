package com.duo.launcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * Game genres: a fixed short list, so that every platform's games fall into the same home folders.
 * Kept in the launcher's own folder as DuoLauncher/<platform>/genres.txt, one "file<TAB>genre" per line,
 * written by the AI service or chosen by hand; a "genre:" line in Pegasus metadata is used when there is none.
 */
final class Genres {
    private Genres() { }

    static final String[] IDS = {"rpg", "action", "adventure", "platformer", "tactics", "strategy", "shooter",
            "puzzle", "racing", "sports", "fighting", "simulation", "party", "other"};
    static final int[] COLORS = {0xFF6C5CE7, 0xFFE8463E, 0xFF2E9E5B, 0xFFF2A93B, 0xFF3A4FA3, 0xFF1FA99B, 0xFF37474F,
            0xFFD6458F, 0xFFE07A3F, 0xFF2F8BFF, 0xFFB23A48, 0xFF6D8299, 0xFFE5A92E, 0xFF8A8F98};

    /** Names in the interface languages, same order as {@link I18n#CODES}. */
    private static final String[][] NAMES = {
            {"角色扮演", "角色扮演", "RPG", "RPG", "RPG", "RPG", "RPG", "Rollenspiel", "GDR", "RPG", "RPG"},
            {"动作", "動作", "Action", "アクション", "액션", "Action", "Acción", "Action", "Azione", "Ação", "Экшен"},
            {"冒险", "冒險", "Adventure", "アドベンチャー", "어드벤처", "Aventure", "Aventura", "Adventure", "Avventura", "Aventura", "Приключения"},
            {"平台跳跃", "平台跳躍", "Platformer", "プラットフォーム", "플랫포머", "Plateforme", "Plataformas", "Jump 'n' Run", "Piattaforme", "Plataforma", "Платформеры"},
            {"战棋", "戰棋", "Tactics", "シミュレーションRPG", "전략 RPG", "Tactique", "Táctica", "Taktik", "Tattico", "Tática", "Тактика"},
            {"策略", "策略", "Strategy", "ストラテジー", "전략", "Stratégie", "Estrategia", "Strategie", "Strategia", "Estratégia", "Стратегии"},
            {"射击", "射擊", "Shooter", "シューティング", "슈팅", "Tir", "Disparos", "Shooter", "Sparatutto", "Tiro", "Шутеры"},
            {"益智", "益智", "Puzzle", "パズル", "퍼즐", "Réflexion", "Puzles", "Puzzle", "Rompicapo", "Quebra-cabeça", "Головоломки"},
            {"赛车", "賽車", "Racing", "レース", "레이싱", "Course", "Carreras", "Rennspiel", "Corse", "Corrida", "Гонки"},
            {"体育", "體育", "Sports", "スポーツ", "스포츠", "Sport", "Deportes", "Sport", "Sport", "Esportes", "Спорт"},
            {"格斗", "格鬥", "Fighting", "格闘", "격투", "Combat", "Lucha", "Kampfspiel", "Picchiaduro", "Luta", "Файтинги"},
            {"模拟经营", "模擬經營", "Simulation", "シミュレーション", "시뮬레이션", "Simulation", "Simulación", "Simulation", "Simulazione", "Simulação", "Симуляторы"},
            {"派对音乐", "派對音樂", "Party & music", "パーティー・音楽", "파티·음악", "Party et musique", "Fiesta y música", "Party & Musik", "Party e musica", "Festa e música", "Вечеринки и музыка"},
            {"其他", "其他", "Other", "その他", "기타", "Autres", "Otros", "Sonstige", "Altro", "Outros", "Другое"},
    };

    /** "Not sorted yet", for games no genre is known for. */
    private static final String[] UNSORTED = {"未分类", "未分類", "Not sorted", "未分類", "미분류", "Non classés", "Sin clasificar",
            "Nicht eingeordnet", "Non classificati", "Sem categoria", "Без жанра"};

    static int index(String id) {
        for (int i = 0; i < IDS.length; i++) if (IDS[i].equals(id)) return i;
        return -1;
    }

    /** What the AI icon of a genre folder shows (English, for the image prompt). */
    static String subject(String id) {
        switch (id) {
            case "rpg": return "role-playing games: a fantasy sword crossed with a magic staff and a glowing potion bottle";
            case "action": return "action games: a clenched fist bursting through a star-shaped explosion";
            case "adventure": return "adventure games: an old treasure map with a compass and a lantern";
            case "platformer": return "platform jumping games: floating grassy platforms with a spring and a gold coin";
            case "tactics": return "tactical strategy games: chess-like figures standing on a square grid board with a small flag";
            case "strategy": return "strategy games: a castle tower with a war banner and a rolled map";
            case "shooter": return "shooting games: a retro rocket spaceship firing laser beams";
            case "puzzle": return "puzzle games: colourful interlocking jigsaw pieces and falling blocks";
            case "racing": return "racing games: a small race car with a chequered flag";
            case "sports": return "sports games: a tennis racket, a baseball and a small trophy";
            case "fighting": return "fighting games: a pair of red boxing gloves";
            case "simulation": return "simulation and life games: a cosy little farmhouse with a watering can";
            case "party": return "party and music games: a microphone with music notes and confetti";
            case "other": return "assorted video games: a game controller surrounded by sparkles";
            default: return "unsorted games: an open cardboard box full of game cartridges";
        }
    }

    static String name(String id) {
        int i = index(id);
        return i < 0 ? UNSORTED[I18n.index()] : NAMES[i][I18n.index()];
    }

    static int color(String id) {
        int i = index(id);
        return i < 0 ? 0xFF9AA3AD : COLORS[i];
    }

    /** Our genre for free text such as Pegasus' "genre: Action RPG" or "角色扮演", or null. */
    static String fromText(String s) {
        if (s == null) return null;
        String t = s.toLowerCase(Locale.ROOT).trim();
        if (t.isEmpty()) return null;
        if (index(t) >= 0) return t;
        String[][] words = {
                {"tactics", "tactical", "srpg", "战棋", "戰棋", "simulation rpg"},
                {"rpg", "role", "角色扮演"},
                {"fight", "格斗", "格鬥", "beat'em", "beat 'em"},
                {"racing", "race", "driving", "赛车", "賽車", "竞速"},
                {"sport", "体育", "體育", "运动", "golf", "tennis", "soccer", "football", "basketball", "baseball"},
                {"platform", "平台", "跳跃"},
                {"shoot", "射击", "射擊", "fps", "shmup"},
                {"puzzle", "益智", "解谜", "解謎"},
                {"strategy", "策略", "rts"},
                {"party", "music", "rhythm", "音乐", "音游", "派对", "派對", "mini-game", "minigame"},
                {"simulation", "sim", "模拟", "模擬", "经营", "經營", "life"},
                {"adventure", "冒险", "冒險", "visual novel"},
                {"action", "动作", "動作", "hack and slash"},
        };
        String[] ids = {"tactics", "rpg", "fighting", "racing", "sports", "platformer", "shooter", "puzzle", "strategy", "party",
                "simulation", "adventure", "action"};
        for (int i = 0; i < words.length; i++) for (String w : words[i]) if (t.contains(w)) return ids[i];
        return null;
    }

    /** How a game is named in genres.txt: its file name, or the title ID of an installed 3DS title. */
    static String key(Library.Game g) {
        if (g.titleId != null && g.path != null && g.path.getName().endsWith(".app")) return g.titleId;
        return g.path == null ? null : g.path.getName();
    }

    static Map<String, String> read(File dir) {
        Map<String, String> out = new LinkedHashMap<>();
        File f = new File(dir, "genres.txt");
        if (!f.isFile()) return out;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            for (String line; (line = r.readLine()) != null; ) {
                int tab = line.lastIndexOf('\t');
                if (line.startsWith("#") || tab <= 0) continue;
                String id = line.substring(tab + 1).trim();
                if (index(id) >= 0) out.put(line.substring(0, tab), id);
            }
        } catch (IOException ignored) {
        }
        return out;
    }

    static boolean write(File dir, Map<String, String> all) {
        dir.mkdirs();
        File f = new File(dir, "genres.txt"), tmp = new File(dir, "genres.txt.tmp");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
            w.write("# Duo Launcher: genre of each game (file name, tab, one of " + String.join(" ", IDS) + ")\n");
            for (Map.Entry<String, String> e : all.entrySet()) w.write(e.getKey() + "\t" + e.getValue() + "\n");
        } catch (IOException e) {
            tmp.delete();
            return false;
        }
        if (tmp.renameTo(f)) return true;
        f.delete();
        return tmp.renameTo(f);
    }

    /** Sets (or with id null, clears) one game's genre. */
    static boolean set(Library.Game g, String id) {
        String k = key(g);
        if (k == null) return false;
        File dir = GameText.ownDir(g.platform, g);
        Map<String, String> all = read(dir);
        if (id == null) all.remove(k); else all.put(k, id);
        if (!write(dir, all)) return false;
        g.genre = id;
        return true;
    }

    /** Games of the platform without a genre. */
    static List<Library.Game> todo(Library.Platform p) {
        List<Library.Game> out = new ArrayList<>();
        for (Library.Game g : p.games) if (g.pkg == null && g.path != null && g.genre == null) out.add(g);
        return out;
    }

    /**
     * Asks the AI service for the genre of each game, 30 at a time, and writes them to genres.txt.
     * Returns how many were sorted; stops at the first failed request (ai.error) or when {@code stop} is set.
     */
    static int classify(Ai ai, Library.Platform p, List<Library.Game> games, IntConsumer progress, java.util.concurrent.atomic.AtomicBoolean stop) {
        String system = "You sort video games into genres for a game launcher. For each game you get its platform, ROM file name and "
                + "title (possibly Chinese, possibly a fan translation or hack). Identify the game and pick the one genre that fits it best "
                + "from this list: rpg (role-playing, including action RPGs like Zelda-likes only when levelling/stats matter), action "
                + "(action, beat 'em up, hack and slash, action-adventure such as Zelda), adventure (point and click, visual novel, "
                + "detective, story-driven), platformer (2D/3D platform games such as Mario, Kirby, Sonic), tactics (tactical/strategy "
                + "RPGs on a grid such as Fire Emblem, Super Robot Wars, Final Fantasy Tactics), strategy (real-time or turn-based "
                + "strategy, card battles), shooter, puzzle, racing, sports, fighting, simulation (life, farming, business, pets, "
                + "train/flight sims), party (party games, mini-games, music and rhythm), other. Answer only with a JSON array of "
                + "objects {\"i\": number, \"genre\": string}, one for each input.";
        File dir = games.isEmpty() ? null : GameText.ownDir(p, games.get(0));
        Map<String, String> all = dir == null ? new LinkedHashMap<>() : read(dir);
        int done = 0;
        for (int start = 0; start < games.size(); start += 30) {
            if (stop != null && stop.get()) break;
            List<Library.Game> part = games.subList(start, Math.min(games.size(), start + 30));
            JSONArray in = new JSONArray();
            try {
                for (int i = 0; i < part.size(); i++) {
                    Library.Game g = part.get(i);
                    JSONObject o = new JSONObject().put("i", i).put("platform", Library.platformTitle(p.system.isEmpty() ? p.name : p.system))
                            .put("file", g.path.getName()).put("title", g.title);
                    if (g.original != null && !g.original.isEmpty()) o.put("english title", g.original);
                    in.put(o);
                }
            } catch (Exception e) {
                break;
            }
            String answer = ai.chat(system, in.toString(), 200 + part.size() * 30);
            if (answer == null) break;
            int a = answer.indexOf('['), b = answer.lastIndexOf(']');
            if (a < 0 || b <= a) { ai.error = "unexpected answer"; break; }
            try {
                JSONArray out = new JSONArray(answer.substring(a, b + 1));
                for (int j = 0; j < out.length(); j++) {
                    JSONObject o = out.getJSONObject(j);
                    int i = o.optInt("i", -1);
                    String id = o.optString("genre", "").trim().toLowerCase(Locale.ROOT);
                    if (i < 0 || i >= part.size()) continue;
                    if (index(id) < 0) id = fromText(id);
                    if (id == null) id = "other";
                    Library.Game g = part.get(i);
                    String k = key(g);
                    if (k == null) continue;
                    all.put(k, id);
                    g.genre = id;
                    done++;
                }
            } catch (Exception e) {
                ai.error = "unexpected answer";
                break;
            }
            all.putAll(read(dir));
            for (Library.Game g : part) { String k = key(g); if (k != null && all.containsKey(k)) g.genre = all.get(k); }
            if (!write(dir, all)) { ai.error = "could not write " + dir; break; }
            progress.accept(Math.min(games.size(), start + part.size()));
        }
        return done;
    }
}
