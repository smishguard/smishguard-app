package com.smishguard.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.smishguard.R;
import com.smishguard.databinding.ActivityRegisterBinding;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private static final String URL_PRIVACY = "https://smishguard.github.io/smishguard-web/politica-privacidad.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ocultarBarrasDeSistema();
        FirebaseApp.initializeApp(this);

        binding.textViewPrivacyPolicy.setOnClickListener(v -> showPrivacyPolicyDialog());

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.setTextMailR.getText().toString().trim();
                String password = binding.setTextPasswordR.getText().toString().trim();
                String confirmPassword = binding.setTextConfirmPassword.getText().toString().trim(); // Nuevo campo

                if (email.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!esPasswordValido(password)) {
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!binding.checkBoxPrivacyPolicy.isChecked()) {
                    Toast.makeText(RegisterActivity.this, "Debes aceptar la política de privacidad para continuar", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.putExtra("showGuide", true);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Error en el registro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        binding.btnGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    private void showPrivacyPolicyDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_privacy_policy);

        dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.7));

        WebView webView = dialog.findViewById(R.id.webViewPrivacyPolicy);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(URL_PRIVACY);

        dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ocultarBarrasDeSistema();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RegisterActivity.this, InitialActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void ocultarBarrasDeSistema() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private boolean esPasswordValido(String password) {
        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        Pattern mayusculaPattern = Pattern.compile("[A-Z]");
        Matcher matcherMayuscula = mayusculaPattern.matcher(password);
        if (!matcherMayuscula.find()) {
            Toast.makeText(this, "La contraseña debe tener al menos una letra mayúscula", Toast.LENGTH_SHORT).show();
            return false;
        }

        Pattern minusculaPattern = Pattern.compile("[a-z]");
        Matcher matcherMinuscula = minusculaPattern.matcher(password);
        if (!matcherMinuscula.find()) {
            Toast.makeText(this, "La contraseña debe tener al menos una letra minúscula", Toast.LENGTH_SHORT).show();
            return false;
        }

        Pattern numeroPattern = Pattern.compile("[0-9]");
        Matcher matcherNumero = numeroPattern.matcher(password);
        if (!matcherNumero.find()) {
            Toast.makeText(this, "La contraseña debe tener al menos un número", Toast.LENGTH_SHORT).show();
            return false;
        }

        Pattern especialPattern = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
        Matcher matcherEspecial = especialPattern.matcher(password);
        if (!matcherEspecial.find()) {
            Toast.makeText(this, "La contraseña debe tener al menos un carácter especial", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}