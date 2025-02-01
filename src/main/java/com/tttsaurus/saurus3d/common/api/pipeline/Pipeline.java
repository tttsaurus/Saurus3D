package com.tttsaurus.saurus3d.common.api.pipeline;

import com.google.common.collect.TreeMultimap;
import com.tttsaurus.saurus3d.common.api.shader.ShaderProgram;
import java.util.*;

public class Pipeline
{
    private TreeMultimap<Integer, ShaderProgram> shaderPrograms = TreeMultimap.create(
            Comparator.<Integer>reverseOrder(),
            Comparator.<ShaderProgram>naturalOrder());

    public void addShaderProgram(int priority, ShaderProgram shaderProgram)
    {
        shaderPrograms.put(priority, shaderProgram);
    }
}
