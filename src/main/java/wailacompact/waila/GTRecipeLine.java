package wailacompact.waila;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import mcp.mobius.waila.api.SpecialChars;

/**
 * Replaces the recipe item lists of GregTech machines by one line of icons:
 * <ul>
 * <li>single blocks: {@code [inputs] → [what is being crafted] | [what is in the output slots]}</li>
 * <li>multiblocks: {@code → [what is being crafted]} (multiblocks don't keep the inputs of the running recipe)</li>
 * </ul>
 */
public class GTRecipeLine {

    public static final String CONFIG_KEY = "wailacompact.compactrecipe";

    private static final int MAX_INPUTS = 6;
    private static final int MAX_OUTPUTS = 4;
    private static final int MAX_BUFFER = 3;
    private static final int MAX_MULTI_OUTPUTS = 6;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int GRAY = 0xAAAAAA;
    // ForgeDirection ordinals: DOWN, UP, NORTH, SOUTH, WEST, EAST
    private static final String[] SIDES = { "↓", "↑", "N", "S", "W", "E" };

    private static final String STACK_LINE_PREFIX = SpecialChars.RENDER + "{waila.stack";
    private static final String MULTI_STACK_LINE_PREFIX = "  " + STACK_LINE_PREFIX;
    private static final String BAR_LINE_PREFIX = SpecialChars.RENDER + "{waila.bar";
    private static final String GT_BAR_LINE_PREFIX = SpecialChars.RENDER + "{waila.gt.progress";

    public static void compact(List<String> lines, NBTTagCompound tag) {
        if (tag == null) return;
        // Only GregTech single block machines (MTEBasicMachine) send this
        if (tag.hasKey("outputFacingSingleBlock")) {
            compactSingleBlock(lines, tag);
        } else if (tag.hasKey("incompleteStructure") && tag.hasKey("maxProgress")) {
            // MTEMultiBlockBase and its subclasses (also gt++ steam multiblocks, TecTech...)
            compactMultiBlock(lines, tag);
        }
    }

    private static void compactMultiBlock(List<String> lines, NBTTagCompound tag) {
        String producingHeader = StatCollector.translateToLocal("GT5U.waila.producing");
        String lockedRecipe = StatCollector.translateToLocal("GT5U.waila.multiblock.status.locked_recipe");
        String andMorePrefix = formatPrefix("GT5U.waila.producing.andmore");

        int insertAt = lines.indexOf(producingHeader);
        if (insertAt >= 0) {
            lines.remove(insertAt);
            if (insertAt < lines.size() && lines.get(insertAt)
                .equals(lockedRecipe)) insertAt++;
            while (insertAt < lines.size()) {
                String line = lines.get(insertAt);
                boolean isEntry = line.startsWith(MULTI_STACK_LINE_PREFIX)
                    || (!andMorePrefix.trim()
                        .isEmpty() && line.startsWith(andMorePrefix));
                if (!isEntry) break;
                lines.remove(insertAt);
            }
        }

        if (!tag.getBoolean("isActive")) return;
        List<String> outputs = new ArrayList<>();
        int itemCount = tag.getInteger("outputItemLength");
        for (int i = 0; i < itemCount; i++) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("outputItemStack" + i));
            if (stack == null || stack.getItem() == null) continue;
            outputs.add(RecipeStackRenderer.item(stack, Math.max(1, tag.getInteger("outputItemCount" + i))));
        }
        int fluidCount = tag.getInteger("outputFluidLength");
        for (int i = 0; i < fluidCount; i++) {
            Fluid fluid = FluidRegistry.getFluid(tag.getString("outputFluidName" + i));
            if (fluid == null) continue;
            outputs.add(RecipeStackRenderer.fluid(fluid, tag.getInteger("outputFluidCount" + i)));
        }
        if (outputs.isEmpty()) return;

        StringBuilder line = new StringBuilder(RecipeStackRenderer.text("\u2192", TEXT_COLOR));
        append(line, outputs, MAX_MULTI_OUTPUTS);

        if (insertAt < 0) {
            // Multiblocks that don't list their outputs (e.g. gt++ steam multiblocks): put it after the progress bar
            insertAt = lines.size();
            for (int i = 0; i < lines.size(); i++) {
                String l = lines.get(i);
                if (l.startsWith(BAR_LINE_PREFIX) || l.startsWith(GT_BAR_LINE_PREFIX)) {
                    insertAt = i + 1;
                    break;
                }
            }
        }
        lines.add(insertAt, line.toString());
    }

    private static void compactSingleBlock(List<String> lines, NBTTagCompound tag) {
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
