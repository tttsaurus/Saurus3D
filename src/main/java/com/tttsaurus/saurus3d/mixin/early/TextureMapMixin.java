package com.tttsaurus.saurus3d.mixin.early;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.mcpatches.ITextureAtlasSpriteExtra;
import com.tttsaurus.saurus3d.common.core.mcpatches.Rect;
import com.tttsaurus.saurus3d.common.core.mcpatches.RectMergeAlgorithm;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fml.common.ProgressManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.List;

@Mixin(TextureMap.class)
public class TextureMapMixin
{
    @Shadow
    @Final
    protected List<TextureAtlasSprite> listAnimatedSprites;

    @Inject(method = "finishLoading", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;onTextureStitchedPost(Lnet/minecraft/client/renderer/texture/TextureMap;)V", shift = At.Shift.AFTER), remap = false)
    private void afterFinishLoading(Stitcher stitcher, ProgressManager.ProgressBar bar, int j, int k, CallbackInfo ci)
    {
        List<Rect> rects = new ArrayList<>();
        for (TextureAtlasSprite sprite: listAnimatedSprites)
            rects.add(((ITextureAtlasSpriteExtra)sprite).getRect());

        List<Rect> result = RectMergeAlgorithm.mergeRects(rects);
        for (Rect r: result)
        {
            Saurus3D.LOGGER.info("x: " + r.x + ", y: " + r.y + ", width: " + r.width + ", height: " + r.height);
        }
    }
}
