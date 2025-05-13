package com.tttsaurus.saurus3d.common.core.model;

import javax.annotation.Nullable;

public interface IModelLoader
{
    @Nullable
    Mesh load(String rl);
}
