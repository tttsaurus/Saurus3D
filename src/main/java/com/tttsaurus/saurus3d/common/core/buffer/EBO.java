package com.tttsaurus.saurus3d.common.core.buffer;

import com.tttsaurus.saurus3d.common.core.gl.exception.GLIllegalBufferIDException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLIllegalStateException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLMapBufferException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLOverflowException;
import com.tttsaurus.saurus3d.common.core.gl.resource.GLDisposable;
import com.tttsaurus.saurus3d.common.core.gl.resource.GLResourceManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class EBO extends GLDisposable
{
    private BufferID eboID = null;
    private int eboSize;
    private int indicesLength;

    public int getEboSize() { return eboSize; }
    public int getIndicesLength() { return indicesLength; }

    private boolean autoUnbind = false;
    public boolean isAutoUnbind() { return autoUnbind; }
    public void setAutoUnbind(boolean autoUnbind) { this.autoUnbind = autoUnbind; }

    private boolean autoBind = true;
    public boolean isAutoBind() { return autoBind; }
    public void setAutoBind(boolean autoBind) { this.autoBind = autoBind; }

    int prevEbo = 0;
    public void storePrevEbo()
    {
        prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
    }
    public void restorePrevEbo()
    {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);
    }

    public void fetchSize()
    {
        if (eboID == null)
            throw new GLIllegalBufferIDException("Must set an EBO ID first.");

        int prevEbo = 0;
        if (autoUnbind) prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);

        if (autoBind) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID.id);
        eboSize = GL15.glGetBufferParameteri(GL15.GL_ELEMENT_ARRAY_BUFFER, GL15.GL_BUFFER_SIZE);
        indicesLength = eboSize / 4;
        if (eboSize % 4 != 0)
            throw new GLIllegalStateException("Size must be a multiple of 4 because they are indices.");

        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);
    }

    //<editor-fold desc="id">
    public void setEboID(BufferID eboID)
    {
        if (this.eboID != null)
            throw new GLIllegalStateException("Cannot set EBO ID again.");
        if (eboID.type != BufferType.EBO)
            throw new GLIllegalBufferIDException("Buffer ID must be an EBO ID.");

        this.eboID = eboID;

        int prevEbo = 0;
        if (autoUnbind) prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);

        if (autoBind) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID.id);
        eboSize = GL15.glGetBufferParameteri(GL15.GL_ELEMENT_ARRAY_BUFFER, GL15.GL_BUFFER_SIZE);
        indicesLength = eboSize / 4;
        if (eboSize % 4 != 0)
            throw new GLIllegalStateException("Size must be a multiple of 4 because they are indices.");

        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);

        GLResourceManager.addDisposable(this);
    }
    public BufferID getEboID()
    {
        return eboID;
    }
    public static BufferID genEboID()
    {
        return new BufferID(GL15.glGenBuffers(), BufferType.EBO);
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
        if (eboID == null)
            throw new GLIllegalBufferIDException("Must set an EBO ID first.");
        if (byteBuffer.remaining() % 4 != 0)
            throw new GLIllegalStateException("Size, which is byteBuffer.remaining(), must be a multiple of 4 because they are indices.");

        int prevEbo = 0;
        if (autoUnbind) prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID.id);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, byteBuffer, GL15.GL_STATIC_DRAW);
        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);

        eboSize = byteBuffer.remaining();
        indicesLength = eboSize / 4;
    }
    public void allocNewGpuMem(int size, BufferUploadHint hint)
    {
        if (eboID == null)
            throw new GLIllegalBufferIDException("Must set an EBO ID first.");
        if (size < 0)
            throw new GLIllegalStateException("Cannot have negative size.");
        if (size % 4 != 0)
            throw new GLIllegalStateException("Size must be a multiple of 4 because they are indices.");

        int prevEbo = 0;
        if (autoUnbind) prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID.id);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, size, hint.glValue);
        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);

        eboSize = size;
        indicesLength = eboSize / 4;
    }
    public void uploadBySubData(int offset, ByteBuffer byteBuffer)
    {
        if (eboID == null)
            throw new GLIllegalBufferIDException("Must set an EBO ID first.");
        if (offset < 0)
            throw new GLOverflowException("Cannot have negative offset.");
        if (offset + byteBuffer.remaining() > eboSize)
            throw new GLOverflowException("Allocated EBO size must be greater than or equal to offset + byteBuffer.remaining().");

        int prevEbo = 0;
        if (autoUnbind) prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID.id);
        GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, offset, byteBuffer);
        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);
    }

    public void uploadByMappedBuffer(int mappingOffset, int mappingSize, int offset, ByteBuffer byteBuffer)
    {
        uploadByMappedBuffer(mappingOffset, mappingSize, offset, byteBuffer, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.INVALIDATE_RANGE_BIT);
    }
    public void uploadByMappedBuffer(int mappingOffset, int mappingSize, int offset, ByteBuffer byteBuffer, MapBufferAccessBit... accessBits)
    {
        if (eboID == null)
            throw new GLIllegalBufferIDException("Must set an EBO ID first.");
        if (mappingSize < 0)
            throw new GLIllegalStateException("Cannot have negative size.");
        if (mappingOffset < 0 || offset < 0)
            throw new GLOverflowException("Cannot have negative offset.");
        if (mappingOffset + mappingSize > eboSize)
            throw new GLOverflowException("Allocated EBO size must be greater than or equal to mappingOffset + mappingSize.");
        if (offset + byteBuffer.remaining() > mappingSize)
            throw new GLOverflowException("Parameter mappingSize must be greater than or equal to offset + byteBuffer.remaining().");

        int prevEbo = 0;
        if (autoUnbind) prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);

        if (autoBind) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID.id);

        int access = 0;
        for (MapBufferAccessBit bit: accessBits)
            access |= bit.glValue;

        ByteBuffer mappedBuffer = GL30.glMapBufferRange(
                GL15.GL_ELEMENT_ARRAY_BUFFER,
                mappingOffset,
                mappingSize,
                access,
                null);

        if (mappedBuffer != null)
        {
            mappedBuffer.position(offset);
            mappedBuffer.put(byteBuffer);
            boolean success = GL15.glUnmapBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER);
            if (!success) throw new GLIllegalStateException("Buffer unmap failed, data may be corrupted.");
        }
        else
            throw new GLMapBufferException("Failed to map buffer.");

        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);
    }
    //</editor-fold>

    @Override
    public void dispose()
    {
        if (eboID != null)
            if (GL15.glIsBuffer(eboID.id))
                GL15.glDeleteBuffers(eboID.id);
    }
}
