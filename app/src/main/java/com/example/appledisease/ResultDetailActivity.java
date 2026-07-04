package com.example.appledisease;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_detail);

        ImageView imgView      = findViewById(R.id.detailImage);
        TextView  tvName       = findViewById(R.id.detailDiseaseName);
        TextView  tvConfidence = findViewById(R.id.detailConfidence);
        TextView  tvDateTime   = findViewById(R.id.detailDateTime);
        TextView  tvDesc       = findViewById(R.id.detailDescription);

        // Get data from intent
        String imagePath   = getIntent().getStringExtra("imagePath");
        String diseaseName = getIntent().getStringExtra("diseaseName");
        float  confidence  = getIntent().getFloatExtra("confidence", 0f);
        String dateTime    = getIntent().getStringExtra("dateTime");

        // Set image
        if (imagePath != null) {
            Bitmap bmp = BitmapFactory.decodeFile(imagePath);
            if (bmp != null) imgView.setImageBitmap(bmp);
        }

        // Set disease info
        DiseaseInfo info = DiseaseInfo.getInfo(diseaseName);
        tvName.setText(info.displayName);
        tvConfidence.setText(String.format("Confidence: %.1f%%", confidence * 100));
        tvDateTime.setText("📅 " + dateTime);
        tvDesc.setText(info.description);
    }
}