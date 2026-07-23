package com.example.chinoscafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chinoscafe.data.DatabaseHelper;
import com.example.chinoscafe.data.SyncManager;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        db = new DatabaseHelper(this, new SyncManager());
        setContentView(R.layout.inicio_sesion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btn_ingresar).setOnClickListener(v -> iniciarSesion());
    }

    private void iniciarSesion() {
        String usuario = getInputText(R.id.et_usuario);
        String contrasena = getInputText(R.id.et_contrasena);
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            toast("Ingresa usuario y contrasena");
            return;
        }

        long clienteId = db.autenticarCliente(usuario, contrasena);
        if (clienteId == -1) {
            toast("Credenciales invalidas");
            return;
        }

        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(HomeActivity.EXTRA_CLIENTE_ID, clienteId);
        startActivity(intent);
    }

    private String getInputText(int id) {
        EditText editText = findViewById(id);
        return editText.getText().toString().trim();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
