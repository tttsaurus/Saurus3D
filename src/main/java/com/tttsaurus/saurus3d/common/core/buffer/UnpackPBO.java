package com.tttsaurus.saurus3d.common.core.buffer;

import com.tttsaurus.saurus3d.common.core.buffer.meta.BufferID;
import com.tttsaurus.saurus3d.common.core.buffer.meta.BufferType;
import com.tttsaurus.saurus3d.common.core.buffer.meta.BufferUploadHint;
import com.tttsaurus.saurus3d.common.core.buffer.meta.MapBufferAccessBit;
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
import org.lwjgl.system.MemoryStack;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class UnpackPBO extends GLDisposable
{
    private BufferID pboID = null;
    private int pboSize;

    public int getPboSize() { return pboSize; }

    private boolean autoUnbind = false;
    public boolean isAutoUnbind() { return autoUnbind; }
    public void setAutoUnbind(boolean autoUnbind) { this.autoUnbind = autoUnbind; }

    private boolean autoBind = true;
    public boolean isAutoBind() { return autoBind; }
    public void setAutoBind(boolean autoBind) { this.autoBind = autoBind; }

    int prevPbo = 0;
    public void storePrevPbo()
    {
        prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
    }
    public void restorePrevPbo()
    {
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);
    }

    public void fetchSize()
    {
        if (pboID == null)
            throw new GLIllegalBufferIDException("Must set a PBO ID first.");

        int prevPbo = 0;
        if (autoUnbind) prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboID.id);
        pboSize = GL15.glGetBufferParameteri(GL21.GL_PIXEL_UNPACK_BUFFER, GL15.GL_BUFFER_SIZE);
        if (autoUnbind) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);
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
        if (autoUnbind) prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboID.id);
        pboSize = GL15.glGetBufferParameteri(GL21.GL_PIXEL_UNPACK_BUFFER, GL15.GL_BUFFER_SIZE);
        if (autoUnbind) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);

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
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ByteBuffer byteBuffer = stack.malloc(4, arr.length * Integer.BYTES);

            IntBuffer intView = byteBuffer.asIntBuffer();
            intView.put(arr);

            byteBuffer.limit(intView.limit() * Integer.BYTES);
            byteBuffer.position(0);

            directUpload(byteBuffer);
        }
    }
    public void directUpload(ByteBuffer byteBuffer)
    {
        if (pboID == null)
            throw new GLIllegalBufferIDException("Must set a PBO ID first.");

        int prevPbo = 0;
        if (autoUnbind) prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboID.id);
        GL15.glBufferData(GL21.GL_PIXEL_UNPACK_BUFFER, byteBuffer, GL15.GL_STATIC_DRAW);
        if (autoUnbind) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);

        pboSize = byteBuffer.remaining();
    }
    public void allocNewGpuMem(int size, BufferUploadHint hint)
    {
        if (pboID == null)
            throw new GLIllegalBufferIDException("Must set a PBO ID first.");
        if (size < 0)
            throw new GLIllegalStateException("Cannot have negative size.");

        int prevPbo = 0;
        if (autoUnbind) prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboID.id);
        GL15.glBufferData(GL21.GL_PIXEL_UNPACK_BUFFER, size, hint.glValue);
        if (autoUnbind) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);

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
        if (autoUnbind) prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboID.id);
        GL15.glBufferSubData(GL21.GL_PIXEL_UNPACK_BUFFER, offset, byteBuffer);
        if (autoUnbind) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);
    }

    public void uploadByMappedBuffer(int mappingOffset, int mappingSize, int offset, ByteBuffer byteBuffer)
    {
        uploadByMappedBuffer(mappingOffset, mappingSize, offset, byteBuffer, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.INVALIDATE_RANGE_BIT);
    }
    public void uploadByMappedBuffer(int mappingOffset, int mappingSize, int offset, ByteBuffer byteBuffer, MapBufferAccessBit... accessBits)
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
        if (autoUnbind) prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);

        if (autoBind) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboID.id);

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
            GL15.glUnmapBuffer(GL21.GL_PIXEL_UNPACK_BUFFER);
        }
        else
            throw new GLMapBufferException("Failed to map buffer.");

        if (autoUnbind) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);
    }
    public void uploadByMappedBuffer(int mappingOffset, int mappingSize, int offset, int[] data)
    {
        uploadByMappedBuffer(mappingOffset, mappingSize, offset, data, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.INVALIDATE_RANGE_BIT);
    }
    public void uploadByMappedBuffer(int mappingOffset, int mappingSize, int offset, int[] data, MapBufferAccessBit... accessBits)
    {
        if (pboID == null)
            throw new GLIllegalBufferIDException("Must set a PBO ID first.");
        if (mappingSize < 0)
            throw new GLIllegalStateException("Cannot have negative size.");
        if (mappingOffset < 0 || offset < 0)
            throw new GLOverflowException("Cannot have negative offset.");
        if (mappingOffset + mappingSize > pboSize)
            throw new GLOverflowException("Allocated PBO size must be greater than or equal to mappingOffset + mappingSize.");
        if (offset + data.length * Integer.BYTES > mappingSize)
            throw new GLOverflowException("Parameter mappingSize must be greater than or equal to offset + data.length * Integer.BYTES.");

        int prevPbo = 0;
        if (autoUnbind) prevPbo = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);

        if (autoBind) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboID.id);

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
            IntBuffer intView = mappedBuffer.asIntBuffer();
            intView.put(data);
            boolean success = GL15.glUnmapBuffer(GL21.GL_PIXEL_UNPACK_BUFFER);
            if (!success) throw new GLIllegalStateException("Buffer unmap failed, data may be corrupted.");
        }
        else
            throw new GLMapBufferException("Failed to map buffer.");

        if (autoUnbind) GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPbo);
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
