package wailacompact.waila;

import net.minecraft.block.Block;

import mcp.mobius.waila.api.IWailaRegistrar;

public class WailaRegister {

    // Called by Waila through IMC, see ClientProxy
    public static void register(IWailaRegistrar registrar) {
        String key = HarvestIconsProvider.CONFIG_KEY;
        registrar.addConfig("WailaCompact", key, "option." + key, true);
        registrar.registerHeadProvider(new HarvestIconsProvider(), Block.class);
        registrar.registerTooltipRenderer(HarvestIconsRenderer.NAME, new HarvestIconsRenderer());
    }
}
