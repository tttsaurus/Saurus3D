package com.tttsaurus.saurus3d.common.core.mesh;

import com.tttsaurus.saurus3d.common.core.gl.exception.GLIllegalStateException;
import com.tttsaurus.saurus3d.common.core.gl.resource.GLResourceManager;
import com.tttsaurus.saurus3d.common.core.gl.resource.GLDisposable;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.AttributeLayout;
import com.tttsaurus.saurus3d.common.core.buffer.EBO;
import com.tttsaurus.saurus3d.common.core.buffer.VBO;
import org.lwjgl.opengl.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mesh extends GLDisposable
{
    private boolean setup = false;

    private final AttributeLayout attributeLayout;
    private final EBO ebo;
    private final List<VBO> vbos = new ArrayList<>();

    private int vaoID;
    private int eboIndexOffset = 0;

    private boolean instancing;
    private int instancePrimCount;

    public void setInstancing(boolean flag) { instancing = flag; }
    public void setInstancePrimCount(int count) { instancePrimCount = count; }

    public boolean getSetup() { return setup; }
    public int getVaoID() { return vaoID; }

    protected int getEboIndexOffset() { return eboIndexOffset; }
    protected void setEboIndexOffset(int offset) { eboIndexOffset = offset; }

    public EBO getEbo() { return ebo; }
    public List<VBO> getVbos() { return vbos; }

    public Mesh(AttributeLayout attributeLayout, EBO ebo, VBO... vbos)
    {
        if (vbos.length != attributeLayout.getStrideCount())
            throw new GLIllegalStateException("Number of VBOs must match the number of strides in the attribute layout.");
        if (ebo.getEboID() == null)
            throw new GLIllegalStateException("EBO must have an ID first.");
        for (VBO vbo: vbos)
            if (vbo.getVboID() == null)
                throw new GLIllegalStateException("Each VBO must have an ID first.");

        this.attributeLayout = attributeLayout;
        this.ebo = ebo;
        this.vbos.addAll(Arrays.asList(vbos));
    }

    public void setup()
    {
        if (setup) return;

        int prevVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);

        vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);

        attributeLayout.uploadToGL(vbos.toArray(new VBO[0]));
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo.getEboID().id);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);
        GL30.glBindVertexArray(prevVao);

        setup = true;
        GLResourceManager.addDisposable(this);
    }

    public void render()
    {
        if (!setup)
            throw new GLIllegalStateException("Mesh must be set up first.");

        int prevVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);

        GL30.glBindVertexArray(vaoID);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo.getEboID().id);

        if (instancing)
            GL31.glDrawElementsInstanced(GL11.GL_TRIANGLES, ebo.getIndicesLength(), GL11.GL_UNSIGNED_INT, (long) eboIndexOffset * Integer.BYTES, instancePrimCount);
        else
            GL11.glDrawElements(GL11.GL_TRIANGLES, ebo.getIndicesLength(), GL11.GL_UNSIGNED_INT, (long) eboIndexOffset * Integer.BYTES);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);
        GL30.glBindVertexArray(prevVao);
    }

    @Override
    public void dispose()
    {
        GL30.glDeleteVertexArrays(vaoID);
        setup = false;
    }
}

