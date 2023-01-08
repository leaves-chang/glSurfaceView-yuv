package com.ypmtop.opengl_tst1.utils;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.zip.DeflaterInputStream;

public  class GlHelper {

    private static String TAG = "GlHelper";
    private static boolean DEBUG = true;
    public static String readResFile(Context context, int ResID) {
        InputStream inputStream = context.getResources().openRawResource(ResID);
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer result = new StringBuffer("");
        String line;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            while ((line = reader.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }
    public static byte[] read(Context context, String fileName) {
        try {
            InputStream inputStream = context.getResources().getAssets().open(fileName);
            int len = inputStream.available();
            byte[] buffer = new byte[len];
            inputStream.read(buffer);
            //Log.d(TAG, "read len: " + len);
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new byte[0];
    }
    public static int compileShader(int type, String shaderCode) {
        int[] compileStatus =  new int[]{0};

        //1. create shader
        int shaderObjectId = GLES20.glCreateShader(type);
        if (shaderObjectId == 0) {
            if (DEBUG) {
                Log.w(TAG, "compileShader: Could not create new shader");
            }
            return 0;
        }
        // 2. upload source to shader
        GLES20.glShaderSource(shaderObjectId, shaderCode);

        // 3. compile shader
        GLES20.glCompileShader(shaderObjectId);

        GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (DEBUG) {
            Log.i(TAG, "compileShader: Results of compiling source:\n "+ shaderCode + "\n "
                    + GLES20.glGetShaderInfoLog(shaderObjectId));
        }
        if (compileStatus[0] == 0) {
            // If it failed, delete the shader object.
            GLES20.glDeleteShader(shaderObjectId);
            if (DEBUG) {
                Log.w(TAG, "compileShader: Compilation of shader failed");
            }
            return 0;
        }
        return shaderObjectId;
    }
    private static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        int[] linkStatus = new int[1];
        // 1. create program
        int programObjectId = GLES20.glCreateProgram();
        if (programObjectId == 0) {
            if (DEBUG) {
                Log.w(TAG, "linkProgram: Could not create new program");
            }
            return 0;
        }
        GLES20.glAttachShader(programObjectId, vertexShaderId);
        GLES20.glAttachShader(programObjectId, fragmentShaderId);
        GLES20.glLinkProgram(programObjectId);

        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (DEBUG) {
            Log.i(TAG, "linkProgram: Results of linking program:\n"
                    + GLES20.glGetProgramInfoLog(programObjectId));
        }

        if (linkStatus[0] == 0) {
            GLES20.glDeleteProgram(programObjectId);
            if (DEBUG) {
                Log.w(TAG, "linkProgram: failed");
            }
            return 0;
        }

        return programObjectId;
    }
    public static boolean validateProgram(int programObjectId) {
        int[] validateStatus = new int[1];
        GLES20.glValidateProgram(programObjectId);

        GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
        Log.i(TAG, "validateProgram: Results of validating program: " + validateStatus[0]
                + "\nLog: " + GLES20.glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }
    public static int buildProgram(String vertexShaderSrc, String fragmentShaderSrc) {

        int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSrc);
        int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSrc);
        // Link them into a shader program.
        int program = linkProgram(vertexShader, fragmentShader);
        if (DEBUG) {
            validateProgram(program);
        }
        //Log.i(TAG, "vertexShader: " + vertexShader + " fragmentShader: " + fragmentShader + " program: " + program);
        return program;
    }
}
