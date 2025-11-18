// ZoomLayout.java
package com.example.studentmanager_system.Util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ZoomLayout extends FrameLayout {

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    private float scaleFactor = 1.0f;
    private float translationX = 0.0f;
    private float translationY = 0.0f;

    // 拖拽相关变量
    private float lastTouchX, lastTouchY;
    private boolean isDragging = false;

    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 5.0f;

    public ZoomLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ZoomLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        final int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX();
                final float y = event.getY();

                lastTouchX = x;
                lastTouchY = y;
                isDragging = true;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (!isDragging) break;

                final float x = event.getX();
                final float y = event.getY();

                // 只在缩放状态下允许拖拽
                if (scaleFactor > 1.0f) {
                    final float dx = x - lastTouchX;
                    final float dy = y - lastTouchY;

                    // 计算合理的拖拽范围，允许看到图片的所有部分
                    translationX += dx;
                    translationY += dy;

                    // 应用限制，确保不会拖出太多空白区域
                    applyBounds();

                    applyTransformation();
                }

                lastTouchX = x;
                lastTouchY = y;
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                isDragging = false;
                break;
            }
        }

        return true;
    }

    private void applyTransformation() {
        if (getChildCount() > 0) {
            getChildAt(0).setScaleX(scaleFactor);
            getChildAt(0).setScaleY(scaleFactor);
            getChildAt(0).setTranslationX(translationX);
            getChildAt(0).setTranslationY(translationY);
        }
    }

    // 计算并应用边界限制
    private void applyBounds() {
        if (getChildCount() == 0) return;

        View child = getChildAt(0);

        // 计算图片缩放后的实际尺寸
        float scaledWidth = child.getWidth() * scaleFactor;
        float scaledHeight = child.getHeight() * scaleFactor;

        // 计算最大允许的偏移量
        float maxTranslationX = Math.max(0, (scaledWidth - getWidth()) / 2);
        float maxTranslationY = Math.max(0, (scaledHeight - getHeight()) / 2);

        // 限制偏移量在合理范围内
        if (scaledWidth > getWidth()) {
            translationX = Math.max(-maxTranslationX, Math.min(maxTranslationX, translationX));
        } else {
            translationX = 0; // 如果图片比屏幕小，则不需要偏移
        }

        if (scaledHeight > getHeight()) {
            translationY = Math.max(-maxTranslationY, Math.min(maxTranslationY, translationY));
        } else {
            translationY = 0; // 如果图片比屏幕小，则不需要偏移
        }
    }

    public void resetZoom() {
        scaleFactor = 1.0f;
        translationX = 0.0f;
        translationY = 0.0f;
        applyTransformation();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float oldScale = scaleFactor;
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));

            // 缩放时调整位置，使缩放中心保持在手指位置
            if (scaleFactor > 1.0f) {
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();

                // 计算缩放中心相对于图片中心的偏移
                float scaleChange = scaleFactor / oldScale;
                translationX = focusX - (focusX - translationX) * scaleChange;
                translationY = focusY - (focusY - translationY) * scaleChange;

                // 应用边界限制
                applyBounds();
            } else {
                // 如果缩放回到1.0，重置位置
                translationX = 0;
                translationY = 0;
            }

            applyTransformation();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            resetZoom();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // 处理双指滚动（缩放状态下的拖拽）
            if (scaleFactor > 1.0f) {
                translationX -= distanceX;
                translationY -= distanceY;

                // 应用边界限制
                applyBounds();

                applyTransformation();
                return true;
            }
            return false;
        }
    }
}