package com.jay.ai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Lightweight animated Jay character used for idle/listening/speaking states.
 * No external image or network dependency is required.
 */
public class JayAvatarView extends View {

    public enum State {
        IDLE,
        LISTENING,
        SPEAKING
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private State state = State.IDLE;
    private long animationStart = System.currentTimeMillis();

    public JayAvatarView(Context context) {
        super(context);
        init();
    }

    public JayAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        paint.setStrokeCap(Paint.Cap.ROUND);
        setFocusable(false);
    }

    public void setState(State newState) {
        if (newState == null) {
            newState = State.IDLE;
        }
        state = newState;
        animationStart = System.currentTimeMillis();
        invalidate();
    }

    public State getState() {
        return state;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float scale = Math.min(w, h) / 280f;

        long elapsed = System.currentTimeMillis() - animationStart;
        float pulse = (float) (0.5 + 0.5 * Math.sin(elapsed / 420.0));
        float breathe = (float) (Math.sin(elapsed / 1200.0) * 3.0 * scale);

        // Holographic aura.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((4f + pulse * 3f) * scale);
        paint.setShadowLayer((12f + pulse * 12f) * scale, 0, 0,
                state == State.LISTENING ? 0xFF35C8FF : 0xFF8B5CFF);
        paint.setColor(state == State.LISTENING ? 0xFF35C8FF : 0xFF8B5CFF);
        canvas.drawCircle(cx, cy, 112f * scale + pulse * 5f * scale, paint);
        paint.clearShadowLayer();

        // Head.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF10152B);
        RectF head = new RectF(
                cx - 67f * scale,
                cy - 82f * scale + breathe,
                cx + 67f * scale,
                cy + 78f * scale + breathe
        );
        canvas.drawOval(head, paint);

        // Hair / futuristic silhouette.
        paint.setColor(0xFF263B68);
        RectF hair = new RectF(
                cx - 70f * scale,
                cy - 93f * scale + breathe,
                cx + 70f * scale,
                cy - 10f * scale + breathe
        );
        canvas.drawArc(hair, 180, 180, true, paint);

        // Eyes.
        float eyeY = cy - 20f * scale + breathe;
        float eyeOffset = 28f * scale;
        paint.setColor(0xFF8FE9FF);
        paint.setShadowLayer(8f * scale, 0, 0, 0xFF35C8FF);
        canvas.drawCircle(cx - eyeOffset, eyeY, 7f * scale, paint);
        canvas.drawCircle(cx + eyeOffset, eyeY, 7f * scale, paint);
        paint.clearShadowLayer();

        // Mouth changes with state.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f * scale);
        paint.setColor(0xFFB58CFF);
        if (state == State.SPEAKING) {
            float mouth = 7f + pulse * 15f;
            canvas.drawOval(new RectF(
                    cx - 18f * scale,
                    cy + 30f * scale + breathe,
                    cx + 18f * scale,
                    cy + (30f + mouth) * scale + breathe
            ), paint);
        } else {
            canvas.drawArc(new RectF(
                    cx - 23f * scale,
                    cy + 23f * scale + breathe,
                    cx + 23f * scale,
                    cy + 48f * scale + breathe
            ), 15, 150, false, paint);
        }

        // Listening ring / sound waves.
        if (state == State.LISTENING) {
            paint.setColor(0xFF35C8FF);
            paint.setStrokeWidth(3f * scale);
            for (int i = 0; i < 3; i++) {
                float radius = (88f + i * 15f + pulse * 7f) * scale;
                paint.setAlpha(150 - i * 35);
                canvas.drawArc(new RectF(
                        cx - radius,
                        cy - radius,
                        cx + radius,
                        cy + radius
                ), 210, 120, false, paint);
                canvas.drawArc(new RectF(
                        cx - radius,
                        cy - radius,
                        cx + radius,
                        cy + radius
                ), 30, 120, false, paint);
            }
            paint.setAlpha(255);
        }

        // Speaking waveform dots.
        if (state == State.SPEAKING) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFB58CFF);
            for (int i = -2; i <= 2; i++) {
                float dotHeight = (8f + pulse * 12f) * scale * (1f - Math.abs(i) * 0.12f);
                canvas.drawRoundRect(
                        cx + i * 16f * scale - 3f * scale,
                        cy + 96f * scale - dotHeight,
                        cx + i * 16f * scale + 3f * scale,
                        cy + 96f * scale + dotHeight,
                        4f * scale,
                        4f * scale,
                        paint
                );
            }
        }

        postInvalidateDelayed(32);
    }
}
