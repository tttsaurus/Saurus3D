package com.tttsaurus.saurus3d.mixin;

import com.tttsaurus.saurus3d.mcpatches.api.extra.ITextureAtlasSpriteExtra;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexRect;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexUpdatePlan;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(TextureAtlasSprite.class)
public class TextureAtlasSpriteMixin implements ITextureAtlasSpriteExtra
{
    @Shadow
    protected int originX;

    @Shadow
    protected int originY;

    @Shadow
    protected int width;

    @Shadow
    protected int height;

    @Override
    public TexRect getRect()
    {
        return new TexRect(originX, originY, width, height);
    }

    @Shadow
    protected int tickCounter;

    @Shadow
    private AnimationMetadataSection animationMetadata;

    @Shadow
    protected int frameCounter;

    @Shadow
    protected List<int[][]> framesTextureData;

    @Shadow
    protected int[][] interpolatedFrameData;

    @Unique
    private CompletableFuture<?> saurus3D$interpolationProcess;

    @Override
    public TexUpdatePlan updateAnimationV2(Executor executor)
    {
        ++this.tickCounter;

        if (this.tickCounter >= this.animationMetadata.getFrameTimeSingle(this.frameCounter))
        {
            //int i = this.animationMetadata.getFrameIndex(this.frameCounter);
            int j = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
            this.frameCounter = (this.frameCounter + 1) % j;
            this.tickCounter = 0;
            int k = this.animationMetadata.getFrameIndex(this.frameCounter);

            return new TexUpdatePlan(getRect(), this.framesTextureData.get(k));
        }
        else if (this.animationMetadata.isInterpolate())
        {
            if (saurus3D$interpolationProcess == null)
            {
                saurus3D$updateAnimationInterpolated();

                TexUpdatePlan plan = new TexUpdatePlan(getRect(), this.interpolatedFrameData);

                int[][] newFrameData = new int[this.interpolatedFrameData.length][];
                for (int i = 0; i < newFrameData.length; i++)
                    newFrameData[i] = new int[this.interpolatedFrameData[i].length];

                this.interpolatedFrameData = newFrameData;

                saurus3D$interpolationProcess = CompletableFuture.runAsync(
                        this::saurus3D$updateAnimationInterpolated,
                        executor);

                return plan;
            }
            else
            {
                if (!saurus3D$interpolationProcess.isDone())
                    saurus3D$interpolationProcess.join();

                TexUpdatePlan plan = new TexUpdatePlan(getRect(), this.interpolatedFrameData);

                int[][] newFrameData = new int[this.interpolatedFrameData.length][];
                for (int i = 0; i < newFrameData.length; i++)
                    newFrameData[i] = new int[this.interpolatedFrameData[i].length];

                this.interpolatedFrameData = newFrameData;

                saurus3D$interpolationProcess = CompletableFuture.runAsync(
                        this::saurus3D$updateAnimationInterpolated,
                        executor);

                return plan;
            }
        }

        return new TexUpdatePlan(getRect(), this.framesTextureData.get(this.animationMetadata.getFrameIndex(this.frameCounter)));
    }

    @Unique
    private void saurus3D$updateAnimationInterpolated()
    {
        double d0 = 1.0D - (double)this.tickCounter / (double)this.animationMetadata.getFrameTimeSingle(this.frameCounter);
        int i = this.animationMetadata.getFrameIndex(this.frameCounter);
        int j = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
        int k = this.animationMetadata.getFrameIndex((this.frameCounter + 1) % j);

        int[][] aint = this.framesTextureData.get(i);
        int[][] aint1 = this.framesTextureData.get(k);

        if (this.interpolatedFrameData == null || this.interpolatedFrameData.length != aint.length)
            this.interpolatedFrameData = new int[aint.length][];

        for (int l = 0; l < aint.length; ++l)
        {
            if (this.interpolatedFrameData[l] == null)
                this.interpolatedFrameData[l] = new int[aint[l].length];

            if (l < aint1.length && aint1[l].length == aint[l].length)
            {
                for (int i1 = 0; i1 < aint[l].length; ++i1)
                {
                    int j1 = aint[l][i1];
                    int k1 = aint1[l][i1];
                    int l1 = saurus3D$interpolateColor(d0, j1 >> 16 & 255, k1 >> 16 & 255);
                    int i2 = saurus3D$interpolateColor(d0, j1 >> 8 & 255, k1 >> 8 & 255);
                    int j2 = saurus3D$interpolateColor(d0, j1 & 255, k1 & 255);
                    this.interpolatedFrameData[l][i1] = j1 & -16777216 | l1 << 16 | i2 << 8 | j2;
                }
            }
        }
    }

    @Unique
    private int saurus3D$interpolateColor(double p_188535_1_, int p_188535_3_, int p_188535_4_)
    {
        return (int)(p_188535_1_ * (double)p_188535_3_ + (1.0D - p_188535_1_) * (double)p_188535_4_);
    }
}
