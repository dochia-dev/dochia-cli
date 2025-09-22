package dev.dochia.cli.core.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.Callable;

@Command(
        name = "legend",
        description = "Tells the Dochia legend. Short version.",
        mixinStandardHelpOptions = true,
        hidden = true,
        showDefaultValues = true
)
public class LegendCommand implements Callable<Integer> {

    @Option(names = "--haiku", description = "Force haiku output.")
    boolean haiku;

    @Option(names = "--ascii", description = "Force ASCII mini output.")
    boolean ascii;

    @Option(names = "--seed", description = "Deterministic selection seed.")
    Long seed;

    @Option(names = "--no-color", description = "Disable ANSI colors (or set NO_COLOR env).")
    boolean noColor;

    @Option(names = "--width", description = "Target output width (for boxes).", defaultValue = "60")
    int width;

    @ParentCommand
    Object parent; // optional: access parent config if needed later

    public static final String LINE = "─────────────────────────────";
    private static final String[][] STORY_EN = new String[][]{
            {
                    LINE,
                    "        Dochia’s Legend",
                    LINE,
                    "Baba Dochia climbed the mountain,",
                    "wearing nine coats, one on top of another.",
                    "She thought spring had come,",
                    "so she shed them one by one.",
                    "",
                    "Winter came back. She froze.",
                    "… and turned to stone. 🪨",
                    "",
                    "Moral of the story?",
                    "Never trust the weather forecast,",
                    "and always test your edge cases. 😉",
                    "",
                    LINE,
                    "Tip: Run `dochia --chaos` to shed",
                    "some coats of your own.",
                    LINE
            }
    };

    private static final String[][] HAIKU_EN = new String[][]{
            {"Nine coats on the hill,", "Spring fooled her, winter returned —", "Stone remembers all."}
    };

    private static final String[][] ASCII_EN = new String[][]{
            {
                    "   /\\    Baba Dochia climbed high,",
                    "  /  \\   dropping her nine coats…",
                    " / ❄  \\  …then winter laughed.",
                    "/______\\ Moral: never trust inputs. 😉"
            }
    };

    @Override
    public Integer call() throws Exception {
        final boolean ansi = !noColor && System.getenv("NO_COLOR") == null;


        Variant chosen = chooseVariant();
        String[][] lines = resolveLines(chosen);

        printBoxed(lines, ansi);
        return 0;
    }

    private enum Variant {STORY, HAIKU, ASCII}

    private Variant chooseVariant() {
        if (haiku && ascii) return Variant.HAIKU; // prefer explicit & deterministic
        if (haiku) return Variant.HAIKU;
        if (ascii) return Variant.ASCII;

        // Random choice for the easter egg when no explicit style is set
        Random rnd = (seed != null) ? new Random(seed) : new Random();
        int pick = rnd.nextInt(3);
        if (pick == 0) {
            return Variant.STORY;
        }
        return pick == 1 ? Variant.HAIKU : Variant.ASCII;
    }

    private String[][] resolveLines(Variant v) {
        return switch (v) {
            case STORY -> STORY_EN;
            case HAIKU -> HAIKU_EN;
            case ASCII -> ASCII_EN;
        };
    }

    private void printBoxed(String[][] content, boolean ansi) {
        final String top = repeat("─", Math.clamp(width, 20, 120));
        // Only add colored title bars for STORY; keep HAIKU/ASCII clean
        boolean banner = content[0].length > 8; // crude but effective

        if (banner && ansi) {
            System.out.println("\u001B[2m" + top + "\u001B[0m");
        } else if (banner) {
            System.out.println(top);
        }

        for (String line : content[0]) {
            System.out.println(line);
        }

        if (banner && ansi) {
            System.out.println("\u001B[2m" + top + "\u001B[0m");
        } else if (banner) {
            System.out.println(top);
        }
    }

    private static String repeat(String s, int n) {
        byte[] unit = s.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[unit.length * n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(unit, 0, out, i * unit.length, unit.length);
        }
        return new String(out, StandardCharsets.UTF_8);
    }
}

