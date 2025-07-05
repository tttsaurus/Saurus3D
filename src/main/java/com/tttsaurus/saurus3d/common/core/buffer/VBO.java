package com.tttsaurus.saurus3d.common.core.buffer;

import com.tttsaurus.saurus3d.common.core.gl.exception.GLIllegalStateException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLMapBufferException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLOverflowException;
import com.tttsaurus.saurus3d.common.core.gl.exception.GLIllegalBufferIDException;
import com.tttsaurus.saurus3d.common.core.gl.resource.GLDisposable;
import com.tttsaurus.saurus3d.common.core.gl.resource.GLResourceManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VBO extends GLDisposable
{
    private BufferID vboID = null;
    private int vboSize;

    public int getVboSize() { return vboSize; }

    private boolean autoUnbind = false;
    public boolean isAutoUnbind() { return autoUnbind; }
    public void setAutoUnbind(boolean autoUnbind) { this.autoUnbind = autoUnbind; }

    private boolean autoBind = true;
    public boolean isAutoBind() { return autoBind; }
    public void setAutoBind(boolean autoBind) { this.autoBind = autoBind; }

    int prevVbo = 0;
    public void storePrevVbo()
    {
        prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
    }
    public void restorePrevVbo()
    {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);
    }

    public void fetchSize()
    {
        if (vboID == null)
            throw new GLIllegalBufferIDException("Must set a VBO ID first.");

        int prevVbo = 0;
        if (autoUnbind) prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID.id);
        vboSize = GL15.glGetBufferParameteri(GL15.GL_ARRAY_BUFFER, GL15.GL_BUFFER_SIZE);
        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);
    }

    //<editor-fold desc="id">
    public void setVboID(BufferID vboID)
    {
        if (this.vboID != null)
            throw new GLIllegalStateException("Cannot set VBO ID again.");
        if (vboID.type != BufferType.VBO)
            throw new GLIllegalBufferIDException("Buffer ID must be a VBO ID.");

        this.vboID = vboID;

        int prevVbo = 0;
        if (autoUnbind) prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID.id);
        vboSize = GL15.glGetBufferParameteri(GL15.GL_ARRAY_BUFFER, GL15.GL_BUFFER_SIZE);
        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);

        GLResourceManager.addDisposable(this);
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
    public void directUpload(float[] arr)
    {
        ByteBuffer byteBuffer = ByteBuffer
                .allocateDirect(arr.length * Float.BYTES)
                .order(ByteOrder.nativeOrder());

        FloatBuffer floatView = byteBuffer.asFloatBuffer();
        floatView.put(arr);

        byteBuffer.limit(floatView.limit() * Float.BYTES);
        byteBuffer.position(0);

        directUpload(byteBuffer);
    }
    public void directUpload(ByteBuffer byteBuffer)
    {
        if (vboID == null)
            throw new GLIllegalBufferIDException("Must set a VBO ID first.");

        int prevVbo = 0;
        if (autoUnbind) prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID.id);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, byteBuffer, GL15.GL_STATIC_DRAW);
        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);

        vboSize = byteBuffer.remaining();
    }
    public void allocNewGpuMem(int size, BufferUploadHint hint)
    {
        if (vboID == null)
            throw new GLIllegalBufferIDException("Must set a VBO ID first.");
        if (size < 0)
            throw new GLIllegalStateException("Cannot have negative size.");

        int prevVbo = 0;
        if (autoUnbind) prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID.id);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, size, hint.glValue);
        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);

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
        if (autoUnbind) prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID.id);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, byteBuffer);
        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);
    }

    public void uploadByMappedBuffer(int mappingOffset, int mappingSize, int offset, ByteBuffer byteBuffer)
    {
        uploadByMappedBuffer(mappingOffset, mappingSize, offset, byteBuffer, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.INVALIDATE_RANGE_BIT);
    }
    public void uploadByMappedBuffer(int mappingOffset, int mappingSize, int offset, ByteBuffer byteBuffer, MapBufferAccessBit... accessBits)
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
        if (autoUnbind) prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);

        if (autoBind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID.id);

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
            mappedBuffer.position(offset);
            mappedBuffer.put(byteBuffer);
            boolean success = GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
            if (!success) throw new GLIllegalStateException("Buffer unmap failed, data may be corrupted.");
        }
        else
            throw new GLMapBufferException("Failed to map buffer.");

        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);
    }
    //</editor-fold>

    //<editor-fold desc="persistent buffer">
    private ByteBuffer persistentMappedBuffer = null;

    public void allocPersistentStorage(int size)
    {
        allocPersistentStorage(size, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
    }
    public void allocPersistentStorage(int size, MapBufferAccessBit... accessBits)
    {
        if (vboID == null)
            throw new GLIllegalBufferIDException("Must set a VBO ID first.");
        if (size < 0)
            throw new GLIllegalStateException("Cannot have negative size.");

        int prevVbo = 0;
        if (autoUnbind) prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID.id);

        int access = 0;
        for (MapBufferAccessBit bit: accessBits)
            access |= bit.glValue;

        GL45.glBufferStorage(GL15.GL_ARRAY_BUFFER, size, access);

        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);

        vboSize = size;
    }

    public void mapPersistentBuffer(int offset, int length)
    {
        mapPersistentBuffer(offset, length, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
    }
    public void mapPersistentBuffer(int offset, int length, MapBufferAccessBit... accessBits)
    {
        if (vboID == null)
            throw new GLIllegalBufferIDException("Must set a VBO ID first.");
        if (persistentMappedBuffer != null)
            throw new GLIllegalStateException("Buffer already mapped persistently.");
        if (offset < 0)
            throw new GLOverflowException("Cannot have negative offset.");
        if (offset + length > vboSize)
            throw new GLOverflowException("Allocated VBO size must be greater than or equal to offset + length.");

        int prevVbo = 0;
        if (autoUnbind) prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID.id);

        int access = 0;
        for (MapBufferAccessBit bit: accessBits)
            access |= bit.glValue;

        persistentMappedBuffer = GL45.glMapBufferRange(GL15.GL_ARRAY_BUFFER, offset, length, access, persistentMappedBuffer);

        if (persistentMappedBuffer == null)
            throw new GLMapBufferException("Failed to map persistent buffer.");

        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);
    }

    public void unmapPersistentBuffer()
    {
        if (persistentMappedBuffer == null)
            throw new GLIllegalStateException("Buffer not persistently mapped.");

        int prevVbo = 0;
        if (autoUnbind) prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        if (autoBind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID.id);

        boolean success = GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
        if (!success) throw new GLIllegalStateException("Persistent buffer unmap failed, data may be corrupted.");

        persistentMappedBuffer = null;

        if (autoUnbind) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);
    }

    public void writePersistent(int offset, ByteBuffer byteBuffer)
    {
        if (persistentMappedBuffer == null)
            throw new GLIllegalStateException("Buffer not persistently mapped.");
        if (offset < 0)
            throw new GLOverflowException("Cannot have negative offset.");
        if (offset + byteBuffer.remaining() > persistentMappedBuffer.remaining())
            throw new GLOverflowException("persistentMappedBuffer.remaining() must be greater than or equal to offset + byteBuffer.remaining().");

        int pos = persistentMappedBuffer.position();
        persistentMappedBuffer.position(offset);
        persistentMappedBuffer.put(byteBuffer);
        persistentMappedBuffer.position(pos);
    }

    public ByteBuffer getPersistentMappedBuffer()
    {
        return persistentMappedBuffer;
    }
    //</editor-fold>

    @Override
    public void dispose()
    {
        if (vboID != null)
            if (GL15.glIsBuffer(vboID.id))
                GL15.glDeleteBuffers(vboID.id);
    }
}
