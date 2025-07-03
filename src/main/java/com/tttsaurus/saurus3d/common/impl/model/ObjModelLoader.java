package com.tttsaurus.saurus3d.common.impl.model;

import com.tttsaurus.saurus3d.common.core.mesh.attribute.AttributeLayout;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.Slot;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.Stride;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.Type;
import com.tttsaurus.saurus3d.common.core.buffer.EBO;
import com.tttsaurus.saurus3d.common.core.buffer.VBO;
import com.tttsaurus.saurus3d.common.core.model.IModelLoader;
import com.tttsaurus.saurus3d.common.core.mesh.Mesh;
import com.tttsaurus.saurus3d.common.core.reader.RlReaderUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjModelLoader implements IModelLoader
{
    @Override
    public Mesh load(String rl)
    {
        String raw = RlReaderUtils.read(rl, true);
        if (raw.isEmpty()) return null;

        Pattern vertexPattern = Pattern.compile("^v\\s+([-+]?[0-9]*\\.?[0-9]+)\\s+([-+]?[0-9]*\\.?[0-9]+)\\s+([-+]?[0-9]*\\.?[0-9]+)$");
        Pattern texCoordPattern = Pattern.compile("^vt\\s+([-+]?[0-9]*\\.?[0-9]+)\\s+([-+]?[0-9]*\\.?[0-9]+)(?:\\s+([-+]?[0-9]*\\.?[0-9]+))?$");
        Pattern normalPattern = Pattern.compile("^vn\\s+([-+]?[0-9]*\\.?[0-9]+)\\s+([-+]?[0-9]*\\.?[0-9]+)\\s+([-+]?[0-9]*\\.?[0-9]+)$");
        Pattern facePattern = Pattern.compile("^f\\s+(\\d+)(?:/(\\d*)?(?:/(\\d+))?)?\\s+" + "(\\d+)(?:/(\\d*)?(?:/(\\d+))?)?\\s+" + "(\\d+)(?:/(\\d*)?(?:/(\\d+))?)?$");

        List<float[]> vertices = new ArrayList<>();
        List<float[]> texCoords = new ArrayList<>();
        List<float[]> normals = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();

        String[] lines = raw.split("(?=\n)|(?<=\n)");

        for (String line : lines)
        {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            Matcher vertexMatcher = vertexPattern.matcher(line);
            Matcher texCoordMatcher = texCoordPattern.matcher(line);
            Matcher normalMatcher = normalPattern.matcher(line);
            Matcher faceMatcher = facePattern.matcher(line);

            if (vertexMatcher.find())
            {
                float x = Float.parseFloat(vertexMatcher.group(1));
                float y = Float.parseFloat(vertexMatcher.group(2));
                float z = Float.parseFloat(vertexMatcher.group(3));
                vertices.add(new float[]{x, y, z});
            }
            else if (texCoordMatcher.find())
            {
                float u = Float.parseFloat(texCoordMatcher.group(1));
                float v = Float.parseFloat(texCoordMatcher.group(2));
                float w = 0;
                if (texCoordMatcher.group(3) != null)
                    w = Float.parseFloat(texCoordMatcher.group(3));
                texCoords.add(new float[]{u, v, w});
            }
            else if (normalMatcher.find())
            {
                float nx = Float.parseFloat(normalMatcher.group(1));
                float ny = Float.parseFloat(normalMatcher.group(2));
                float nz = Float.parseFloat(normalMatcher.group(3));
                normals.add(new float[]{nx, ny, nz});
            }
            else if (faceMatcher.find())
            {
                int[] faceIndices = new int[9];

                for (int i = 0; i < 3; i++)
                {
                    int vIndex = Integer.parseInt(faceMatcher.group(1 + i * 3)) - 1;
                    String vtGroup = faceMatcher.group(2 + i * 3);
                    String vnGroup = faceMatcher.group(3 + i * 3);

                    int vtIndex = (vtGroup != null && !vtGroup.isEmpty()) ? Integer.parseInt(vtGroup) - 1 : -1;
                    int vnIndex = (vnGroup != null && !vnGroup.isEmpty()) ? Integer.parseInt(vnGroup) - 1 : -1;

                    faceIndices[i * 3] = vIndex;
                    faceIndices[i * 3 + 1] = vtIndex;
                    faceIndices[i * 3 + 2] = vnIndex;
                }

                faces.add(faceIndices);
            }
        }

        int numVertices = vertices.size();
        // 3 for position, 2 for texcoord, 3 for normal
        float[] vertexData = new float[numVertices * 8];

        float[] defaultNormal = (normals.size() == 1) ? normals.get(0) : new float[]{0.0f, 0.0f, 1.0f};

        for (int i = 0; i < numVertices; i++)
        {
            float[] vertex = vertices.get(i);
            float[] texCoord = texCoords.size() > i ? texCoords.get(i) : new float[]{0.0f, 0.0f};
            float[] normal = normals.size() > i ? normals.get(i) : defaultNormal;

            int index = i * 8;
            vertexData[index] = vertex[0];
            vertexData[index + 1] = vertex[1];
            vertexData[index + 2] = vertex[2];
            vertexData[index + 3] = texCoord[0];
            vertexData[index + 4] = texCoord[1];
            vertexData[index + 5] = normal[0];
            vertexData[index + 6] = normal[1];
            vertexData[index + 7] = normal[2];
        }

        int[] indices = new int[faces.size() * 3];
        int index = 0;
        for (int[] face : faces)
        {
            indices[index++] = face[0];
            indices[index++] = face[3];
            indices[index++] = face[6];
        }

        AttributeLayout layout = new AttributeLayout();
        layout.push(new Stride(32)
                .push(new Slot(Type.FLOAT, 3))
                .push(new Slot(Type.FLOAT, 2))
                .push(new Slot(Type.FLOAT, 3)));

        EBO ebo = new EBO();
        ebo.setAutoUnbind(true);
        ebo.setEboID(EBO.genEboID());
        ebo.directUpload(indices);

        VBO vbo = new VBO();
        vbo.setAutoUnbind(true);
        vbo.setVboID(VBO.genVboID());
        vbo.directUpload(vertexData);

        return new Mesh(layout, ebo, vbo);
    }
}
