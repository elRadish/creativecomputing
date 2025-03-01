uniform sampler2D velocity;
uniform sampler2D advected;
uniform sampler2D bounds;

uniform vec2 gridSize;
uniform float gridScale;

uniform float timestep;
uniform float dissipation;

vec3 bilerp(sampler2D d, vec2 p)
{
    vec4 ij; // i0, j0, i1, j1
    ij.xy = floor(p - 0.5) + 0.5;
    ij.zw = ij.xy + 1.0;

    vec4 uv = ij / gridSize.xyxy;
    vec3 d11 = texture2D(d, uv.xy).xyz;
    vec3 d21 = texture2D(d, uv.zy).xyz;
    vec3 d12 = texture2D(d, uv.xw).xyz;
    vec3 d22 = texture2D(d, uv.zw).xyz;

    vec2 a = p - ij.xy;

    return mix(mix(d11, d21, a.x), mix(d12, d22, a.x), a.y);
}

void main()
{
    vec2 uv = gl_FragCoord.xy / gridSize.xy;
    
    float bound = texture2D(bounds, uv).x;
    if (bound > 0.0) {
        gl_FragColor = vec4(0.0);
        return;
    }
    
    float scale = 1.0 / gridScale;

    // trace point back in time
    vec2 p = gl_FragCoord.xy - timestep * scale * texture2D(velocity, uv).xy;

    gl_FragColor = vec4(dissipation * bilerp(advected, p), 1.0);
}
