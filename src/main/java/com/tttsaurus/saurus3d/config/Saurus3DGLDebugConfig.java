package com.tttsaurus.saurus3d.config;

import com.tttsaurus.saurus3d.common.core.gl.debug.DebugMessageFilter;
import com.tttsaurus.saurus3d.common.core.gl.debug.DebugMsgSeverity;
import com.tttsaurus.saurus3d.common.core.gl.debug.DebugMsgSource;
import com.tttsaurus.saurus3d.common.core.gl.debug.DebugMsgType;
import net.minecraftforge.common.config.Configuration;
import java.util.ArrayList;
import java.util.List;

public final class Saurus3DGLDebugConfig
{
    public static boolean ENABLE_INGAME_DEBUG;
    public static boolean ENABLE_AUTO_DEBUG;
    public static final List<DebugMessageFilter> AUTO_DEBUG_MSG_FILTERS = new ArrayList<>();

    public static Configuration CONFIG;

    public static void loadConfig()
    {
        try
        {
            CONFIG.load();

            ENABLE_INGAME_DEBUG = CONFIG.getBoolean("Ingame Debug Log", "general", false, "Send GL error logs to chat");
            ENABLE_AUTO_DEBUG = CONFIG.getBoolean("KHR Auto Debug", "general", false, "GL errors will be automatically logged\nRequires GL43 or above or KHR_debug extension");

            String source = CONFIG.getString("Auto Debug Source Filters", "general.filter", "ANY", "Valid values are:\nANY\nAPI\nWINDOW_SYSTEM\nSHADER_COMPILER\nTHIRD_PARTY\nAPPLICATION\nOTHER");
            String type = CONFIG.getString("Auto Debug Type Filters", "general.filter", "ERROR", "Valid values are:\nANY\nERROR\nDEPRECATED_BEHAVIOR\nUNDEFINED_BEHAVIOR\nPORTABILITY\nPERFORMANCE\nMARKER\nOTHER");
            String severity = CONFIG.getString("Auto Debug Severity Filters", "general.filter", "ANY", "Valid values are:\nANY\nHIGH\nMEDIUM\nLOW\nNOTIFICATION");

            String[] sourceArgs = source.split(",");
            String[] typeArgs = type.split(",");
            String[] severityArgs = severity.split(",");
            int length = Math.max(Math.max(sourceArgs.length, typeArgs.length), severityArgs.length);

            AUTO_DEBUG_MSG_FILTERS.clear();
            int i = 0, j = 0, k = 0;
            for (int a = 0; a < length; a++)
            {
                DebugMsgSource source1 = DebugMsgSource.ANY;
                try
                {
                    source1 = DebugMsgSource.valueOf(sourceArgs[i].trim());
                }
                catch (IllegalArgumentException ignored) { }

                DebugMsgType type1 = DebugMsgType.ANY;
                try
                {
                    type1 = DebugMsgType.valueOf(typeArgs[j].trim());
                }
                catch (IllegalArgumentException ignored) { }

                DebugMsgSeverity severity1 = DebugMsgSeverity.ANY;
                try
                {
                    severity1 = DebugMsgSeverity.valueOf(severityArgs[k].trim());
                }
                catch (IllegalArgumentException ignored) { }

                AUTO_DEBUG_MSG_FILTERS.add(new DebugMessageFilter(source1, type1, severity1));

                if (i < sourceArgs.length - 1) i++;
                if (j < typeArgs.length - 1) j++;
                if (k < severityArgs.length - 1) k++;
            }
        }
        catch (Exception ignored) { }
        finally
        {
            if (CONFIG.hasChanged()) CONFIG.save();
        }
    }
}
