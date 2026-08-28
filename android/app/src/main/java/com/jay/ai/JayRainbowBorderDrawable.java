package com.jay.ai;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.LinearInterpolator;
import android.animation.ValueAnimator;

/** Lightweight animated rainbow/neon border for Jay's home screen. */
public final class JayRainbowBorderDrawable extends Drawable {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int baseColor = Color.rgb(7, 10, 24);
    private float phase;
    private ValueAnimator animator;

    public JayRainbowBorderDrawable(int baseColor) {
        this.baseColor = baseColor;
        fill.setStyle(Paint.Style.FILL);
        glow.setStyle(Paint.Style.STROKE);
        glow.setStrokeWidth(4f);
        startAnimation();
    }

    public void setBaseColor(int color) { baseColor = color; invalidateSelf(); }

    private void startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(6500L);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(a -> { phase = (Float)a.getAnimatedValue(); invalidateSelf(); });
        animator.start();
    }

    @Override public void draw(Canvas canvas) {
        RectF b = getBounds();
        if (b.width() <= 0 || b.height() <= 0) return;
        float inset = 2f;
        rect.set(b.left + inset, b.top + inset, b.right - inset, b.bottom - inset);
        fill.setColor(baseColor);
        canvas.drawRect(b, fill);

        int[] colors = new int[]{
                Color.rgb(255, 50, 120), Color.rgb(255, 150, 40),
                Color.rgb(255, 235, 60), Color.rgb(60, 255, 150),
                Color.rgb(40, 210, 255), Color.rgb(90, 90, 255),
                Color.rgb(220, 70, 255), Color.rgb(255, 50, 120)
        };
        float dx = (float)Math.cos(Math.toRadians(phase)) * b.width();
        float dy = (float)Math.sin(Math.toRadians(phase)) * b.height();
        LinearGradient gradient = new LinearGradient(
                b.left - dx, b.top - dy, b.right + dx, b.bottom + dy,
                colors, null, Shader.TileMode.MIRROR);
        glow.setShader(gradient);
        glow.setAlpha(85);
        glow.setStrokeWidth(12f);
        canvas.drawRoundRect(rect, 28f, 28f, glow);
        glow.setAlpha(220);
        glow.setStrokeWidth(3.5f);
        canvas.drawRoundRect(rect, 28f, 28f, glow);
    }

    @Override public void setAlpha(int alpha) { fill.setAlpha(alpha); glow.setAlpha(alpha); invalidateSelf(); }
    @Override public void setColorFilter(android.graphics.ColorFilter filter) { fill.setColorFilter(filter); glow.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
    @Override protected void onBoundsChange(android.graphics.Rect bounds) { super.onBoundsChange(bounds); invalidateSelf(); }

    public void stop() {
        if (animator != null) animator.cancel();
        handler.removeCallbacksAndMessages(null);
    }
}
