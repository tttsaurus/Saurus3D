package com.tttsaurus.saurus3d.config;

import com.tttsaurus.saurus3d.common.core.gl.debug.DebugMessageFilter;
import com.tttsaurus.saurus3d.common.core.gl.debug.DebugMsgSeverity;
import com.tttsaurus.saurus3d.common.core.gl.debug.DebugMsgSource;
import com.tttsaurus.saurus3d.common.core.gl.debug.DebugMsgType;
import net.minecraftforge.common.config.Configuration;


public final class Saurus3DMCPatchesConfig
{
    public static boolean ENABLE_TEXTUREMAP_BATCH_TEX_UPLOAD;

    public static Configuration CONFIG;

    public static void loadConfig()
    {
        try
        {
            CONFIG.load();

            ENABLE_TEXTUREMAP_BATCH_TEX_UPLOAD = CONFIG.getBoolean("TextureMap Batch Upload", "general.texturemap", false, "Batch upload animated texture updates to optimize TextureMap#updateAnimations.");
        }
        catch (Exception ignored) { }
        finally
        {
            if (CONFIG.hasChanged()) CONFIG.save();
        }
    }
}
