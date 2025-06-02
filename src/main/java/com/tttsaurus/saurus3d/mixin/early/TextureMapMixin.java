package com.tttsaurus.saurus3d.mixin.early;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLIllegalStateException;
import com.tttsaurus.saurus3d.mcpatches.api.extra.ITextureAtlasSpriteExtra;
import com.tttsaurus.saurus3d.mcpatches.api.extra.ITextureMapExtra;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.ITextureUploader;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexRect;
import com.tttsaurus.saurus3d.mcpatches.impl.texturemap.RectMergeAlgorithm;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexUpdatePlan;
import com.tttsaurus.saurus3d.mcpatches.impl.texturemap.TextureUploaderV1;
import com.tttsaurus.saurus3d.mcpatches.impl.texturemap.TextureUploaderV2;
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
public class TextureMapMixin implements ITextureMapExtra
{
    @Unique
    private static boolean saurus3D$enableBatchTexUpload;

    @Override
    public boolean isEnableBatchTexUpload()
    {
        return saurus3D$enableBatchTexUpload;
    }

    @Override
    public void setEnableBatchTexUpload(boolean flag)
    {
        saurus3D$enableBatchTexUpload = flag;
    }

    @Unique
    private Map<TexRect, List<TextureAtlasSprite>> saurus3D$mergedAnimatedSprites;

    @Unique
    private List<ITextureUploader> saurus3D$textureUploader;

    @Shadow
    @Final
    protected List<TextureAtlasSprite> listAnimatedSprites;

    @Inject(method = "finishLoading", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;onTextureStitchedPost(Lnet/minecraft/client/renderer/texture/TextureMap;)V", shift = At.Shift.AFTER), remap = false)
    private void afterFinishLoading(Stitcher stitcher, ProgressManager.ProgressBar bar, int j, int k, CallbackInfo ci)
    {
        if (saurus3D$enableBatchTexUpload)
        {
            if (saurus3D$mergedAnimatedSprites == null)
            {
                List<TexRect> rects = new ArrayList<>();
                for (TextureAtlasSprite sprite: listAnimatedSprites)
                    rects.add(((ITextureAtlasSpriteExtra)sprite).getRect());

                List<TexRect> result = RectMergeAlgorithm.mergeRects(rects);

                saurus3D$mergedAnimatedSprites = new HashMap<>();
                for (TexRect r: result)
                {
                    List<TextureAtlasSprite> sprites = new ArrayList<>();
                    saurus3D$mergedAnimatedSprites.put(r, sprites);
                    for (TextureAtlasSprite sprite: listAnimatedSprites)
                        if (r.contains(((ITextureAtlasSpriteExtra)sprite).getRect()))
                            sprites.add(sprite);
                }

                int count = 0;
                for (List<TextureAtlasSprite> list: saurus3D$mergedAnimatedSprites.values())
                    count += list.size();
                if (count != listAnimatedSprites.size())
                    throw new GLIllegalStateException("TextureMap post-texture-stitching merging algorithm ran into a problem.");

                saurus3D$textureUploader = new ArrayList<>();

                // 4 extra bytes to avoid potential overflow
                int bufferSize = 4;
                for (TexRect rect: saurus3D$mergedAnimatedSprites.keySet())
                {
                    bufferSize += rect.width * rect.height * 4;
                    saurus3D$textureUploader.add(new TextureUploaderV2());
                }

                for (ITextureUploader uploader: saurus3D$textureUploader)
                {
                    if (uploader instanceof TextureUploaderV1 v1)
                        v1.init(bufferSize);
                    else if (uploader instanceof TextureUploaderV2 v2)
                        v2.init(bufferSize, 2);
                }
            }
        }
    }

    @WrapMethod(method = "updateAnimations")
    public void updateAnimations(Operation<Void> original)
    {
        if (!saurus3D$enableBatchTexUpload || saurus3D$mergedAnimatedSprites == null)
        {
            original.call();
            return;
        }

        GlStateManager.bindTexture(((TextureMap)(Object)this).getGlTextureId());

        for (TextureAtlasSprite sprite: this.listAnimatedSprites)
            ((ITextureAtlasSpriteExtra)sprite).setUpdated(false);

        int index = 0;
        for (Map.Entry<TexRect, List<TextureAtlasSprite>> entry: saurus3D$mergedAnimatedSprites.entrySet())
        {
            TexRect rect = entry.getKey();
            List<TextureAtlasSprite> sprites = entry.getValue();
            ITextureUploader uploader = saurus3D$textureUploader.get(index);

            boolean abort = false;
            uploader.reset();
            for (TextureAtlasSprite sprite: sprites)
            {
                ITextureAtlasSpriteExtra spriteExtra = ((ITextureAtlasSpriteExtra)sprite);

                TexUpdatePlan plan = spriteExtra.updateAnimationV2();
                for (int i = 0; i < plan.data.length; i++)
                {
                    if ((plan.rect.width >> i <= 0) || (plan.rect.height >> i <= 0))
                        abort = true;
                    else
                        uploader.planTexUpload(i, plan.data[i], plan.rect);
                }

                if (abort) break;

                spriteExtra.setUpdated(true);
            }

            if (abort)
            {
                for (TextureAtlasSprite sprite: sprites)
                    ((ITextureAtlasSpriteExtra)sprite).setUpdated(false);
            }
            else
                uploader.batchUpload(rect);

            index++;
        }

        for (TextureAtlasSprite sprite: this.listAnimatedSprites)
            if (!((ITextureAtlasSpriteExtra)sprite).isUpdated())
                sprite.updateAnimation();
    }
}
