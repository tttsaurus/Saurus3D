package com.tttsaurus.saurus3d.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLIllegalStateException;
import com.tttsaurus.saurus3d.common.core.gl.resource.GLDisposable;
import com.tttsaurus.saurus3d.common.core.gl.resource.GLResourceManager;
import com.tttsaurus.saurus3d.config.Saurus3DMCPatchesConfig;
import com.tttsaurus.saurus3d.mcpatches.api.extra.ITextureAtlasSpriteExtra;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.ITextureUploader;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexRect;
import com.tttsaurus.saurus3d.mcpatches.impl.texturemap.RectMergeAlgorithm;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexUpdatePlan;
import com.tttsaurus.saurus3d.mcpatches.impl.texturemap.TextureUploaderV1;
import com.tttsaurus.saurus3d.mcpatches.impl.texturemap.TextureUploaderV2;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

@Mixin(TextureMap.class)
public class TextureMapMixin
{
    @Unique
    private Map<TexRect, List<TextureAtlasSprite>> saurus3D$mergedAnimatedSprites;

    @Unique
    private List<ITextureUploader> saurus3D$textureUploaders;

    @Unique
    private ExecutorService saurus3D$executor;

    @Shadow
    @Final
    protected List<TextureAtlasSprite> listAnimatedSprites;

    @Inject(method = "loadTextureAtlas", at = @At("RETURN"))
    private void afterLoadTextureAtlas(IResourceManager resourceManager, CallbackInfo ci)
    {
        if (Saurus3DMCPatchesConfig.ENABLE_TEXTUREMAP_BATCH_TEX_UPLOAD)
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

            Saurus3D.LOGGER.info(String.format("Merged %d animated textures into %d rectangles.", count, saurus3D$mergedAnimatedSprites.size()));

            if (saurus3D$textureUploaders != null)
                for (ITextureUploader uploader: saurus3D$textureUploaders)
                    uploader.dispose();

            saurus3D$textureUploaders = new ArrayList<>();

            for (TexRect rect: saurus3D$mergedAnimatedSprites.keySet())
            {
                int data = rect.width * rect.height * 4;

                StringBuilder builder = new StringBuilder();
                builder.append(String.format("Animated texture rectangle: x=%d, y=%d, width=%d, height=%d; Data size: %d bytes; ",
                        rect.x,
                        rect.y,
                        rect.width,
                        rect.height,
                        data));

                if (data < Saurus3DMCPatchesConfig.DOUBLE_BUFFERING_THRESHOLD)
                {
                    builder.append("Using TextureUploaderV1");
                    TextureUploaderV1 uploader = new TextureUploaderV1();
                    uploader.init(data + 128);
                    saurus3D$textureUploaders.add(uploader);
                }
                else if (data < Saurus3DMCPatchesConfig.TRIPLE_BUFFERING_THRESHOLD)
                {
                    builder.append("Using TextureUploaderV2 Double Buffering");
                    TextureUploaderV2 uploader = new TextureUploaderV2();
                    uploader.init(data + 128, 2);
                    saurus3D$textureUploaders.add(uploader);
                }
                else
                {
                    builder.append("Using TextureUploaderV2 Triple Buffering");
                    TextureUploaderV2 uploader = new TextureUploaderV2();
                    uploader.init(data + 128, 3);
                    saurus3D$textureUploaders.add(uploader);
                }

                Saurus3D.LOGGER.info(builder.toString());
            }

            if (saurus3D$executor == null)
            {
                ForkJoinPool.ForkJoinWorkerThreadFactory factory = pool ->
                {
                    ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                    worker.setName("Saurus3D TextureMap Common Pool-" + worker.getPoolIndex());
                    return worker;
                };

                saurus3D$executor = new ForkJoinPool(
                        Runtime.getRuntime().availableProcessors(),
                        factory,
                        null,
                        true);

                GLResourceManager.addDisposable(new GLDisposable()
                {
                    @Override
                    public void dispose()
                    {
                        saurus3D$executor.shutdown();
                    }

                    @Override
                    public String getResourceType()
                    {
                        return "Saurus3D TextureMap Common Pool";
                    }
                });
            }
        }
    }

    @Inject(method = "updateAnimations", at = @At("HEAD"))
    public void updateAnimations(CallbackInfo ci)
    {

    }

    @WrapMethod(method = "updateAnimations")
    public void updateAnimations(Operation<Void> original)
    {
        if (!Saurus3DMCPatchesConfig.ENABLE_TEXTUREMAP_BATCH_TEX_UPLOAD || saurus3D$mergedAnimatedSprites == null)
        {
            original.call();
            return;
        }

        GlStateManager.bindTexture(((TextureMap)(Object)this).getGlTextureId());

        boolean setTexParam = true;
        int index = 0;
        for (Map.Entry<TexRect, List<TextureAtlasSprite>> entry: saurus3D$mergedAnimatedSprites.entrySet())
        {
            TexRect rect = entry.getKey();
            List<TextureAtlasSprite> sprites = entry.getValue();
            ITextureUploader uploader = saurus3D$textureUploaders.get(index);

            uploader.reset();
            for (TextureAtlasSprite sprite: sprites)
            {
                ITextureAtlasSpriteExtra spriteExtra = ((ITextureAtlasSpriteExtra)sprite);
                TexUpdatePlan plan = spriteExtra.updateAnimationV2(saurus3D$executor);
                for (int i = 0; i < plan.data.length; i++)
                    uploader.planTexUpload(i, plan.data[i], plan.rect);
            }

            uploader.batchUpload(rect, setTexParam, saurus3D$executor);
            if (setTexParam) setTexParam = false;

            index++;
        }
    }
}
