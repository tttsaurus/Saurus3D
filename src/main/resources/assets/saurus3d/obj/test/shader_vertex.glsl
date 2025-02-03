#version 330 core

layout (location = 0) in vec3 pos;
layout (location = 1) in vec2 texCoord;
layout (location = 2) in vec3 normal;
layout (location = 3) in vec3 offset;

uniform mat4 u_transform;

out vec2 TexCoord;
out vec3 FragNormal;

void main()
{
    vec3 worldPos = pos + offset;
    gl_Position = u_transform * vec4(worldPos, 1.0);

    TexCoord = texCoord;
    FragNormal = normal;
}
