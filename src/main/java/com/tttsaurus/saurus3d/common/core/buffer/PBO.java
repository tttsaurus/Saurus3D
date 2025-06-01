package com.tttsaurus.saurus3d.common.core.buffer;

import com.tttsaurus.saurus3d.common.core.gl.exception.GLIllegalBufferIDException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLIllegalStateException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLMapBufferException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLOverflowException;
import com.tttsaurus.saurus3d.common.core.gl.resource.GLDisposable;
import com.tttsaurus.saurus3d.common.core.gl.resource.GLResourceManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class PBO extends GLDisposable
{
    private BufferID pboID = null;
    private int pboSize;

    public int getPboSize() { return pboSize; }

    private boolean autoRebindToOldPbo = false;
    public boolean isAutoRebindToOldPbo() { return autoRebindToOldPbo; }
    public void setAutoRebindToOldPbo(boolean autoRebindToOldPbo) { this.autoRebindToOldPbo = autoRebindToOldPbo; }

    int prevPbo = 0;
    public void storePrevPbo()
    {
        prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
    }
    public void restorePrevPbo()
    {
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);
    }

    public void updateSize()
    {
        if (pboID == null)
            throw new GLIllegalBufferIDException("Must set a PBO ID first.");

        int prevPbo = 0;
        if (autoRebindToOldPbo) prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboID.id);
        pboSize = GL15.glGetBufferParameteri(GL21.GL_PIXEL_UNPACK_BUFFER, GL15.GL_BUFFER_SIZE);
        if (autoRebindToOldPbo) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);
    }

    //<editor-fold desc="id">
    public void setPboID(BufferID pboID)
    {
        if (this.pboID != null)
            throw new GLIllegalStateException("Cannot set PBO ID again.");
        if (pboID.type != BufferType.PBO)
            throw new GLIllegalBufferIDException("Buffer ID must be a PBO ID.");

        this.pboID = pboID;

        int prevPbo = 0;
        if (autoRebindToOldPbo) prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboID.id);
        pboSize = GL15.glGetBufferParameteri(GL21.GL_PIXEL_UNPACK_BUFFER, GL15.GL_BUFFER_SIZE);
        if (autoRebindToOldPbo) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);

        GLResourceManager.addDisposable(this);
    }
    public BufferID getPboID()
    {
        return pboID;
    }
    public static BufferID genPboID()
    {
        return new BufferID(GL15.glGenBuffers(), BufferType.PBO);
    }
    //</editor-fold>

    //<editor-fold desc="upload">
    public void directUpload(int[] arr)
    {
        ByteBuffer byteBuffer = ByteBuffer
                .allocateDirect(arr.length * Integer.BYTES)
                .order(ByteOrder.nativeOrder());

        IntBuffer intView = byteBuffer.asIntBuffer();
        intView.put(arr);

        byteBuffer.limit(intView.limit() * Integer.BYTES);
        byteBuffer.position(0);

        directUpload(byteBuffer);
    }
    public void directUpload(ByteBuffer byteBuffer)
    {
        if (pboID == null)
            throw new GLIllegalBufferIDException("Must set a PBO ID first.");

        int prevPbo = 0;
        if (autoRebindToOldPbo) prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboID.id);
        GL15.glBufferData(GL21.GL_PIXEL_UNPACK_BUFFER, byteBuffer, GL15.GL_STATIC_DRAW);
        if (autoRebindToOldPbo) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);

        pboSize = byteBuffer.remaining();
    }
    public void allocNewGpuMem(int size, BufferUploadHint hint)
    {
        if (pboID == null)
            throw new GLIllegalBufferIDException("Must set a PBO ID first.");
        if (size < 0)
            throw new GLIllegalStateException("Cannot have negative size.");

        int prevPbo = 0;
        if (autoRebindToOldPbo) prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboID.id);
        GL15.glBufferData(GL21.GL_PIXEL_UNPACK_BUFFER, size, hint.glValue);
        if (autoRebindToOldPbo) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);

        pboSize = size;
    }
    public void uploadBySubData(int offset, ByteBuffer byteBuffer)
    {
        if (pboID == null)
            throw new GLIllegalBufferIDException("Must set a PBO ID first.");
        if (offset < 0)
            throw new GLOverflowException("Cannot have negative offset.");
        if (offset + byteBuffer.remaining() > pboSize)
            throw new GLOverflowException("Allocated PBO size must be greater than or equal to offset + byteBuffer.remaining().");

        int prevPbo = 0;
        if (autoRebindToOldPbo) prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboID.id);
        GL15.glBufferSubData(GL21.GL_PIXEL_UNPACK_BUFFER, offset, byteBuffer);
        if (autoRebindToOldPbo) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);
    }

    public void uploadByMappedBuffer(int mappingOffset, int mappingSize, int offset, ByteBuffer byteBuffer)
    {
        uploadByMappedBuffer(mappingOffset, mappingSize, offset, byteBuffer, true);
    }
    public void uploadByMappedBuffer(int mappingOffset, int mappingSize, int offset, ByteBuffer byteBuffer, MapBufferAccessBit... accessBits)
    {
        uploadByMappedBuffer(mappingOffset, mappingSize, offset, byteBuffer, true, accessBits);
    }
    public void uploadByMappedBuffer(int mappingOffset, int mappingSize, int offset, ByteBuffer byteBuffer, boolean unmap)
    {
        uploadByMappedBuffer(mappingOffset, mappingSize, offset, byteBuffer, unmap, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.INVALIDATE_RANGE_BIT);
    }
    public void uploadByMappedBuffer(int mappingOffset, int mappingSize, int offset, ByteBuffer byteBuffer, boolean unmap, MapBufferAccessBit... accessBits)
    {
        if (pboID == null)
            throw new GLIllegalBufferIDException("Must set a PBO ID first.");
        if (mappingSize < 0)
            throw new GLIllegalStateException("Cannot have negative size.");
        if (mappingOffset < 0 || offset < 0)
            throw new GLOverflowException("Cannot have negative offset.");
        if (mappingOffset + mappingSize > pboSize)
            throw new GLOverflowException("Allocated PBO size must be greater than or equal to mappingOffset + mappingSize.");
        if (offset + byteBuffer.remaining() > mappingSize)
            throw new GLOverflowException("Parameter mappingSize must be greater than or equal to offset + byteBuffer.remaining().");

        int prevPbo = 0;
        if (autoRebindToOldPbo) prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);

        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboID.id);

        int access = 0;
        for (MapBufferAccessBit bit: accessBits)
            access |= bit.glValue;

        ByteBuffer mappedBuffer = GL30.glMapBufferRange(
                GL21.GL_PIXEL_UNPACK_BUFFER,
                mappingOffset,
                mappingSize,
                access,
                null);

        if (mappedBuffer != null)
        {
            mappedBuffer.position(offset);
            mappedBuffer.put(byteBuffer);
            if (unmap) GL15.glUnmapBuffer(GL21.GL_PIXEL_UNPACK_BUFFER);
        }
        else
            throw new GLMapBufferException("Failed to map buffer.");

        if (autoRebindToOldPbo) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);
    }
    //</editor-fold>

    @Override
    public void dispose()
    {
        if (pboID != null)
            if (GL15.glIsBuffer(pboID.id))
                GL15.glDeleteBuffers(pboID.id);
    }
}
