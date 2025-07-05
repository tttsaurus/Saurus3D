package com.tttsaurus.saurus3d.common.core.shader;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.commonutils.RLReaderUtils;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public final class ShaderManager
{
    // key: rl
    // value: shader hash
    private static final Map<String, String> rlHashMapping = new HashMap<>();
    // key: shader hash
    private static final Map<String, Shader> shaders = new HashMap<>();

    private static String hashShader(String shaderSource)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(shaderSource.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        }
        catch (Exception ignored) { return null; }
    }

    public static boolean loadShader(String rl, Shader.ShaderType shaderType)
    {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement caller = null;
        if (stackTrace.length > 2) caller = stackTrace[2];

        String raw = RLReaderUtils.read(rl, true);

        String hash = hashShader(raw + shaderType.toString());
        Shader shader = shaders.get(hash);
        if (shader == null)
        {
            shader = new Shader(rl, raw, shaderType);
            shader.compile();

            if (!shader.getValidity())
            {
                StringBuilder builder = new StringBuilder();
                builder.append("Shader Compile Error: ").append(shader.getErrorLog()).append(".\n").append("\tThis ShaderManager.loadShader() call is executed at: ");

                if (caller == null)
                    builder.append("Can't find");
                else
                    builder.append(String.format("%s (%s:%d)", caller.getMethodName(), caller.getFileName(), caller.getLineNumber()));

                Saurus3D.LOGGER.warn(builder.toString());
            }

            shaders.put(hash, shader);
            rlHashMapping.put(rl, hash);

            return true;
        }

        return false;
    }

    @Nullable
    public static Shader getShader(String rl)
    {
        String hash = rlHashMapping.get(rl);
        if (hash == null) return null;
        return shaders.get(hash);
    }

    private static final Map<String, ShaderProgram> shaderPrograms = new HashMap<>();

    public static ShaderProgram createShaderProgram(String key, Shader... shaders)
    {
        ShaderProgram shaderProgram = new ShaderProgram(shaders);
        shaderProgram.setup();
        return shaderPrograms.put(key, shaderProgram);
    }

    @Nullable
    public static ShaderProgram getShaderProgram(String key)
    {
        return shaderPrograms.get(key);
    }
}
