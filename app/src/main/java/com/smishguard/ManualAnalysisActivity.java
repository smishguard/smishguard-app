package com.smishguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.smishguard.databinding.ActivityManualAnalysisBinding;

public class ManualAnalysisActivity extends AppCompatActivity {

    private ActivityManualAnalysisBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManualAnalysisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBackMainManualAnalisis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ManualAnalysisActivity.this, MainActivity.class));
            }
        });

        binding.btnAnalisis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //LOGICA BACKEND
            }
        });
    }
}