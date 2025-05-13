package com.tttsaurus.saurus3d.common.impl.shader;

import com.tttsaurus.saurus3d.common.core.shader.IShaderLoader;
import com.tttsaurus.saurus3d.common.core.shader.Shader;
import com.tttsaurus.saurus3d.common.core.reader.RlReaderUtils;

public class ShaderLoader implements IShaderLoader
{
    @Override
    public Shader load(String rl, Shader.ShaderType shaderType)
    {
        String raw = RlReaderUtils.read(rl, true);
        if (raw.isEmpty()) return null;

        return new Shader(rl, raw, shaderType);
    }
}
