package com.tttsaurus.saurus3d.config;

import net.minecraftforge.common.config.Configuration;

public final class Saurus3DMCPatchesConfig
{
    public static boolean ENABLE_TEXTUREMAP_BATCH_TEX_UPLOAD;
    public static int DOUBLE_BUFFERING_THRESHOLD;
    public static int TRIPLE_BUFFERING_THRESHOLD;

    public static Configuration CONFIG;

    public static void loadConfig()
    {
        try
        {
            CONFIG.load();

            ENABLE_TEXTUREMAP_BATCH_TEX_UPLOAD = CONFIG.getBoolean("Optimized TextureMap#updateAnimations", "general.texturemap", false, "Batch upload animated texture updates using PBO with async texture merging and interpolation.");
            DOUBLE_BUFFERING_THRESHOLD = CONFIG.getInt("PBO Double Buffering Threshold", "general.texturemap", 48 * 1024 - 1, 1, 0x7fffffff, "Data size threshold in bytes.");
            TRIPLE_BUFFERING_THRESHOLD = CONFIG.getInt("PBO Triple Buffering Threshold", "general.texturemap", 112 * 1024 - 1, 1, 0x7fffffff, "Data size threshold in bytes.");
        }
        catch (Exception ignored) { }
        finally
        {
            if (CONFIG.hasChanged()) CONFIG.save();
        }
    }
}
