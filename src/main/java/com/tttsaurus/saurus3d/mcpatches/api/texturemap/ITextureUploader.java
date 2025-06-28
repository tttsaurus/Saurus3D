package com.tttsaurus.saurus3d.mcpatches.api.texturemap;

import java.util.concurrent.Executor;

public interface ITextureUploader
{
    void reset();
    void planTexUpload(int level, int[] data, TexRect rect);
    void batchUpload(TexRect rect, boolean setTexParam, Executor executor);
    void dispose();
}
