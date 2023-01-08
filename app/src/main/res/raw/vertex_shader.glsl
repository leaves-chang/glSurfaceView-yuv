attribute vec4 mVertexPos;
attribute vec2 aTextureData;

uniform mat4 matrix;

varying vec2 vTextureData;

void main() {
    vTextureData = aTextureData;
    gl_Position = matrix * mVertexPos;
}