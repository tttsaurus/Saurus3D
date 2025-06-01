package com.tttsaurus.saurus3d.mixin.early;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tttsaurus.saurus3d.common.core.buffer.BufferUploadHint;
import com.tttsaurus.saurus3d.common.core.buffer.MapBufferAccessBit;
import com.tttsaurus.saurus3d.common.core.buffer.PBO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

@Mixin(TextureUtil.class)
public class TextureUtilMixin
{
//    @Unique
//    private static final int saurus3D$maxSubImageSize = 256 * 256 * 4;
//
//    @Unique
//    private static ByteBuffer saurus3D$pboByteBuffer;
//
//    @Unique
//    private static PBO saurus3D$pbo;
//
//    @WrapOperation(method = "uploadTextureSub", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureUtil;copyToBufferPos([III)V"))
//    private static void copyToBufferPos(int[] p_110994_0_, int p_110994_1_, int p_110994_2_, Operation<Void> original)
//    {
//        if (saurus3D$pboByteBuffer == null)
//            saurus3D$pboByteBuffer = ByteBuffer.allocateDirect(saurus3D$maxSubImageSize).order(ByteOrder.nativeOrder());
//
//        int[] aint = p_110994_0_;
//
//        if (Minecraft.getMinecraft().gameSettings.anaglyph)
//        {
//            aint = TextureUtil.updateAnaglyph(p_110994_0_);
//        }
//
//        saurus3D$pboByteBuffer.position(0);
//        saurus3D$pboByteBuffer.clear();
//        IntBuffer intView = saurus3D$pboByteBuffer.asIntBuffer();
//        intView.clear();
//        intView.put(aint, p_110994_1_, p_110994_2_);
//        saurus3D$pboByteBuffer.position(0);
//        saurus3D$pboByteBuffer.limit(p_110994_2_ * Integer.BYTES);
//    }
//
//    @WrapOperation(method = "uploadTextureSub", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;glTexSubImage2D(IIIIIIIILjava/nio/IntBuffer;)V"))
//    private static void glTexSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, IntBuffer pixels, Operation<Void> original)
//    {
//        if (saurus3D$pbo == null)
//        {
//            saurus3D$pbo = new PBO();
//            saurus3D$pbo.setPboID(PBO.genPboID());
//            saurus3D$pbo.allocNewGpuMem(saurus3D$maxSubImageSize, BufferUploadHint.STREAM_DRAW);
//        }
//
//        saurus3D$pbo.uploadByMappedBuffer(0, saurus3D$pboByteBuffer.remaining(), 0, saurus3D$pboByteBuffer);
////        saurus3D$pbo.uploadByMappedBuffer(0, saurus3D$pboByteBuffer.remaining(), 0, saurus3D$pboByteBuffer,
////                MapBufferAccessBit.WRITE_BIT,
////                MapBufferAccessBit.INVALIDATE_BUFFER_BIT,
////                MapBufferAccessBit.UNSYNCHRONIZED_BIT);
//        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, level, xOffset, yOffset, width, height, format, type, 0);
//        saurus3D$pbo.restorePrevPbo();
//    }
}
