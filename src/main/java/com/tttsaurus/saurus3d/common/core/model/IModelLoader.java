package com.tttsaurus.saurus3d.common.core.model;

import com.tttsaurus.saurus3d.common.core.mesh.Mesh;

import javax.annotation.Nullable;

public interface IModelLoader
{
    @Nullable
    Mesh load(String rl);
}
