/**
 * PageCurlRS.java
 * Copyright (c) 2011 daoki2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.android.example.curl;

import com.android.example.curl.R;
import com.android.example.curl.ScriptC_pagecurl;
import com.android.example.curl.ScriptField_Bitmaps;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.Allocation;
import android.renderscript.Matrix4f;
import android.renderscript.ProgramFragment;
import android.renderscript.ProgramFragmentFixedFunction;
import android.renderscript.ProgramRaster;
import android.renderscript.ProgramStore;
import android.renderscript.ProgramVertex;
import android.renderscript.ProgramVertexFixedFunction;
import android.renderscript.RenderScript;
import android.renderscript.RenderScriptGL;
import android.renderscript.Sampler;
import android.renderscript.ProgramStore.BlendDstFunc;
import android.renderscript.ProgramStore.BlendSrcFunc;

public class PageCurlRS {

    int mWidth;
    int mHeight;

    public PageCurlRS() {
    }

    public void init(RenderScriptGL rs, Resources res) {
        mRS = rs;
        mWidth = mRS.getWidth();
        mHeight = mRS.getHeight();
        mRes = res;
        mOptionsARGB.inScaled = false;
        mOptionsARGB.inPreferredConfig = Bitmap.Config.ARGB_8888;
        initRS();
    }

    public void surfaceChanged() {
        mWidth = mRS.getWidth();
        mHeight = mRS.getHeight();
        Matrix4f proj = new Matrix4f();
        proj.loadOrthoWindow(mWidth, mHeight);
        mPVA.setProjection(proj);
    }

    private Resources mRes;
    private RenderScriptGL mRS;
    private Sampler mLinearClamp;
    private ProgramStore mProgStoreBlendNone;
    private ProgramFragment mProgFragmentTexture;
    private ProgramVertex mProgVertex;
    private ProgramVertexFixedFunction.Constants mPVA;
    private ProgramRaster mCullNone;
    private ScriptC_pagecurl mScript;

    private final BitmapFactory.Options mOptionsARGB = new BitmapFactory.Options();

    int px, py;
    
    public void onActionDown(int x, int y) {
        px = x;
        py = y;
        mScript.set_gPx(x);
        mScript.set_gPy(y);
    }
    
    public void onActionMove(int x, int y) {
    	mScript.set_gVx((float)(px - x)/3);
    	mScript.set_gVy((float)(py - y)/3);
    }
    
    public void onActionUp(int x, int y) {
    	mScript.set_gVx(0);
    	mScript.set_gVy(0);
    	px = 0;
    	py = 0;
    }

    ProgramStore BLEND_ADD_DEPTH_NONE(RenderScript rs) {
        ProgramStore.Builder builder = new ProgramStore.Builder(rs);
        builder.setDepthFunc(ProgramStore.DepthFunc.ALWAYS);
        builder.setBlendFunc(BlendSrcFunc.ONE, BlendDstFunc.ONE);
        builder.setDitherEnabled(false);
        builder.setDepthMaskEnabled(false);
        return builder.create();
    }

    private void initProgramStore() {
        // Use stock the stock program store object
        mProgStoreBlendNone = ProgramStore.BLEND_NONE_DEPTH_NONE(mRS);
        mScript.set_gProgStoreBlendNone(mProgStoreBlendNone);
    }

    private void initProgramFragment() {
        ProgramFragmentFixedFunction.Builder texBuilder = new ProgramFragmentFixedFunction.Builder(mRS);
        texBuilder.setTexture(ProgramFragmentFixedFunction.Builder.EnvMode.REPLACE,
                              ProgramFragmentFixedFunction.Builder.Format.RGBA, 0);
        mProgFragmentTexture = texBuilder.create();
        mProgFragmentTexture.bindSampler(mLinearClamp, 0);
        
        mScript.set_gProgFragmentTexture(mProgFragmentTexture);
    }

    private void initProgramVertex() {
        ProgramVertexFixedFunction.Builder pvb = new ProgramVertexFixedFunction.Builder(mRS);
        mProgVertex = pvb.create();

        mPVA = new ProgramVertexFixedFunction.Constants(mRS);
        ((ProgramVertexFixedFunction)mProgVertex).bindConstants(mPVA);
        Matrix4f proj = new Matrix4f();
        proj.loadOrthoWindow(mWidth, mHeight);
        mPVA.setProjection(proj);

        mScript.set_gProgVertex(mProgVertex);
    }
    
    private void loadImages(int id) {
    	// Generate the original bitmap
    	Bitmap b = BitmapFactory.decodeResource(mRes, id, mOptionsARGB);
    	
    	// Split the bitmap to 5 parts
    	mScript.set_gTex_00(Allocation.createFromBitmap(mRS, Bitmap.createBitmap(b,   0,   0, 198, 512),
                Allocation.MipmapControl.MIPMAP_ON_SYNC_TO_TEXTURE,
                Allocation.USAGE_GRAPHICS_TEXTURE));

    	mScript.set_gTex_01(Allocation.createFromBitmap(mRS, Bitmap.createBitmap(b, 198,   0, 116, 512),
                Allocation.MipmapControl.MIPMAP_ON_SYNC_TO_TEXTURE,
                Allocation.USAGE_GRAPHICS_TEXTURE));
    	
    	mScript.set_gTex_02(Allocation.createFromBitmap(mRS, Bitmap.createBitmap(b, 314,   0,  82, 512),
                Allocation.MipmapControl.MIPMAP_ON_SYNC_TO_TEXTURE,
                Allocation.USAGE_GRAPHICS_TEXTURE));
    	
    	mScript.set_gTex_03(Allocation.createFromBitmap(mRS, Bitmap.createBitmap(b, 396,   0,  64, 512),
                Allocation.MipmapControl.MIPMAP_ON_SYNC_TO_TEXTURE,
                Allocation.USAGE_GRAPHICS_TEXTURE));
    	
    	mScript.set_gTex_04(Allocation.createFromBitmap(mRS, Bitmap.createBitmap(b, 460,   0,  52, 512),
                Allocation.MipmapControl.MIPMAP_ON_SYNC_TO_TEXTURE,
                Allocation.USAGE_GRAPHICS_TEXTURE));
        
    	ScriptField_Bitmaps bitmap = new ScriptField_Bitmaps(mRS, 5);
        mScript.bind_bitmap(bitmap);
    }

    private void initSamplers() {
        mLinearClamp = Sampler.CLAMP_LINEAR(mRS);
        mScript.set_gLinearClamp(mLinearClamp);
    }

    private void initProgramRaster() {
        mCullNone = ProgramRaster.CULL_NONE(mRS);
        mScript.set_gCullNone(mCullNone);
    }

    private void initRS() {
        mScript = new ScriptC_pagecurl(mRS, mRes, R.raw.pagecurl);

        initSamplers();
        initProgramStore();
        initProgramFragment();
        initProgramVertex();
        loadImages(R.drawable.frog);
        initProgramRaster();

        mRS.bindRootScript(mScript);
    }
}
