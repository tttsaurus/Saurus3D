package com.tttsaurus.saurus3d.mixin.early;

import com.tttsaurus.saurus3d.common.core.mcpatches.ITextureAtlasSpriteExtra;
import com.tttsaurus.saurus3d.common.core.mcpatches.Rect;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TextureAtlasSprite.class)
public class TextureAtlasSpriteMixin implements ITextureAtlasSpriteExtra
{
    @Unique
    private boolean saurus3D$uploaded;

    @Override
    public boolean uploaded()
    {
        return saurus3D$uploaded;
    }

    @Override
    public void setUploaded(boolean uploaded)
    {
        saurus3D$uploaded = uploaded;
    }

    @Shadow
    protected boolean rotated;

    @Override
    public boolean getRotated()
    {
        return rotated;
    }

    @Override
    public Rect getRect()
    {
        TextureAtlasSprite this0 = ((TextureAtlasSprite)(Object)this);
        int x = this0.getOriginX();
        int y = this0.getOriginY();
        boolean rotated = ((ITextureAtlasSpriteExtra)this0).getRotated();
        int width = rotated ? this0.getIconHeight() : this0.getIconWidth();
        int height = rotated ? this0.getIconWidth() : this0.getIconHeight();
        return new Rect(x, y, width, height);
    }
}
