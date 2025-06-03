package com.tttsaurus.saurus3d.mixin.early;

import com.tttsaurus.saurus3d.mcpatches.api.extra.ITextureAtlasSpriteExtra;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexRect;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexUpdatePlan;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import java.util.List;

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

    @Override
    public TexUpdatePlan updateAnimationV2()
    {
        ++this.tickCounter;

        if (this.tickCounter >= this.animationMetadata.getFrameTimeSingle(this.frameCounter))
        {
            int i = this.animationMetadata.getFrameIndex(this.frameCounter);
            int j = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
            this.frameCounter = (this.frameCounter + 1) % j;
            this.tickCounter = 0;
            int k = this.animationMetadata.getFrameIndex(this.frameCounter);

            return new TexUpdatePlan(getRect(), this.framesTextureData.get(k));
        }
        else if (this.animationMetadata.isInterpolate())
        {
            return saurus3D$updateAnimationInterpolated();
        }

        return new TexUpdatePlan(getRect(), this.framesTextureData.get(this.animationMetadata.getFrameIndex(this.frameCounter)));
    }

    @Unique
    private TexUpdatePlan saurus3D$updateAnimationInterpolated()
    {
        double d0 = 1.0D - (double)this.tickCounter / (double)this.animationMetadata.getFrameTimeSingle(this.frameCounter);
        int i = this.animationMetadata.getFrameIndex(this.frameCounter);
        int j = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
        int k = this.animationMetadata.getFrameIndex((this.frameCounter + 1) % j);

        int[][] aint = this.framesTextureData.get(i);
        int[][] aint1 = this.framesTextureData.get(k);

        if (this.interpolatedFrameData == null || this.interpolatedFrameData.length != aint.length)
        {
            this.interpolatedFrameData = new int[aint.length][];
        }

        for (int l = 0; l < aint.length; ++l)
        {
            if (this.interpolatedFrameData[l] == null)
            {
                this.interpolatedFrameData[l] = new int[aint[l].length];
            }

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

        return new TexUpdatePlan(getRect(), this.interpolatedFrameData);
    }

    @Unique
    private int saurus3D$interpolateColor(double p_188535_1_, int p_188535_3_, int p_188535_4_)
    {
        return (int)(p_188535_1_ * (double)p_188535_3_ + (1.0D - p_188535_1_) * (double)p_188535_4_);
    }
}
