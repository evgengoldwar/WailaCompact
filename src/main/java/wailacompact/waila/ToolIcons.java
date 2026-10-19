package wailacompact.waila;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Loader;
import wailacompact.WailaCompact;

/**
 * Item stacks used as icons for the tool classes.
 */
public class ToolIcons {

    private static final Map<String, ItemStack> VANILLA = new HashMap<>();
    // Tool class -> GregTech MetaGeneratedTool01 tool id (see gregtech.common.items.IDMetaTool01)
    private static final Map<String, Integer> GREGTECH = new HashMap<>();
    private static final Map<String, ItemStack> CACHE = new HashMap<>();

    static {
        VANILLA.put("pickaxe", new ItemStack(Items.iron_pickaxe));
        VANILLA.put("axe", new ItemStack(Items.iron_axe));
        VANILLA.put("shovel", new ItemStack(Items.iron_shovel));
        VANILLA.put("hoe", new ItemStack(Items.iron_hoe));
        VANILLA.put("sword", new ItemStack(Items.iron_sword));
        VANILLA.put("shears", new ItemStack(Items.shears));

        GREGTECH.put("saw", 10);
        GREGTECH.put("hammer", 12);
        GREGTECH.put("softhammer", 14);
        GREGTECH.put("softmallet", 14);
        GREGTECH.put("wrench", 16);
        GREGTECH.put("file", 18);
        GREGTECH.put("crowbar", 20);
        GREGTECH.put("screwdriver", 22);
        GREGTECH.put("cutter", 26);
        GREGTECH.put("scoop", 28);
        GREGTECH.put("grafter", 30);
        GREGTECH.put("knife", 34);
        GREGTECH.put("plunger", 44);
    }

    /**
     * @return the icon of the tool class, or null if there is none
     */
    public static ItemStack get(String toolClass) {
        if (CACHE.containsKey(toolClass)) return CACHE.get(toolClass);

        ItemStack stack = null;
        Integer gtToolId = GREGTECH.get(toolClass);
        if (gtToolId != null) stack = createGTTool(gtToolId);
        if (stack == null) stack = VANILLA.get(toolClass);

        CACHE.put(toolClass, stack);
        return stack;
    }

    private static ItemStack createGTTool(int toolId) {
        if (!Loader.isModLoaded("gregtech")) return null;
        try {
            // MetaGeneratedTool01.INSTANCE.getToolWithStats(toolId, 1, Materials.Steel, Materials.Steel, null)
            Class<?> toolClass = Class.forName("gregtech.common.items.MetaGeneratedTool01");
            Class<?> materialsClass = Class.forName("gregtech.api.enums.Materials");
            Object instance = toolClass.getField("INSTANCE")
                .get(null);
            Object steel = materialsClass.getField("Steel")
                .get(null);
            Method getToolWithStats = toolClass
                .getMethod("getToolWithStats", int.class, int.class, materialsClass, materialsClass, long[].class);
            return (ItemStack) getToolWithStats.invoke(instance, toolId, 1, steel, steel, null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            WailaCompact.LOG.warn("Could not create the GregTech tool icon with id " + toolId, e);
            return null;
        }
    }
}
