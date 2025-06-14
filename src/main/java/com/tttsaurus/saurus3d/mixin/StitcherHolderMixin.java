package com.tttsaurus.saurus3d.mixin;

import com.tttsaurus.saurus3d.config.Saurus3DMCPatchesConfig;
import com.tttsaurus.saurus3d.mcpatches.api.extra.IStitcherHolderExtra;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Stitcher.Holder.class)
public class StitcherHolderMixin implements IStitcherHolderExtra
{
    @Shadow
    @Final
    private TextureAtlasSprite sprite;

    @Override
    public TextureAtlasSprite getSprite()
    {
        return sprite;
    }

    @Inject(method = "compareTo(Lnet/minecraft/client/renderer/texture/Stitcher$Holder;)I", at = @At("HEAD"), cancellable = true)
    public void compareTo(Stitcher.Holder p_compareTo_1_, CallbackInfoReturnable<Integer> cir)
    {
        if (Saurus3DMCPatchesConfig.ENABLE_TEXTUREMAP_BATCH_TEX_UPLOAD)
        {
            boolean thisAnimated = ((IStitcherHolderExtra)this).getSprite().hasAnimationMetadata();
            boolean otherAnimated = ((IStitcherHolderExtra)p_compareTo_1_).getSprite().hasAnimationMetadata();

            if (thisAnimated != otherAnimated)
                cir.setReturnValue(thisAnimated ? -1 : 1);
        }
    }
}
