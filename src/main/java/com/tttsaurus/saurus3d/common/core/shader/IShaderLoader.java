package com.tttsaurus.saurus3d.common.core.shader;

import javax.annotation.Nullable;

public interface IShaderLoader
{
    @Nullable
    Shader load(String rl, Shader.ShaderType shaderType);
}
