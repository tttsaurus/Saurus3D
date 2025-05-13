package com.tttsaurus.saurus3d.common.core.shader;

import com.tttsaurus.saurus3d.common.core.reader.RlReaderUtils;

public final class ShaderLoadingUtils
{
    public static Shader load(String rl, Shader.ShaderType shaderType)
    {
        String raw = RlReaderUtils.read(rl, true);
        if (raw.isEmpty()) return null;

        return new Shader(rl, raw, shaderType);
    }
}
