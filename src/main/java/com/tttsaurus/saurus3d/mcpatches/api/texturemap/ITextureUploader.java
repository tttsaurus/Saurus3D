package com.tttsaurus.saurus3d.mcpatches.api.texturemap;

public interface ITextureUploader
{
    void reset();
    void planTexUpload(int level, int[] data, TexRect rect);
    void batchUpload(TexRect rect);
}
