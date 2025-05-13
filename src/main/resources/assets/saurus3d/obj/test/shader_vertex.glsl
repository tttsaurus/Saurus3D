#version 330 core

layout (location = 0) in vec3 pos;
layout (location = 1) in vec2 texCoord;
layout (location = 2) in vec3 normal;

uniform mat4 u_transform;
uniform mat4 modelView;
uniform mat4 projection;
uniform vec3 camPos;

out vec2 TexCoord;
out vec3 FragNormal;

void main()
{
    gl_Position = projection * modelView * u_transform * vec4(pos - camPos + vec3(0, 90, 0), 1.0);

    //gl_Position = vec4(pos, 1.0);

    TexCoord = texCoord;
    FragNormal = normal;
}
