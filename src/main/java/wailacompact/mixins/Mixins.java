package wailacompact.mixins;

import javax.annotation.Nonnull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    // Hides the text lines added by WailaHarvestability, they are replaced by the harvest icons
    HIDE_HARVESTABILITY_TEXT(new MixinBuilder().setPhase(Phase.LATE)
        .addRequiredMod(TargetMods.WAILA_HARVESTABILITY)
        .addClientMixins("MixinWailaHandler_HideText")),
    // Hides the "0t / 0t" progress bar of idle machines, compacts the recipe of GregTech machines
    TOOLTIP_POST_PROCESSING(new MixinBuilder().setPhase(Phase.LATE)
        .addRequiredMod(TargetMods.WAILA)
        .addClientMixins("MixinMetaDataProvider_Body"));

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Nonnull
    @Override
    public MixinBuilder getBuilder() {
        return builder;
    }
}
