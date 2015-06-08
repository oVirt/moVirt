/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ovirt.mobile.movirt.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {
    private final Paint paint;
    private CameraManager cameraManager;
    private Bitmap resultBitmap;
    private ResultPoint[] points;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }
        Rect frame = cameraManager.getFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        // Draw the exterior
        paint.setColor(Color.BLACK);
        canvas.drawLines(new float[]{
                frame.left, frame.top, frame.right, frame.top,
                frame.right, frame.top, frame.right, frame.bottom,
                frame.right, frame.bottom, frame.left, frame.bottom,
                frame.left, frame.bottom, frame.left, frame.top
        }, paint);

        if (resultBitmap != null) {
            //highlight detected code with red frame
            float wScale = (float) frame.width() / (float) previewFrame.width();
            float hScale = (float) frame.height() / (float) previewFrame.height();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(2.5f);
            float[] f = new float[]{
                    points[0].getX() * wScale + frame.left, points[0].getY() * hScale + frame.top,
                    points[1].getX() * wScale + frame.left, points[1].getY() * hScale + frame.top,
                    points[2].getX() * wScale + frame.left, points[2].getY() * hScale + frame.top,
                    points[3].getX() * wScale + frame.left, points[3].getY() * hScale + frame.top
            };
            canvas.drawLines(new float[]{
                            f[0], f[1], f[2], f[3], f[2], f[3], f[4], f[5],
                            f[4], f[5], f[6], f[7], f[6], f[7], f[0], f[1]
                    },
                    paint);
        }
    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    /**
     * Highlight detected code
     *
     * @param barcode An image of the decoded barcode.
     * @param points  Corners of barcode (should be scaled to properly fit on screen)
     */
    public void drawResultBitmap(Bitmap barcode, ResultPoint[] points) {
        resultBitmap = barcode;
        this.points = points;
        invalidate();
    }

}
