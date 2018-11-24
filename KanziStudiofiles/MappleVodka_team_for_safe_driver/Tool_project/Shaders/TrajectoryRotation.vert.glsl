attribute vec3 kzPosition;
attribute vec2 kzTextureCoordinate0;
varying mediump vec2 vTexCoord;
uniform highp mat4 kzProjectionCameraWorldMatrix;
uniform mediump float kzTime;
uniform mediump float VisibleAmountInParent;
 
struct Transform
{
  vec4 position;
  vec4 axis_angle;
};
Transform T;
 
 
vec4 quat_from_axis_angle(vec3 axis, float angle)
{ 
  vec4 qr;
  float half_angle = (angle * 0.5) * 3.14159 / 180.0;
  qr.x = axis.x * sin(half_angle);
  qr.y = axis.y * sin(half_angle);
  qr.z = axis.z * sin(half_angle);
  qr.w = cos(half_angle);
  return qr;
}
 
vec3 rotate_vertex_position(vec3 position, vec3 axis, float angle)
{ 
  vec4 q = quat_from_axis_angle(axis, angle);
  vec3 v = position.xyz;
  return v + 2.0 * cross(q.xyz, cross(q.xyz, v) + q.w * v);
}


float trajectoryAngle(float x){
    float temp = sin(VisibleAmountInParent*3.14159);

    if(x>0.0)
        return temp* (-90.0);
    else 
        return temp*90.0;
    

}

void main()
{
  T.position = vec4(kzPosition, 1.0);
  
  vec4 myPos = kzProjectionCameraWorldMatrix * vec4(kzPosition, 1);
  
  //Define here over which axis you want the rotation to appear (x,y,z) and last the angle.
  T.axis_angle = vec4(0.0, 1.0, 0.0, trajectoryAngle(myPos.x));  
  
  vec3 P = rotate_vertex_position(kzPosition, T.axis_angle.xyz, T.axis_angle.w);
  //P += T.position.xyz;
  //P.y+=0.5*VisibleAmountInParent;
  //P.z+=1.5*VisibleAmountInParent;
  gl_Position = kzProjectionCameraWorldMatrix * vec4(P, 1.0);
  vTexCoord = kzTextureCoordinate0;
}