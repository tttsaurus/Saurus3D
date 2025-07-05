package com.tttsaurus.saurus3d.common.core.shader;

import com.tttsaurus.saurus3d.common.core.commonutils.RLReaderUtils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public final class ShaderManager
{
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

    public static Shader loadShader(String rl, Shader.ShaderType shaderType)
    {
        String raw = RLReaderUtils.read(rl, true);
        if (raw.isEmpty()) return null;

        String hash = hashShader(raw + shaderType.toString());
        Shader shader = shaders.get(hash);
        if (shader == null)
        {
            shader = new Shader(rl, raw, shaderType);
            shader.compile();
            shaders.put(hash, shader);
        }

        return shader;
    }
}
