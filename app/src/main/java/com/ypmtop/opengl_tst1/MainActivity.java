package com.ypmtop.opengl_tst1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private GLSurfaceView mGlSurfaceView;
    private GlRender mGlRender = null;
    private static Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        mGlSurfaceView = findViewById(R.id.gl_surface_view);
        mGlSurfaceView.setEGLContextClientVersion(2);
        mGlRender = new GlRender(context);
        mGlSurfaceView.setRenderer(mGlRender);


    }
}