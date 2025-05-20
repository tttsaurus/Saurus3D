//#version 120
//
//uniform sampler2D texture;
//uniform sampler2D lightmap;
//
//void main (void)
//{
//    gl_FragColor = texture2D(texture, gl_TexCoord[0].xy) * gl_Color * texture2D(lightmap, (gl_TexCoord[1].xy + vec2(8, 8)) / vec2(256, 256));
//}
#version 330 core

in vec2 TexCoord0;
in vec2 TexCoord1;
in vec4 Color;

out vec4 FragColor;

uniform sampler2D tex;
uniform sampler2D lightmap;

void main(void)
{
    vec4 baseColor = texture(tex, TexCoord0);
    vec4 lightColor = texture(lightmap, (TexCoord1 + vec2(8.0, 8.0)) / vec2(256.0, 256.0));
    FragColor = baseColor * Color * lightColor;
//    FragColor = baseColor * Color;
//    FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
