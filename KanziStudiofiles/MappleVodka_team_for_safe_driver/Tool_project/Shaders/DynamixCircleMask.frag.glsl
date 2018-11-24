precision mediump float;    

varying mediump vec2 vTexCoord;
uniform sampler2D Texture;

uniform mediump float sphereSize;

uniform mediump vec2 TextureOffset;
uniform mediump vec2 TextureScale;

uniform mediump float BlendIntensity;

float circle()
{
    float dist = length(vTexCoord*2.0-1.0)*sphereSize;
    
    if(dist<0.5)
        return 1.0;
    else
        return 0.0;
}


void main()
{
    
    
    float mask = circle();    
    vec4 text = texture2D(Texture, vTexCoord*TextureScale+TextureOffset);
    
    gl_FragColor = mask*text*BlendIntensity;
}