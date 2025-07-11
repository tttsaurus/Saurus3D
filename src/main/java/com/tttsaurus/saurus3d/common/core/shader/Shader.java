package com.tttsaurus.saurus3d.common.core.shader;

import com.tttsaurus.saurus3d.common.core.gl.resource.GLResourceManager;
import com.tttsaurus.saurus3d.common.core.gl.resource.GLDisposable;
import com.tttsaurus.saurus3d.common.core.shader.uniform.UniformField;
import com.tttsaurus.saurus3d.common.core.shader.uniform.UniformParseUtils;
import org.lwjgl.opengl.*;
import java.util.List;

public class Shader extends GLDisposable
{
    public enum ShaderType
    {
        VERTEX(GL20.GL_VERTEX_SHADER),
        FRAGMENT(GL20.GL_FRAGMENT_SHADER),
        GEOMETRY(GL32.GL_GEOMETRY_SHADER),
        TESS_CONTROL(GL40.GL_TESS_CONTROL_SHADER),
        TESS_EVALUATION(GL40.GL_TESS_EVALUATION_SHADER),
        COMPUTE(GL43.GL_COMPUTE_SHADER);

        public final int glValue;
        ShaderType(int glValue)
        {
            this.glValue = glValue;
        }
    }

    private String shaderSource;

    private boolean setup = false;
    private int shaderID;
    private final ShaderType shaderType;
    private boolean valid = true;
    private String errorLog;
    private final List<UniformField> uniformFields;
    private final String fileName;

    public boolean getSetup() { return setup; }
    public int getShaderID() { return shaderID; }
    public ShaderType getShaderType() { return shaderType; }
    public String getFileName() { return fileName; }
    protected boolean getValidity() { return valid; }
    protected String getErrorLog() { return errorLog; }
    protected List<UniformField> getUniformFields() { return uniformFields; }

    protected Shader(String fileName, String shaderSource, ShaderType shaderType)
    {
        this.fileName = fileName;
        this.shaderSource = shaderSource;
        this.shaderType = shaderType;

        uniformFields = UniformParseUtils.getUniformFields(shaderSource);
    }

    protected void compile()
    {
        if (setup) return;

        shaderID = GL20.glCreateShader(shaderType.glValue);

        GL20.glShaderSource(shaderID, shaderSource);

        GL20.glCompileShader(shaderID);

        shaderSource = null;

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
        {
            errorLog = GL20.glGetShaderInfoLog(shaderID, 1024);
            GL20.glDeleteShader(shaderID);
            shaderID = 0;
            valid = false;
        }

        setup = true;
        if (shaderID != 0)
            GLResourceManager.addDisposable(this);
    }

    @Override
    public int disposePriority()
    {
        return 900;
    }

    @Override
    public void dispose()
    {
        if (shaderID != 0)
            GL20.glDeleteShader(shaderID);
        setup = false;
    }
}
