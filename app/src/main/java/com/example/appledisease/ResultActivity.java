package com.example.appledisease;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private static final String TAG = "ResultActivity";

    private ImageView resultImageView;
    private TextView  tvDiseaseName, tvConfidence, tvDescription, tvTreatment;
    private ProgressBar progressBar;
    private View resultCard;

    private AppleDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultImageView = findViewById(R.id.resultImageView);
        tvDiseaseName   = findViewById(R.id.tvDiseaseName);
        tvConfidence    = findViewById(R.id.tvConfidence);
        tvDescription   = findViewById(R.id.tvDescription);
        progressBar     = findViewById(R.id.progressBar);
        resultCard      = findViewById(R.id.resultCard);

        String imageUriStr = getIntent().getStringExtra("imageUri");
        if (imageUriStr == null) {
            Toast.makeText(this, "No image received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            detector = new AppleDetector(this);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load model: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        processImage(Uri.parse(imageUriStr));
    }

    private void processImage(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);
        resultCard.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap originalBitmap   = BitmapFactory.decodeStream(inputStream);

                if (originalBitmap == null) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Could not decode image", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Run detection
                List<AppleDetector.Detection> detections = detector.detect(originalBitmap);

                // Draw bounding boxes
                Bitmap annotated = BoundingBoxDrawer.draw(
                        originalBitmap.copy(Bitmap.Config.ARGB_8888, true), detections);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    resultCard.setVisibility(View.VISIBLE);
                    resultImageView.setImageBitmap(annotated);

                    if (detections.isEmpty()) {
                        tvDiseaseName.setText("No Detection");
                        tvConfidence.setText("Confidence: —");
                        tvDescription.setText("No apple disease detected. Please try with a clearer image.");
                        tvTreatment.setText("");
                    } else {
                        AppleDetector.Detection best = detections.get(0);
                        DiseaseInfo info = DiseaseInfo.getInfo(best.label);
                        tvDiseaseName.setText(info.displayName);
                        tvConfidence.setText(String.format("Confidence: %.1f%%", best.confidence * 100));
                        tvDescription.setText(info.description);

                        // Save to history
                        saveToHistory(annotated, best.label, best.confidence);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Detection error", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void saveToHistory(Bitmap bitmap, String label, float confidence) {
        new Thread(() -> {
            try {
                // Save image to internal storage
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(new Date());
                File dir = new File(getFilesDir(), "history_images");
                if (!dir.exists()) dir.mkdirs();

                File imageFile = new File(dir, "detection_" + timeStamp + ".jpg");
                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.close();

                // Save to Room DB
                String dateTime = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(new Date());

                DetectionHistory history = new DetectionHistory(
                        imageFile.getAbsolutePath(), label, confidence, dateTime);
                AppDatabase.getInstance(this).historyDao().insert(history);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (detector != null) detector.close();
    }
}
