package com.example.appledisease;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.List;

public class BoundingBoxDrawer {

    private static final int[] CLASS_COLORS = {
            Color.parseColor("#FF5722"), // blotch_apple
            Color.parseColor("#4CAF50"), // normal_apple
            Color.parseColor("#9C27B0"), // rot_apple
            Color.parseColor("#FF9800"), // scab_apple
    };

    public static Bitmap draw(Bitmap bitmap, List<AppleDetector.Detection> detections) {
        Canvas canvas = new Canvas(bitmap);

        Paint boxPaint  = new Paint();
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(5f);
        boxPaint.setAntiAlias(true);

        Paint bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);

        for (AppleDetector.Detection det : detections) {
            int color = CLASS_COLORS[getClassIndex(det.label)];
            boxPaint.setColor(color);

            canvas.drawRect(det.box, boxPaint);

            String labelText = det.label.replace("_", " ").toUpperCase()
                    + "  " + String.format("%.0f%%", det.confidence * 100);

            float textWidth  = textPaint.measureText(labelText);
            float textHeight = 50f;
            float labelTop   = det.box.top - textHeight - 8f;
            if (labelTop < 0) labelTop = det.box.top + 8f;

            bgPaint.setColor(color);
            canvas.drawRect(det.box.left, labelTop,
                    det.box.left + textWidth + 16f, labelTop + textHeight + 8f, bgPaint);
            canvas.drawText(labelText, det.box.left + 8f, labelTop + textHeight, textPaint);
        }
        return bitmap;
    }

    private static int getClassIndex(String label) {
        for (int i = 0; i < AppleDetector.LABELS.length; i++)
            if (AppleDetector.LABELS[i].equals(label)) return i;
        return 0;
    }
}
