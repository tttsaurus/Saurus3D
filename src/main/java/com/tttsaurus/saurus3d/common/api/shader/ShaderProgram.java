package com.tttsaurus.saurus3d.common.api.shader;

import com.tttsaurus.saurus3d.common.api.CommonBuffers;
import com.tttsaurus.saurus3d.common.api.shader.uniform.UniformField;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL33;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShaderProgram implements Comparable<ShaderProgram>
{
    private double cpuTimeMs;
    private double gpuTimeMs;
    private int prevProgramID;
    private int programID;
    private final Map<UniformField, Integer> uniformFields = new ConcurrentHashMap<>();
    private final List<Integer> shaderIDs = new ArrayList<>();
    private final List<Shader> shaders = new ArrayList<>();

    public int getProgramID() { return programID; }

    // call after setup
    public String getSetupDebugReport()
    {
        StringBuilder builder = new StringBuilder();

        builder.append("\n===Shader Program Setup Report===\n").append("Shaders:\n");
        for (Shader shader: shaders)
        {
            builder
                    .append(String.format("  - [%s] %s", shader.getShaderType().toString(), shader.getFileName())).append("\n")
                    .append("    - Validity: ").append(shader.getValidity() ? "valid" : "invalid").append("\n");
            if (!shader.getValidity())
                builder.append("    - Error Log: ").append(shader.getErrorLog()).append("\n");
        }

        builder.append("\nActive Shaders:\n");
        for (int shaderID: shaderIDs)
        {
            Shader shader = getShaderByID(shaderID);
            if (shader != null)
            {
                builder
                        .append(String.format("  - [%s] %s", shader.getShaderType().toString(), shader.getFileName())).append("\n")
                        .append("    - Shader GL ID: ").append(shader.getShaderID()).append("\n")
                        .append("    - Uniform Fields:").append((shader.getUniformFields().isEmpty() ? " (Empty)" : "")).append("\n");
                for (UniformField field: shader.getUniformFields())
                    builder.append(String.format("      - [%s] %s", field.getType(), field.getFieldName())).append("\n");
            }
        }

        builder
                .append("\nShader Program:\n")
                .append("  - Program GL ID: ").append(programID).append("\n")
                .append("  - Program Uniform Fields:").append((uniformFields.isEmpty() ? " (Empty)" : "")).append("\n");
        for (Map.Entry<UniformField, Integer> entry: uniformFields.entrySet())
            builder.append(String.format("    - [%s] %s, GL Loc: %d", entry.getKey().getType(), entry.getKey().getFieldName(), entry.getValue())).append("\n");

        builder.append("\nCPU Time Taken: ").append(cpuTimeMs).append(" ms").append("\n");
        builder.append("GPU Time Taken: ").append(gpuTimeMs).append(" ms").append("\n");

        builder.append("\n===End of the Setup Report===");

        return builder.toString();
    }

    @Nullable
    private Shader getShaderByID(int shaderID)
    {
        for (Shader shader: shaders)
            if (shaderID == shader.getShaderID())
                return shader;
        return null;
    }

    public ShaderProgram(Shader... shaders)
    {
        this.shaders.addAll(Arrays.asList(shaders));
    }

    public void setup()
    {
        int gpuTimeQueryID = GL15.glGenQueries();
        GL15.glBeginQuery(GL33.GL_TIME_ELAPSED, gpuTimeQueryID);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for (Shader shader: shaders)
        {
            shader.compile();
            if (shader.getValidity())
                shaderIDs.add(shader.getShaderID());
        }
        for (int shaderID: shaderIDs)
        {
            Shader shader = getShaderByID(shaderID);
            if (shader != null)
            {
                List<String> nameCache = new ArrayList<>();
                for (UniformField field : shader.getUniformFields())
                {
                    if (nameCache.contains(field.getFieldName())) continue;
                    nameCache.add(field.getFieldName());
                    uniformFields.put(field, -1);
                }
            }
        }

        programID = GL20.glCreateProgram();
        for (int shaderID: shaderIDs)
            GL20.glAttachShader(programID, shaderID);

        GL20.glLinkProgram(programID);

        Iterator<Map.Entry<UniformField, Integer>> iterator = uniformFields.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<UniformField, Integer> entry = iterator.next();
            int uniformLocation = GL20.glGetUniformLocation(programID, entry.getKey().getFieldName());

            if (uniformLocation != -1)
                entry.setValue(uniformLocation);
            else
                iterator.remove();
        }

        stopWatch.stop();
        cpuTimeMs = stopWatch.getTime();

        GL15.glEndQuery(GL33.GL_TIME_ELAPSED);
        GL15.glGetQueryObject(gpuTimeQueryID, GL15.GL_QUERY_RESULT, CommonBuffers.intBuffer);
        gpuTimeMs = CommonBuffers.intBuffer.get(0) / 1.0E6d;
    }

    public int getUniformLocation(String fieldName)
    {
        for (Map.Entry<UniformField, Integer> entry: uniformFields.entrySet())
            if (entry.getKey().getFieldName().equals(fieldName))
                return entry.getValue();
        return GL20.glGetUniformLocation(programID, fieldName);
    }

    public void use()
    {
        prevProgramID = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        GL20.glUseProgram(programID);
    }
    public void unuse()
    {
        GL20.glUseProgram(prevProgramID);
    }

    public void dispose()
    {
        for (int shaderID: shaderIDs)
        {
            GL20.glDetachShader(programID, shaderID);
            GL20.glDeleteShader(shaderID);
        }
        GL20.glDeleteProgram(programID);
    }

    @Override
    public int compareTo(@NotNull ShaderProgram o)
    {
        return Integer.compare(programID, o.programID);
    }
}
