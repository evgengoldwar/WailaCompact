package wailacompact.waila;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.EnumChatFormatting;

import mcp.mobius.waila.api.SpecialChars;
import mcp.mobius.waila.overlay.DisplayUtil;

/**
 * Removes the progress bars of machines that aren't working, e.g. GregTech's "0t / 0t" bar on idle (steam) machines and
 * multiblocks.
 */
public class IdleProgressFilter {

    public static final String CONFIG_KEY = "wailacompact.hideidleprogress";

    // "0t / 0t", "0 / 0", "0.00s / 0.00s" ...
    private static final Pattern ZERO_PROGRESS = Pattern
        .compile("^\\s*0+(?:[.,]0+)?\\s*\\p{L}*\\s*/\\s*0+(?:[.,]0+)?\\s*\\p{L}*\\s*$");

    public static void filter(List<String> lines) {
        lines.removeIf(IdleProgressFilter::isIdleProgressBar);
    }

    private static boolean isIdleProgressBar(String line) {
        if (line == null) return false;
        Matcher render = SpecialChars.patternRender.matcher(line);
        if (!render.find()) return false;
        // Only lines that are just the bar
        if (!DisplayUtil.stripSymbols(line)
            .trim()
            .isEmpty()) return false;

        String name = render.group("name");
        String[] args = render.group("args")
            .split(SpecialChars.WailaRendererComma);

        if ("waila.bar".equals(name)) {
            // GregTech passes progress / maxProgress as the ratio, NaN when both are 0
            if (args.length > 3 && "NaN".equals(args[3].trim())) return true;
            String text = EnumChatFormatting.getTextWithoutFormattingCodes(args[0]);
            return text != null && ZERO_PROGRESS.matcher(text)
                .matches();
        }
        if ("waila.gt.progress".equals(name)) {
            // Params: progress, maxProgress, isAllowedToWork
            return args.length > 1 && "0".equals(args[1].trim());
        }
        return false;
    }
}
