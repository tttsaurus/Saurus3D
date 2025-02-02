#version 120

uniform mat4 u_transform;
uniform int testArray[2];
uniform ivec2 testArray2[3];

attribute vec3 a_position;

void main() {
    gl_Position = u_transform * vec4(a_position, 1.0);
    int temp = testArray[0];
    ivec2 temp2 = testArray2[0];
}
