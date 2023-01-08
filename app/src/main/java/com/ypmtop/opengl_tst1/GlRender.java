package com.ypmtop.opengl_tst1;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;



import com.ypmtop.opengl_tst1.utils.GlHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GlRender implements GLSurfaceView.Renderer {
    private final String TAG = "GlRender";

    private final int YUV_TYPE = 1;
    private static Context mContext;
    private int mProgram = -1;
    private int mVertexPosMap = -1;
    private int mTextureDataPosMap = -1;
    private int mTextureDataYMap = -1;
    private int mTextureDataUMap = -1;
    private int mTextureDataVMap = -1;
    private int mTextureDataUVMap = -1;
    private int mMatrixMap = -1;
    private int mType = -1;

    private final int mImageWidth = 480;
    private final int mImageHeight = 800;

    //顶点坐标buffer
    private FloatBuffer vertexBuffer;
    //纹理数据buffer
    private FloatBuffer textureBuffer;

    private byte[] mImageBytes;
    private int[] textureObjIds = null;
    private ByteBuffer mYbuffer;
    private ByteBuffer mUbuffer;
    private ByteBuffer mVbuffer;
    private ByteBuffer mUVbuffer;

    private float[] mMatrixBuffer = new float[16];

    static float vertexPos[] = {
            -1.0f, -1.0f,
             1.0f, -1.0f,
            -1.0f,  1.0f,
             1.0f,  1.0f,
    };
    static float texturePos[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    };

    //构造函数
    public GlRender(Context ctx) {
        mContext = ctx;
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        mProgram = GlHelper.buildProgram(
                GlHelper.readResFile(mContext, R.raw.vertex_shader),
                GlHelper.readResFile(mContext, R.raw.fragment_shader)
        );

        mVertexPosMap = GLES20.glGetAttribLocation(mProgram, "mVertexPos");
        mTextureDataPosMap = GLES20.glGetAttribLocation(mProgram, "aTextureData");

        mTextureDataYMap = GLES20.glGetUniformLocation(mProgram, "yTexture");
        mTextureDataUMap = GLES20.glGetUniformLocation(mProgram, "uTexture");
        mTextureDataVMap = GLES20.glGetUniformLocation(mProgram, "vTexture");
        mTextureDataUVMap = GLES20.glGetUniformLocation(mProgram, "uvTexture");
        mMatrixMap = GLES20.glGetUniformLocation(mProgram, "matrix");
        mType = GLES20.glGetUniformLocation(mProgram, "type");


        vertexBuffer = ByteBuffer.allocateDirect(vertexPos.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureBuffer= ByteBuffer.allocateDirect(texturePos.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        vertexBuffer.put(vertexPos);
        textureBuffer.put(texturePos);

        if (YUV_TYPE == 1) {
            mImageBytes = GlHelper.read(mContext, "test_nv21.yuv");
            Log.i(TAG, "image size:"+ mImageBytes.length);
            mYbuffer = ByteBuffer.allocateDirect(mImageWidth*mImageHeight).order(ByteOrder.nativeOrder());
            mYbuffer.put(mImageBytes, 0, mImageWidth*mImageHeight);
            mYbuffer.position(0);

            mUVbuffer = ByteBuffer.allocateDirect(mImageWidth * mImageHeight / 2).order(ByteOrder.nativeOrder());
            mUVbuffer.put(mImageBytes, mImageWidth * mImageHeight, mImageWidth * mImageHeight / 2);
            mUVbuffer.position(0);

            textureObjIds = new int[2];
            GLES20.glGenTextures(textureObjIds.length, textureObjIds, 0);
            textureLuminance(mYbuffer, mImageWidth, mImageHeight, textureObjIds[0]);
            textureLuminanceAlpha(mUVbuffer, mImageWidth/2, mImageHeight/2, textureObjIds[1]);

        } else {
            mImageBytes = GlHelper.read(mContext, "test_yuv420sp.yuv");
            Log.i(TAG, "image size:"+ mImageBytes.length);
            mYbuffer = ByteBuffer.allocateDirect(mImageWidth*mImageHeight).order(ByteOrder.nativeOrder());
            mYbuffer.put(mImageBytes, 0, mImageWidth*mImageHeight);
            mYbuffer.position(0);

            mUbuffer = ByteBuffer.allocateDirect(mImageWidth * mImageHeight / 4).order(ByteOrder.nativeOrder());
            mUbuffer.put(mImageBytes, mImageWidth * mImageHeight, mImageWidth * mImageHeight / 4);
            mUbuffer.position(0);

            mVbuffer = ByteBuffer.allocateDirect(mImageWidth * mImageHeight / 4).order(ByteOrder.nativeOrder());
            mVbuffer.put(mImageBytes, mImageWidth * mImageHeight * 5 / 4, mImageWidth * mImageHeight / 4);
            mVbuffer.position(0);

            textureObjIds = new int[3];
            GLES20.glGenTextures(textureObjIds.length, textureObjIds, 0);
            textureLuminance(mYbuffer, mImageWidth, mImageHeight, textureObjIds[0]);
            textureLuminance(mUbuffer, mImageWidth/2, mImageHeight/2, textureObjIds[1]);
            textureLuminance(mVbuffer, mImageWidth/2, mImageHeight/2, textureObjIds[2]);
        }


    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        Matrix.setIdentityM(mMatrixBuffer, 0);
        float sx = 1f * mImageWidth / width;
        float sy = 1f * mImageHeight / height;
        Matrix.scaleM(mMatrixBuffer, 0, sx, sy, 1f);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glUseProgram(mProgram);
        vertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(mVertexPosMap);
        GLES20.glVertexAttribPointer(mVertexPosMap, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        textureBuffer.position(0);
        GLES20.glEnableVertexAttribArray(mTextureDataPosMap);
        GLES20.glVertexAttribPointer(mTextureDataPosMap, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);

        GLES20.glUniform1i(mType, YUV_TYPE);

        GLES20.glUniformMatrix4fv(mMatrixMap, 1, false, mMatrixBuffer, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjIds[0]);
        GLES20.glUniform1i(mTextureDataYMap, 0);

        if (YUV_TYPE == 1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjIds[1]);
            GLES20.glUniform1i(mTextureDataUVMap, 1);
        } else {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjIds[1]);
            GLES20.glUniform1i(mTextureDataUMap, 1);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjIds[2]);
            GLES20.glUniform1i(mTextureDataVMap, 2);
        }


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mVertexPosMap);
        GLES20.glDisableVertexAttribArray(mTextureDataPosMap);
    }

    private void textureLuminance(ByteBuffer imageData, int width, int height, int textureId) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0,
                GLES20.GL_LUMINANCE, width, height, 0,
                GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE, imageData
        );
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }
    private void textureLuminanceAlpha(ByteBuffer imageData, int width, int height, int textureId) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0,
                GLES20.GL_LUMINANCE_ALPHA, width, height, 0,
                GLES20.GL_LUMINANCE_ALPHA,
                GLES20.GL_UNSIGNED_BYTE, imageData
        );
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }
}
