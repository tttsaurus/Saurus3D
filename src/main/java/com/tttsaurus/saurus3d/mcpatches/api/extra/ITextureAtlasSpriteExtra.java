package com.tttsaurus.saurus3d.mcpatches.api.extra;

import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexRect;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexUpdatePlan;

public interface ITextureAtlasSpriteExtra
{
    TexRect getRect();
    TexUpdatePlan updateAnimationV2();
}
