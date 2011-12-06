/**
 * PageCurlView.java
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

import android.renderscript.RSSurfaceView;
import android.renderscript.RenderScriptGL;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class PageCurlView extends RSSurfaceView {
    public PageCurlView(Context context) {
        super(context);
        ensureRenderScript();
    }

    private RenderScriptGL mRS;
    private PageCurlRS mRender;

    private void ensureRenderScript() {
        if (mRS == null) {
            RenderScriptGL.SurfaceConfig sc = new RenderScriptGL.SurfaceConfig();
            sc.setDepth(16, 24);
            mRS = createRenderScriptGL(sc);
            mRender = new PageCurlRS();
            mRender.init(mRS, getResources());
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ensureRenderScript();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        mRender.surfaceChanged();
    }

    @Override
    protected void onDetachedFromWindow() {
        mRender = null;
        if (mRS != null) {
            mRS = null;
            destroyRenderScriptGL();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mRender.onActionDown((int)ev.getX(), (int)ev.getY());
            return true;
        }

        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            mRender.onActionMove((int)ev.getX(), (int)ev.getY());
        	return true;
        }
        
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            mRender.onActionUp((int)ev.getX(), (int)ev.getY());
        	return true;
        }
        
        return false;
    }
}
