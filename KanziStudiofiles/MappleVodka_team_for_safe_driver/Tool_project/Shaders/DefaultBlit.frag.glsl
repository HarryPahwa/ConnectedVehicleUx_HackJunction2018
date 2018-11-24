uniform sampler2D Texture0;

varying mediump vec2 vTexCoord;

void main()
{
    precision lowp float;
    
    gl_FragColor = texture2D(Texture0, vTexCoord);
}