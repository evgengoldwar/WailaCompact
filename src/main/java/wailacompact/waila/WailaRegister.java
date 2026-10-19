package wailacompact.waila;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import mcp.mobius.waila.api.IWailaRegistrar;
import wailacompact.WailaCompact;

public class WailaRegister {

    // Called by Waila through IMC on both sides, see CommonProxy
    public static void register(IWailaRegistrar registrar) {
        if (Loader.isModLoaded("gregtech")) {
            try {
                Class<?> gtTileEntity = Class.forName("gregtech.api.metatileentity.BaseMetaTileEntity");
                registrar.registerNBTProvider(new GTRecipeNBTProvider(), gtTileEntity);
            } catch (ClassNotFoundException e) {
                WailaCompact.LOG.warn("Could not find the GregTech tile entity class", e);
            }
        }

        if (FMLCommonHandler.instance()
            .getSide()
            .isClient()) {
            WailaClientRegister.register(registrar);
        }
    }
}
