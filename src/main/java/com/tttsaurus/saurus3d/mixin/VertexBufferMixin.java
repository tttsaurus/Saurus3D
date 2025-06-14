package com.tttsaurus.saurus3d.mixin;

import com.tttsaurus.saurus3d.mcpatches.api.extra.IVertexBufferExtra;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VertexBuffer.class)
public class VertexBufferMixin implements IVertexBufferExtra
{
    @Shadow
    private int glBufferId;

    @Override
    public int getBufferID()
    {
        return glBufferId;
    }
}
