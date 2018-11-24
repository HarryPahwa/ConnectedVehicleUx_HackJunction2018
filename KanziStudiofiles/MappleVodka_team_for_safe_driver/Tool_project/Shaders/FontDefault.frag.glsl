uniform sampler2D ContentTexture;
varying mediump vec2 vTexCoord;
uniform lowp vec4 FontColor;
uniform lowp float BlendIntensity;

void main()
{
    precision lowp float;

    float a = texture2D(ContentTexture, vTexCoord).a;
    float alpha = FontColor.a * a * BlendIntensity;

    gl_FragColor = vec4(FontColor.rgb * alpha, alpha);
}
