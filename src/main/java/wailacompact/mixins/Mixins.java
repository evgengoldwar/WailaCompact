package wailacompact.mixins;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

import javax.annotation.Nonnull;

public enum Mixins implements IMixins {

    // Hides the text lines added by WailaHarvestability, they are replaced by the harvest icons
    HIDE_HARVESTABILITY_TEXT(new MixinBuilder()
        .setPhase(Phase.LATE)
        .addRequiredMod(TargetMods.WAILA_HARVESTABILITY)
        .addClientMixins("MixinWailaHandler_HideText"));

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
