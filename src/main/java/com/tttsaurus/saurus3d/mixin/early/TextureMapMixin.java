package com.tttsaurus.saurus3d.mixin.early;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tttsaurus.saurus3d.mcpatches.api.ITextureAtlasSpriteExtra;
import com.tttsaurus.saurus3d.mcpatches.impl.texturemap.TexRect;
import com.tttsaurus.saurus3d.mcpatches.impl.texturemap.RectMergeAlgorithm;
import com.tttsaurus.saurus3d.mcpatches.impl.texturemap.TexUpdatePlan;
import com.tttsaurus.saurus3d.mcpatches.impl.texturemap.TextureUploader;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fml.common.ProgressManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(TextureMap.class)
public class TextureMapMixin
{
    @Unique
    private Map<TexRect, List<TextureAtlasSprite>> saurus3D$mergedAnimatedSprites;

    @Shadow
    @Final
    protected List<TextureAtlasSprite> listAnimatedSprites;

    @Inject(method = "finishLoading", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;onTextureStitchedPost(Lnet/minecraft/client/renderer/texture/TextureMap;)V", shift = At.Shift.AFTER), remap = false)
    private void afterFinishLoading(Stitcher stitcher, ProgressManager.ProgressBar bar, int j, int k, CallbackInfo ci)
    {
        List<TexRect> rects = new ArrayList<>();
        for (TextureAtlasSprite sprite: listAnimatedSprites)
            rects.add(((ITextureAtlasSpriteExtra)sprite).getRect());

        List<TexRect> result = RectMergeAlgorithm.mergeRects(rects);
        if (saurus3D$mergedAnimatedSprites == null)
        {
            saurus3D$mergedAnimatedSprites = new HashMap<>();
            for (TexRect r: result)
            {
                List<TextureAtlasSprite> sprites = new ArrayList<>();
                saurus3D$mergedAnimatedSprites.put(r, sprites);
                for (TextureAtlasSprite sprite: listAnimatedSprites)
                    if (r.contains(((ITextureAtlasSpriteExtra)sprite).getRect()))
                        sprites.add(sprite);
            }
        }
    }

    @WrapMethod(method = "updateAnimations")
    public void updateAnimations(Operation<Void> original)
    {
        if (saurus3D$mergedAnimatedSprites == null)
        {
            original.call();
            return;
        }

        GlStateManager.bindTexture(((TextureMap)(Object)this).getGlTextureId());

        for (TextureAtlasSprite sprite: this.listAnimatedSprites)
            ((ITextureAtlasSpriteExtra)sprite).setUpdated(false);

        for (Map.Entry<TexRect, List<TextureAtlasSprite>> entry: saurus3D$mergedAnimatedSprites.entrySet())
        {
            TexRect rect = entry.getKey();
            List<TextureAtlasSprite> sprites = entry.getValue();

            TextureUploader.reset();
            for (TextureAtlasSprite sprite: sprites)
            {
                ITextureAtlasSpriteExtra spriteExtra = ((ITextureAtlasSpriteExtra)sprite);

                TexUpdatePlan plan = spriteExtra.updateAnimation_V2();
                for (int i = 0; i < plan.data.length; i++)
                    if (!((plan.rect.width >> i <= 0) || (plan.rect.height >> i <= 0)))
                        TextureUploader.planTexUpload(i, plan.data[i], plan.rect);

                spriteExtra.setUpdated(true);
            }
            TextureUploader.batchUpload(rect);
        }

        for (TextureAtlasSprite sprite: this.listAnimatedSprites)
            if (!((ITextureAtlasSpriteExtra)sprite).isUpdated())
                sprite.updateAnimation();
    }
}
