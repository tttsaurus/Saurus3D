package com.tttsaurus.saurus3d.mixin.early;

import com.tttsaurus.saurus3d.common.core.mcpatches.IBufferBuilderExtra;
import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin implements IBufferBuilderExtra
{
    @Shadow
    private ByteBuffer byteBuffer;

    @Override
    public ByteBuffer getByteBuffer()
    {
        return byteBuffer;
    }
}
