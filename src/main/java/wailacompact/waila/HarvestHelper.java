package wailacompact.waila;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.world.World;

public class HarvestHelper {

    private static final String[] TEST_TOOL_CLASSES = { "pickaxe", "shovel", "axe" };
    private static final ItemStack[] TEST_TOOLS = { new ItemStack(Items.wooden_pickaxe),
        new ItemStack(Items.wooden_shovel), new ItemStack(Items.wooden_axe) };

    /**
     * @return the tool class needed to harvest the block (e.g. "pickaxe", or "wrench" / "cutter" for GregTech blocks),
     *         or null if the block has no effective tool or can't be broken at all
     */
    public static String getEffectiveTool(World world, int x, int y, int z, Block block, int meta) {
        if (block.getBlockHardness(world, x, y, z) < 0) return null;

        String tool = safeGetHarvestTool(block, meta);
        if (tool != null) return tool;

        // Blocks without an explicit harvest tool, guess it from the dig speed of vanilla tools
        for (int i = 0; i < TEST_TOOLS.length; i++) {
            ItemStack testTool = TEST_TOOLS[i];
            if (testTool.func_150997_a(block) // getDigSpeed
                >= ((ItemTool) testTool.getItem()).func_150913_i() // getToolMaterial
                    .getEfficiencyOnProperMaterial()) {
                return TEST_TOOL_CLASSES[i];
            }
        }
        return null;
    }

    public static int getHarvestLevel(Block block, int meta) {
        try {
            return Math.max(0, block.getHarvestLevel(meta));
        } catch (RuntimeException e) {
            return Math.max(0, block.getHarvestLevel(0));
        }
    }

    /**
     * Whether the item held by the player is the right tool, with a high enough harvest level, to harvest the block.
     */
    public static boolean isHoldingHarvestTool(EntityPlayer player, Block block, int meta, String tool) {
        ItemStack held = player.getHeldItem();
        if (held == null || held.getItem() == null) return false;
        Item item = held.getItem();

        if (isGTTool(held)) {
            // GregTech tools report their harvest level for every tool class, but only have a dig speed on the blocks
            // they can actually mine (this also checks the harvest level and uses the real block metadata)
            return item.getDigSpeed(held, block, meta) > 0.0F;
        }

        return item.getHarvestLevel(held, tool) >= getHarvestLevel(block, meta);
    }

    public static boolean isGTTool(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound()
            .hasKey("GT.ToolStats");
    }

    private static String safeGetHarvestTool(Block block, int meta) {
        try {
            return block.getHarvestTool(meta);
        } catch (RuntimeException e) {
            // Vanilla blocks only store 16 metadata values, GregTech machines throw on unknown metadata
            return block.getHarvestTool(0);
        }
    }
}
