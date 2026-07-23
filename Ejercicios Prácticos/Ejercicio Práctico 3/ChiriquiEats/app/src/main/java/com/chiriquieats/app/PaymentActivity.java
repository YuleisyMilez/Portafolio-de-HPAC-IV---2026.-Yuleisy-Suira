package com.chiriquieats.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.chiriquieats.app.data.ChiriquiEatsDbHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PaymentActivity extends AppCompatActivity {

    private ChiriquiEatsDbHelper dbHelper;
    private ImageView receiptPreview;
    private TextView receiptHint;
    private EditText paymentAmount;
    private byte[] selectedImage;
    private long codigoPedido;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImageSelected);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        dbHelper = new ChiriquiEatsDbHelper(this);
        codigoPedido = getIntent().getLongExtra(AddPedidoActivity.EXTRA_CODIGO_PEDIDO, -1);

        paymentAmount = findViewById(R.id.input_payment_amount);
        paymentAmount.setEnabled(false);
        loadPaymentAmount();

        receiptPreview = findViewById(R.id.image_receipt_preview);
        receiptHint = findViewById(R.id.text_receipt_hint);

        //Boton de agregar imagen
        Button attachImageButton = findViewById(R.id.button_attach_receipt);
        attachImageButton.setOnClickListener(view -> imagePickerLauncher.launch("image/*"));

        //Boton de completar el pago
        Button completeOrderButton = findViewById(R.id.button_complete_order);
        completeOrderButton.setOnClickListener(view -> savePagoAndReturnHome());
    }

    private void onImageSelected(Uri uri) {
        if (uri == null) {
            return;
        }

        try {
            InputStream inputStream =
                    getContentResolver().openInputStream(uri);

            ByteArrayOutputStream buffer =
                    new ByteArrayOutputStream();

            byte[] data = new byte[1024];
            int nRead;

            while ((nRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }

            byte[] imageBytes = buffer.toByteArray();

            showSelectedImage(imageBytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPaymentAmount() {
        if (codigoPedido == -1) {
            return;
        }

        String[] pedido = dbHelper.getPedidoDetalleByCodigo(codigoPedido);
        if (pedido == null) {
            return;
        }

        double precioMenu = Double.parseDouble(pedido[5]);
        double montoEnvio = Double.parseDouble(pedido[8]);
        paymentAmount.setText(String.valueOf(precioMenu + montoEnvio));
    }

    //Metodo para mostrar la imagen que se adjunto
    private void showSelectedImage(byte[] image) {
        if (image == null) {
            return;
        }

        selectedImage = image;

        Bitmap bitmap = BitmapFactory.decodeByteArray(
                image,
                0,
                image.length
        );

        receiptPreview.setImageBitmap(bitmap);
        receiptPreview.setVisibility(View.VISIBLE);
        receiptHint.setText("Imagen adjuntada");
    }

    //Metodo para subir el pago a la base de datos
    private void savePagoAndReturnHome() {
        String codigoPagoText = getInputText(R.id.input_yappy_code);
        String numeroOrigen = getInputText(R.id.input_origin_number);
        String numeroDestino = getInputText(R.id.input_destination_number);
        String montoText = getInputText(R.id.input_payment_amount);

        if (codigoPedido == -1 || codigoPagoText.isEmpty() || numeroOrigen.isEmpty()
                || numeroDestino.isEmpty() || montoText.isEmpty() || selectedImage == null) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long codigoPago = Long.parseLong(codigoPagoText);
            double monto = Double.parseDouble(montoText);
            dbHelper.insertPago(
                    codigoPago,
                    codigoPedido,
                    numeroOrigen,
                    numeroDestino,
                    monto,
                    selectedImage
            );
            Toast.makeText(this, "Pedido completado", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (NumberFormatException exception) {
            Toast.makeText(this, "Ingresa codigo y monto validos", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Toast.makeText(this, "No se pudo guardar el pago", Toast.LENGTH_SHORT).show();
        }
    }

    //Metodo para recuperar los textos de los EditText
    private String getInputText(int viewId) {
        EditText editText = findViewById(viewId);
        return editText.getText().toString().trim();
    }
}
