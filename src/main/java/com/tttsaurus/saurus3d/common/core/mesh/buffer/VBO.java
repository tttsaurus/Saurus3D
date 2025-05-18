package com.tttsaurus.saurus3d.common.core.mesh.buffer;

import com.tttsaurus.saurus3d.common.core.gl.exception.GLIllegalStateException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLMappedBufferException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLOverflowException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLIllegalBufferIDException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import java.nio.ByteBuffer;

public class VBO
{
    private BufferID vboID = null;
    private int vboSize;

    private boolean autoRebindToOldVbo = false;
    public boolean isAutoRebindToOldVbo() { return autoRebindToOldVbo; }
    public void setAutoRebindToOldVbo(boolean autoRebindToOldVbo) { this.autoRebindToOldVbo = autoRebindToOldVbo; }

    int prevVbo = 0;
    public void storePrevVbo()
    {
        prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
    }
    public void restorePrevVbo()
    {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);
    }

    //<editor-fold desc="id">
    public void setVboID(BufferID vboID)
    {
        if (vboID.type != BufferType.VBO)
            throw new GLIllegalBufferIDException("Buffer ID must be a VBO ID.");

        this.vboID = vboID;

        int prevVbo = 0;
        if (autoRebindToOldVbo) prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID.id);
        vboSize = GL15.glGetBufferParameteri(vboID.id, GL15.GL_BUFFER_SIZE);
        if (autoRebindToOldVbo) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);
    }
    public BufferID getVboID()
    {
        return vboID;
    }
    public static BufferID genVboID()
    {
        return new BufferID(GL15.glGenBuffers(), BufferType.VBO);
    }
    //</editor-fold>

    //<editor-fold desc="upload">
    public void directUpload(ByteBuffer byteBuffer)
    {
        if (vboID == null)
            throw new GLIllegalBufferIDException("Must set a VBO ID first.");

        int prevVbo = 0;
        if (autoRebindToOldVbo) prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID.id);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, byteBuffer, GL15.GL_STATIC_DRAW);
        if (autoRebindToOldVbo) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);

        vboSize = byteBuffer.remaining();
    }
    public void allocNewGpuMem(int size, BufferUploadHint hint)
    {
        if (vboID == null)
            throw new GLIllegalBufferIDException("Must set a VBO ID first.");
        if (size < 0)
            throw new GLIllegalStateException("Cannot have negative size.");

        int prevVbo = 0;
        if (autoRebindToOldVbo) prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID.id);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, size, hint.glValue);
        if (autoRebindToOldVbo) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);

        vboSize = size;
    }
    public void uploadBySubData(int offset, ByteBuffer byteBuffer)
    {
        if (vboID == null)
            throw new GLIllegalBufferIDException("Must set a VBO ID first.");
        if (offset < 0)
            throw new GLOverflowException("Cannot have negative offset.");
        if (offset + byteBuffer.remaining() > vboSize)
            throw new GLOverflowException("Allocated VBO size must be greater than or equal to offset + byteBuffer.remaining().");

        int prevVbo = 0;
        if (autoRebindToOldVbo) prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID.id);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, byteBuffer);
        if (autoRebindToOldVbo) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);
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
        if (vboID == null)
            throw new GLIllegalBufferIDException("Must set a VBO ID first.");
        if (mappingSize < 0)
            throw new GLIllegalStateException("Cannot have negative size.");
        if (mappingOffset < 0 || offset < 0)
            throw new GLOverflowException("Cannot have negative offset.");
        if (mappingOffset + mappingSize > vboSize)
            throw new GLOverflowException("Allocated VBO size must be greater than or equal to mappingOffset + mappingSize.");
        if (offset + byteBuffer.remaining() > mappingSize)
            throw new GLOverflowException("Parameter mappingSize must be greater than or equal to offset + byteBuffer.remaining().");

        int prevVbo = 0;
        if (autoRebindToOldVbo) prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID.id);

        int access = 0;
        for (MapBufferAccessBit bit: accessBits)
            access |= bit.glValue;

        ByteBuffer mappedBuffer = GL30.glMapBufferRange(
                GL15.GL_ARRAY_BUFFER,
                mappingOffset,
                mappingSize,
                access,
                null);

        if (mappedBuffer != null)
        {
            mappedBuffer.position(offset).put(byteBuffer);
            if (unmap) GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
        }
        else
            throw new GLMappedBufferException("Failed to map buffer.");

        if (autoRebindToOldVbo) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);
    }
    //</editor-fold>
}
