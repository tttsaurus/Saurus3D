package com.tttsaurus.saurus3d.mcpatches.api.extra;

import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexRect;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexUpdatePlan;
import java.util.concurrent.Executor;

public interface ITextureAtlasSpriteExtra
{
    TexRect getRect();
    TexUpdatePlan updateAnimationV2(Executor executor);
}
