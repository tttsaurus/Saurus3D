package com.tttsaurus.saurus3d.mcpatches.api.extra;

import com.tttsaurus.saurus3d.common.core.mesh.Mesh;
import java.nio.ByteBuffer;

public interface IRenderChunkExtra
{
    ByteBuffer[] getVboByteBuffers();
    ByteBuffer[] getEboByteBuffers();
    Mesh[] getMeshes();
}
