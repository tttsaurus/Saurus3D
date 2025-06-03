package com.tttsaurus.saurus3d.mcpatches.impl.texturemap;

import com.tttsaurus.saurus3d.mcpatches.api.texturemap.ITextureUploader;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexRect;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class TextureUploaderV1 implements ITextureUploader
{
    private ByteBuffer byteBuffer;

    private final Map<Integer, List<int[]>> mipmapData = new TreeMap<>();
    private final Map<Integer, List<TexRect>> mipmapRect = new TreeMap<>();

    public void init(int bufferSize)
    {
        byteBuffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
    }

    public void reset()
    {
        mipmapData.clear();
        mipmapRect.clear();
    }

    public void planTexUpload(int level, int[] data, TexRect rect)
    {
        if (!mipmapData.containsKey(level))
            mipmapData.put(level, new ArrayList<>());
        if (!mipmapRect.containsKey(level))
            mipmapRect.put(level, new ArrayList<>());

        mipmapData.get(level).add(data);
        mipmapRect.get(level).add(new TexRect(rect.x >> level, rect.y >> level, rect.width >> level, rect.height >> level));
    }

    public void batchUpload(TexRect rect, boolean setTexParam)
    {
        boolean mipmap = mipmapData.size() > 1;

        if (setTexParam)
        {
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipmap ? GL11.GL_NEAREST_MIPMAP_LINEAR : GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        }

        for (Map.Entry<Integer, List<int[]>> entry: mipmapData.entrySet())
        {
            int level = entry.getKey();

            TexRect bigRect = new TexRect(rect.x >> level, rect.y >> level, rect.width >> level, rect.height >> level);
            List<TexRect> rects = mipmapRect.get(level);
            List<int[]> datas = entry.getValue();

            int[] mergedData = TextureMerger.mergeTexs(bigRect, rects, datas);

            byteBuffer.position(0);
            byteBuffer.clear();
            IntBuffer intView = byteBuffer.asIntBuffer();
            intView.put(mergedData);
            intView.flip();

            GlStateManager.glTexSubImage2D(GL11.GL_TEXTURE_2D, level, bigRect.x, bigRect.y, bigRect.width, bigRect.height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, intView);
        }
    }

    @Override
    public void dispose()
    {

    }
}
