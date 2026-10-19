package wailacompact.mixins.late;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mcp.mobius.waila.api.impl.ConfigHandler;
import mcp.mobius.waila.api.impl.DataAccessorCommon;
import mcp.mobius.waila.api.impl.MetaDataProvider;
import mcp.mobius.waila.cbcore.Layout;
import wailacompact.waila.GTRecipeLine;
import wailacompact.waila.IdleProgressFilter;

/**
 * Post-processes the block tooltip lines once every Waila provider added its own.
 */
@Mixin(value = MetaDataProvider.class, remap = false)
public class MixinMetaDataProvider_Body {

    @Inject(method = "handleBlockTextData", at = @At("RETURN"))
    private void wailacompact$postProcess(ItemStack itemStack, World world, EntityPlayer player,
        MovingObjectPosition mop, DataAccessorCommon accessor, List<String> currenttip, Layout layout,
        CallbackInfoReturnable<List<String>> cir) {
        List<String> lines = cir.getReturnValue();
        if (lines == null) return;
        ConfigHandler config = ConfigHandler.instance();

        if (config.getConfig(IdleProgressFilter.CONFIG_KEY, true)) {
            IdleProgressFilter.filter(lines);
        }
        if (layout == Layout.BODY && config.getConfig(GTRecipeLine.CONFIG_KEY, true)) {
            GTRecipeLine.compact(lines, accessor.getNBTData());
        }
    }
}
