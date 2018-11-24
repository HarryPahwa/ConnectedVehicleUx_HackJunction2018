uniform sampler2D Texture;
uniform lowp float BlendIntensity;
varying mediump vec2 vTexCoord;

uniform mediump float VisibleAmountInParent;

void main()
{
    precision lowp float;

    vec4 color = texture2D(Texture, vTexCoord);
    float visibility = clamp(VisibleAmountInParent*3.0, 0.0, 1.0);   
    
    gl_FragColor.rgba = color*visibility;

}
