#version 120

uniform sampler2D texture;
uniform sampler2D lightmap;

void main (void)
{
    gl_FragColor = texture2D(texture, gl_TexCoord[0].xy) * gl_Color * texture2D(lightmap, (gl_TexCoord[1].xy + vec2(8, 8)) / vec2(256, 256));
}
