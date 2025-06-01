package com.tttsaurus.saurus3d.mixin.early;

import com.tttsaurus.saurus3d.mcpatches.api.ITextureAtlasSpriteExtra;
import com.tttsaurus.saurus3d.mcpatches.impl.texturemap.TexRect;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import java.util.List;

@Mixin(TextureAtlasSprite.class)
public class TextureAtlasSpriteMixin implements ITextureAtlasSpriteExtra
{
    @Unique
    private boolean saurus3D$updated;

    @Override
    public boolean isUpdated()
    {
        return saurus3D$updated;
    }

    @Override
    public void setUpdated(boolean updated)
    {
        saurus3D$updated = updated;
    }

    @Shadow
    protected boolean rotated;

    @Override
    public TexRect getRect()
    {
        TextureAtlasSprite this0 = ((TextureAtlasSprite)(Object)this);
        int x = this0.getOriginX();
        int y = this0.getOriginY();
        int width = this.rotated ? this0.getIconHeight() : this0.getIconWidth();
        int height = this.rotated ? this0.getIconWidth() : this0.getIconHeight();
        return new TexRect(x, y, width, height);
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
    protected int originX;

    @Shadow
    protected int originY;

    @Shadow
    protected int width;

    @Shadow
    protected int height;

//    public void updateAnimation()
//    {
//        ++this.tickCounter;
//
//        if (this.tickCounter >= this.animationMetadata.getFrameTimeSingle(this.frameCounter))
//        {
//            int i = this.animationMetadata.getFrameIndex(this.frameCounter);
//            int j = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
//            this.frameCounter = (this.frameCounter + 1) % j;
//            this.tickCounter = 0;
//            int k = this.animationMetadata.getFrameIndex(this.frameCounter);
//
//            if (i != k && k >= 0 && k < this.framesTextureData.size())
//            {
//                TextureUtil.uploadTextureMipmap(this.framesTextureData.get(k), this.width, this.height, this.originX, this.originY, false, false);
//            }
//        }
//        else if (this.animationMetadata.isInterpolate())
//        {
//            this.updateAnimationInterpolated();
//        }
//    }
}
