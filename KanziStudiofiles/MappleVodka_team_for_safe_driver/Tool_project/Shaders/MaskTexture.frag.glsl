uniform sampler2D Texture;
uniform lowp float BlendIntensity;
uniform sampler2D MaskTexture;
varying mediump vec2 vTexCoord;

uniform mediump vec2 TextureOffset;
uniform mediump vec2 TextureTiling;
uniform mediump vec2 MaskTextureOffset;
uniform mediump vec2 MaskTextureTiling;


void main()
{
    precision lowp float;

    vec4 color = texture2D(Texture, vTexCoord*TextureTiling + TextureOffset);
    float mask = texture2D(MaskTexture, vTexCoord*MaskTextureTiling+MaskTextureOffset).r;
    
    gl_FragColor.rgba = color.rgba * BlendIntensity * mask;
}
