package com.example.chinoscafe;

import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chinoscafe.data.DatabaseHelper;
import com.example.chinoscafe.data.SyncManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {
    public static final String EXTRA_PEDIDO_ID = "pedido_id";
    public static final String EXTRA_TOTAL = "total";

    private DatabaseHelper db;
    private long pedidoId;
    private byte[] capturaPago;
    private ImageView imgReceiptPreview;
    private ActivityResultLauncher<String> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        db = new DatabaseHelper(this, new SyncManager());
        pedidoId = getIntent().getLongExtra(EXTRA_PEDIDO_ID, -1);
        double total = getIntent().getDoubleExtra(EXTRA_TOTAL, 0);

        imgReceiptPreview = findViewById(R.id.img_recipt_preview);
        imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), this::onReceiptSelected);
        setText(R.id.et_monto_pago, formatMoney(total));

        findViewById(R.id.btn_selectImage).setOnClickListener(v -> imagePicker.launch("image/*"));
        findViewById(R.id.btn_createPago).setOnClickListener(v -> guardarPago());
    }

    private void guardarPago() {
        String codigo = getInputText(R.id.et_codigoYappy);
        String origen = getInputText(R.id.et_numeroOrigen);
        String destino = getInputText(R.id.et_numeroDestino);
        double monto = parseDouble(getInputText(R.id.et_monto_pago));

        if (codigo.isEmpty() || origen.isEmpty() || destino.isEmpty() || monto <= 0) {
            toast("Completa los datos del pago");
            return;
        }

        db.guardarPago(Long.parseLong(codigo), pedidoId, origen, destino, monto, capturaPago);
        toast("Pago guardado");
        finish();
    }

    private void onReceiptSelected(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            capturaPago = readBytes(uri);
            imgReceiptPreview.setImageURI(uri);
            imgReceiptPreview.setVisibility(android.view.View.VISIBLE);
            toast("Imagen adjuntada");
        } catch (IOException e) {
            toast("No se pudo leer la imagen");
        }
    }

    private byte[] readBytes(Uri uri) throws IOException {
        try (InputStream input = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (input == null) {
                return null;
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private String getInputText(int id) {
        EditText editText = findViewById(id);
        return editText.getText().toString().trim();
    }

    private void setText(int id, String value) {
        EditText editText = findViewById(id);
        editText.setText(value);
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatMoney(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
