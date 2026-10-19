package wailacompact.waila;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import mcp.mobius.waila.api.SpecialChars;

/**
 * Replaces the "Input:" / "Output:" item lists of GregTech single block machines by one line of icons:
 * {@code [inputs] → [what is being crafted] | [what is in the output slots]}.
 */
public class GTRecipeLine {

    public static final String CONFIG_KEY = "wailacompact.compactrecipe";

    private static final int MAX_INPUTS = 6;
    private static final int MAX_OUTPUTS = 4;
    private static final int MAX_BUFFER = 3;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int GRAY = 0xAAAAAA;
    // ForgeDirection ordinals: DOWN, UP, NORTH, SOUTH, WEST, EAST
    private static final String[] SIDES = { "↓", "↑", "N", "S", "W", "E" };

    private static final String STACK_LINE_PREFIX = SpecialChars.RENDER + "{waila.stack";

    public static void compact(List<String> lines, NBTTagCompound tag) {
        // Only GregTech single block machines (MTEBasicMachine) send this
        if (tag == null || !tag.hasKey("outputFacingSingleBlock")) return;

        String inputHeader = StatCollector.translateToLocal("GT5U.waila.machine.input");
        String outputPrefix = formatPrefix("GT5U.waila.machine.output");
        String moreItemsPrefix = formatPrefix("GT5U.waila.machine.more_items");

        int insertAt = -1;
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);
            boolean isHeader = line.equals(inputHeader) || (!outputPrefix.isEmpty() && line.startsWith(outputPrefix));
            if (!isHeader) {
                i++;
                continue;
            }
            if (insertAt < 0) insertAt = i;
            lines.remove(i);
            // The item / fluid lines listed under the header
            while (i < lines.size() && isListEntry(lines.get(i), moreItemsPrefix)) lines.remove(i);
        }
        if (insertAt < 0) return;

        lines.add(insertAt, buildLine(tag));
    }

    private static String buildLine(NBTTagCompound tag) {
        List<String> inputs = new ArrayList<>();
        readItems(tag, "inputItems", null, inputs);
        readFluid(tag, "inputFluid", inputs);

        List<String> crafting = new ArrayList<>();
        readItems(tag, GTRecipeNBTProvider.TAG_CRAFTING_ITEM, GTRecipeNBTProvider.TAG_AMOUNT, crafting);
        readFluid(tag, GTRecipeNBTProvider.TAG_CRAFTING_FLUID, crafting);
        if (crafting.isEmpty()) {
            // Our mod isn't on the server, use what GregTech sends (newer versions only)
            readItems(tag, "outputRecipeItems", null, crafting);
            readFluid(tag, "outputRecipeFluid", crafting);
        }

        List<String> buffer = new ArrayList<>();
        readItems(tag, "outputItems", null, buffer);
        readFluid(tag, "outputFluid", buffer);

        StringBuilder line = new StringBuilder();
        append(line, inputs, MAX_INPUTS);
        line.append(RecipeStackRenderer.text("→", TEXT_COLOR));
        int facing = tag.getInteger("outputFacingSingleBlock");
        if (facing >= 0 && facing < SIDES.length) line.append(RecipeStackRenderer.text(SIDES[facing], GRAY));
        append(line, crafting, MAX_OUTPUTS);
        if (!buffer.isEmpty()) {
            line.append(RecipeStackRenderer.text("|", GRAY));
            append(line, buffer, MAX_BUFFER);
        }
        return line.toString();
    }

    private static void append(StringBuilder line, List<String> elements, int max) {
        for (int i = 0; i < elements.size(); i++) {
            if (i == max) {
                line.append(RecipeStackRenderer.text("+" + (elements.size() - i), GRAY));
                return;
            }
            line.append(elements.get(i));
        }
    }

    private static void readItems(NBTTagCompound tag, String prefix, String amountKey, List<String> out) {
        List<ItemStack> stacks = new ArrayList<>();
        List<Long> amounts = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            if (!tag.hasKey(prefix + i)) continue;
            NBTTagCompound itemTag = tag.getCompoundTag(prefix + i);
            ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
            if (stack == null || stack.getItem() == null) continue;
            long amount = amountKey != null && itemTag.hasKey(amountKey) ? itemTag.getInteger(amountKey)
                : stack.stackSize;
            if (amount <= 0) amount = 1;

            // Merge the same item spread over several slots
            int existing = indexOf(stacks, stack);
            if (existing >= 0) {
                amounts.set(existing, amounts.get(existing) + amount);
            } else {
                stacks.add(stack);
                amounts.add(amount);
            }
        }
        for (int i = 0; i < stacks.size(); i++) out.add(RecipeStackRenderer.item(stacks.get(i), amounts.get(i)));
    }

    private static int indexOf(List<ItemStack> stacks, ItemStack stack) {
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack other = stacks.get(i);
            if (other.getItem() == stack.getItem() && other.getItemDamage() == stack.getItemDamage()
                && ItemStack.areItemStackTagsEqual(other, stack)) return i;
        }
        return -1;
    }

    private static void readFluid(NBTTagCompound tag, String key, List<String> out) {
        if (!tag.hasKey(key)) return;
        FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag(key));
        if (fluid == null || fluid.getFluid() == null || fluid.amount <= 0) return;
        out.add(RecipeStackRenderer.fluid(fluid.getFluid(), fluid.amount));
    }

    private static boolean isListEntry(String line, String moreItemsPrefix) {
        return line.startsWith(STACK_LINE_PREFIX) || (!moreItemsPrefix.isEmpty() && line.startsWith(moreItemsPrefix));
    }

    /**
     * The part of a translated format string before its first parameter, e.g. "§bOutput: §e" for "§bOutput: §e%s".
     */
    private static String formatPrefix(String key) {
        String formatted = StatCollector.translateToLocalFormatted(key, "\u0000");
        int index = formatted.indexOf('\u0000');
        return index < 0 ? formatted : formatted.substring(0, index);
    }
}
