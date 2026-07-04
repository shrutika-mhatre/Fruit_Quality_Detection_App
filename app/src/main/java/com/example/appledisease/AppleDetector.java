package com.example.appledisease;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppleDetector {

    private static final String MODEL_FILE    = "best.tflite";
    public  static final int    INPUT_SIZE    = 640;
    private static final int    NUM_CLASSES   = 4;
    private static final float  CONF_THRESHOLD = 0.40f;
    private static final float  IOU_THRESHOLD  = 0.45f;

    public static final String[] LABELS = {
            "blotch_apple",
            "normal_apple",
            "rot_apple",
            "scab_apple"
    };

    public static class Detection {
        public final RectF  box;
        public final String label;
        public final float  confidence;

        Detection(RectF box, String label, float confidence) {
            this.box        = box;
            this.label      = label;
            this.confidence = confidence;
        }
    }

    private final Interpreter  interpreter;
    private final ByteBuffer   inputBuffer;
    private final float[][][]  outputBuffer;

    public AppleDetector(Context context) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        interpreter  = new Interpreter(FileUtil.loadMappedFile(context, MODEL_FILE), options);
        inputBuffer  = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4);
        inputBuffer.order(ByteOrder.nativeOrder());
        outputBuffer = new float[1][4 + NUM_CLASSES][8400];
    }

    public List<Detection> detect(Bitmap bitmap) {
        int imgW = bitmap.getWidth();
        int imgH = bitmap.getHeight();

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        bitmapToBuffer(resized, inputBuffer);

        HashMap<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, outputBuffer);
        interpreter.runForMultipleInputsOutputs(new Object[]{inputBuffer}, outputs);

        return postProcess(outputBuffer[0], imgW, imgH);
    }

    private void bitmapToBuffer(Bitmap bitmap, ByteBuffer buffer) {
        buffer.rewind();
        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);
        for (int pixel : pixels) {
            buffer.putFloat(((pixel >> 16) & 0xFF) / 255.0f);
            buffer.putFloat(((pixel >> 8)  & 0xFF) / 255.0f);
            buffer.putFloat(( pixel        & 0xFF) / 255.0f);
        }
    }

    private List<Detection> postProcess(float[][] raw, int imgW, int imgH) {
        List<Detection> candidates = new ArrayList<>();

        // Scale factors from 640 space to original image space
        float scaleX = imgW / (float) INPUT_SIZE;
        float scaleY = imgH / (float) INPUT_SIZE;

        int numBoxes = raw[0].length; // 8400
        for (int i = 0; i < numBoxes; i++) {

            // Find best class
            int   bestClass = 0;
            float bestScore = 0f;
            for (int c = 0; c < NUM_CLASSES; c++) {
                float score = raw[4 + c][i];
                if (score > bestScore) {
                    bestScore = score;
                    bestClass = c;
                }
            }

            if (bestScore < CONF_THRESHOLD) continue;

            // cx, cy, w, h in 640 pixel space
            float cx = raw[0][i];
            float cy = raw[1][i];
            float w  = raw[2][i];
            float h  = raw[3][i];

// Values already 0-1 normalized hain, directly multiply karo image size se
            float x1 = (cx - w / 2f) * imgW;
            float y1 = (cy - h / 2f) * imgH;
            float x2 = (cx + w / 2f) * imgW;
            float y2 = (cy + h / 2f) * imgH;

// Clamp
            x1 = Math.max(0, x1);
            y1 = Math.max(0, y1);
            x2 = Math.min(imgW, x2);
            y2 = Math.min(imgH, y2);

            candidates.add(new Detection(new RectF(x1, y1, x2, y2), LABELS[bestClass], bestScore));
        }

        List<Detection> results = nms(candidates);
        results.sort((a, b) -> Float.compare(b.confidence, a.confidence));
        return results;
    }

    private List<Detection> nms(List<Detection> boxes) {
        boxes.sort((a, b) -> Float.compare(b.confidence, a.confidence));
        List<Detection> kept       = new ArrayList<>();
        boolean[]       suppressed = new boolean[boxes.size()];

        for (int i = 0; i < boxes.size(); i++) {
            if (suppressed[i]) continue;
            kept.add(boxes.get(i));
            for (int j = i + 1; j < boxes.size(); j++) {
                if (!suppressed[j] && iou(boxes.get(i).box, boxes.get(j).box) > IOU_THRESHOLD)
                    suppressed[j] = true;
            }
        }
        return kept;
    }

    private float iou(RectF a, RectF b) {
        float iL = Math.max(a.left, b.left),   iT = Math.max(a.top, b.top);
        float iR = Math.min(a.right, b.right), iB = Math.min(a.bottom, b.bottom);
        if (iR <= iL || iB <= iT) return 0f;
        float inter = (iR - iL) * (iB - iT);
        float aArea = (a.right - a.left) * (a.bottom - a.top);
        float bArea = (b.right - b.left) * (b.bottom - b.top);
        return inter / (aArea + bArea - inter);
    }

    public void close() {
        if (interpreter != null) interpreter.close();
    }
}
