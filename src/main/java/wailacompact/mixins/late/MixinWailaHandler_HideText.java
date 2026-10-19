package wailacompact.mixins.late;

import java.util.List;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import wailacompact.waila.HarvestIconsProvider;

@Mixin(targets = "squeek.wailaharvestability.WailaHandler", remap = false)
public class MixinWailaHandler_HideText {

    @Inject(method = "getWailaBody", at = @At("HEAD"), cancellable = true)
    private void wailacompact$hideText(ItemStack itemStack, List<String> toolTip, IWailaDataAccessor accessor,
        IWailaConfigHandler config, CallbackInfoReturnable<List<String>> cir) {
        if (config.getConfig(HarvestIconsProvider.CONFIG_KEY, true)) {
            cir.setReturnValue(toolTip);
        }
    }
}
