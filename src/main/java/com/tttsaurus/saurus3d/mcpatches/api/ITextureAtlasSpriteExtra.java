package com.tttsaurus.saurus3d.mcpatches.api;

import com.tttsaurus.saurus3d.mcpatches.impl.texturemap.TexRect;

public interface ITextureAtlasSpriteExtra
{
    boolean isUpdated();
    void setUpdated(boolean updated);
    TexRect getRect();

}
