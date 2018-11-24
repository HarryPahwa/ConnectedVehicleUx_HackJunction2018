varying lowp vec4 vColor;
uniform lowp float BlendIntensity;

void main()
{
    precision lowp float;
    
    gl_FragColor.rgba = vColor*BlendIntensity;
}
