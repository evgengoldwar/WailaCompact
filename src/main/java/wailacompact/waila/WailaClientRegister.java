package wailacompact.waila;

import net.minecraft.block.Block;

import mcp.mobius.waila.api.IWailaRegistrar;

public class WailaClientRegister {

    public static void register(IWailaRegistrar registrar) {
        addConfig(registrar, HarvestIconsProvider.CONFIG_KEY);
        addConfig(registrar, IdleProgressFilter.CONFIG_KEY);
        addConfig(registrar, GTRecipeLine.CONFIG_KEY);

        registrar.registerHeadProvider(new HarvestIconsProvider(), Block.class);
        registrar.registerTooltipRenderer(HarvestIconsRenderer.NAME, new HarvestIconsRenderer());
        registrar.registerTooltipRenderer(RecipeStackRenderer.NAME, new RecipeStackRenderer());
    }

    private static void addConfig(IWailaRegistrar registrar, String key) {
        registrar.addConfig("WailaCompact", key, "option." + key, true);
    }
}
