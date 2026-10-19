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
import wailacompact.waila.IdleProgressFilter;

@Mixin(value = MetaDataProvider.class, remap = false)
public class MixinMetaDataProvider_HideIdleProgress {

    @Inject(method = "handleBlockTextData", at = @At("RETURN"))
    private void wailacompact$hideIdleProgress(ItemStack itemStack, World world, EntityPlayer player,
        MovingObjectPosition mop, DataAccessorCommon accessor, List<String> currenttip, Layout layout,
        CallbackInfoReturnable<List<String>> cir) {
        List<String> lines = cir.getReturnValue();
        if (lines != null && ConfigHandler.instance()
            .getConfig(IdleProgressFilter.CONFIG_KEY, true)) {
            IdleProgressFilter.filter(lines);
        }
    }
}
