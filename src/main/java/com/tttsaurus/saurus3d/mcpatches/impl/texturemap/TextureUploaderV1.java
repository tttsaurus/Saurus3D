package com.tttsaurus.saurus3d.mcpatches.impl.texturemap;

import com.tttsaurus.saurus3d.mcpatches.api.texturemap.ITextureUploader;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexRect;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class TextureUploaderV1 implements ITextureUploader
{
    private ByteBuffer byteBuffer;

    private final Map<Integer, List<int[]>> mipmapData = new TreeMap<>();
    private final Map<Integer, List<TexRect>> mipmapRect = new TreeMap<>();

    private final Map<Integer, CompletableFuture<?>> mergingProcesses = new HashMap<>();
    private boolean skipFirstTick;

    public void init(int bufferSize)
    {
        byteBuffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        skipFirstTick = true;
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

    private final Map<Integer, int[]> mergedDataContainer = new HashMap<>();

    public void batchUpload(TexRect rect, boolean setTexParam, Executor executor)
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

            // complete mergedDataContainer in the first tick
            int[] merged = mergedDataContainer.computeIfAbsent(level, k -> new int[bigRect.width * bigRect.height]);

            boolean mergedReady = false;
            CompletableFuture<?> process = mergingProcesses.get(level);
            if (process == null)
            {
                // first tick
                mergingProcesses.put(level, CompletableFuture.runAsync(() ->
                {
                    TextureMerger.mergeTexs(merged, bigRect, rects, datas);
                }, executor));
            }
            else if (process.isDone())
                mergedReady = true;

            if (!skipFirstTick)
            {
                if (!mergedReady)
                    process.join();

                byteBuffer.position(0);
                byteBuffer.clear();
                IntBuffer intView = byteBuffer.asIntBuffer();
                intView.put(merged);
                intView.flip();

                GlStateManager.glTexSubImage2D(GL11.GL_TEXTURE_2D, level, bigRect.x, bigRect.y, bigRect.width, bigRect.height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, intView);

                // merging textures for the next tick
                mergingProcesses.put(level, CompletableFuture.runAsync(() ->
                {
                    TextureMerger.mergeTexs(merged, bigRect, rects, datas);
                }, executor));
            }
        }

        if (skipFirstTick)
            skipFirstTick = false;
    }

    @Override
    public void dispose()
    {

    }
}
