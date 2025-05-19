package com.tttsaurus.saurus3d.common.core.mesh.buffer;

import com.tttsaurus.saurus3d.common.core.gl.exception.GLIllegalBufferIDException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLIllegalStateException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLMappedBufferException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLOverflowException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import java.nio.ByteBuffer;

public class EBO
{
    private BufferID eboID = null;
    private int eboSize;

    private boolean autoRebindToOldEbo = false;
    public boolean isAutoRebindToOldEbo() { return autoRebindToOldEbo; }
    public void setAutoRebindToOldEbo(boolean autoRebindToOldEbo) { this.autoRebindToOldEbo = autoRebindToOldEbo; }

    int prevEbo = 0;
    public void storePrevEbo()
    {
        prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
    }
    public void restorePrevEbo()
    {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);
    }

    //<editor-fold desc="id">
    public void setEboID(BufferID eboID)
    {
        if (eboID.type != BufferType.EBO)
            throw new GLIllegalBufferIDException("Buffer ID must be an EBO ID.");

        this.eboID = eboID;

        int prevEbo = 0;
        if (autoRebindToOldEbo) prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID.id);
        eboSize = GL15.glGetBufferParameteri(eboID.id, GL15.GL_BUFFER_SIZE);
        if (autoRebindToOldEbo) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);
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
    public void directUpload(ByteBuffer byteBuffer)
    {
        if (eboID == null)
            throw new GLIllegalBufferIDException("Must set an EBO ID first.");

        int prevEbo = 0;
        if (autoRebindToOldEbo) prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID.id);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, byteBuffer, GL15.GL_STATIC_DRAW);
        if (autoRebindToOldEbo) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);

        eboSize = byteBuffer.remaining();
    }
    public void allocNewGpuMem(int size, BufferUploadHint hint)
    {
        if (eboID == null)
            throw new GLIllegalBufferIDException("Must set an EBO ID first.");
        if (size < 0)
            throw new GLIllegalStateException("Cannot have negative size.");

        int prevEbo = 0;
        if (autoRebindToOldEbo) prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID.id);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, size, hint.glValue);
        if (autoRebindToOldEbo) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);

        eboSize = size;
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
        if (autoRebindToOldEbo) prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID.id);
        GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, offset, byteBuffer);
        if (autoRebindToOldEbo) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);
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
        if (autoRebindToOldEbo) prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID.id);

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
            mappedBuffer.position(offset).put(byteBuffer);
            if (unmap) GL15.glUnmapBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER);
        }
        else
            throw new GLMappedBufferException("Failed to map buffer.");

        if (autoRebindToOldEbo) GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);
    }
    //</editor-fold>
}
