package com.example.chinoscafe;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.chinoscafe.data.DatabaseHelper;
import com.example.chinoscafe.data.SyncManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class ViewPedidoActivity extends AppCompatActivity {
    public static final String EXTRA_PEDIDO_ID = "pedido_id";

    private DatabaseHelper db;
    private long id_pedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pedido);
        db = new DatabaseHelper(this, new SyncManager());
        long pedidoId = getIntent().getLongExtra(EXTRA_PEDIDO_ID, -1);
        cargarPedido(pedidoId);

        findViewById(R.id.btn_enviarJSON).setOnClickListener(v -> enviarJSON(pedidoId));
        findViewById(R.id.btn_aceptarView).setOnClickListener(v -> finish());
    }

    private void cargarPedido(long pedidoId) {
        DatabaseHelper.PedidoDetalle pedido = db.getPedidoDetalle(pedidoId);
        if (pedido == null) {
            finish();
            return;
        }
        setText(R.id.tv_Direccion, pedido.direccion);
        setText(R.id.tv_Telefono, pedido.telefono);
        setText(R.id.tv_detalles, String.join("\n", pedido.detallesMenus));
        setText(R.id.tv_subtotal, "$" + formatMoney(pedido.subtotal));
        setText(R.id.tv_envio, "$" + formatMoney(pedido.envio));
        setText(R.id.tv_fecha, pedido.fecha);
        setText(R.id.tv_estado, pedido.estado);
        setText(R.id.tv_codigoYappy, pedido.codigoPago == 0 ? "Sin pago" : String.valueOf(pedido.codigoPago));
        setText(R.id.tv_numeroOrigen, pedido.numeroOrigen == null ? "Sin pago" : pedido.numeroOrigen);
        setText(R.id.tv_numeroDestino, pedido.numeroDestino == null ? "Sin pago" : pedido.numeroDestino);
        setText(R.id.tv_montoTotal, "$" + formatMoney(pedido.montoPago));

        if (pedido.captura != null && pedido.captura.length > 0) {
            ImageView imageView = findViewById(R.id.img_viewCaptura);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(pedido.captura, 0, pedido.captura.length));
        }
    }

    private void enviarJSON(long pedidoId) {
        DatabaseHelper.PedidoDetalle pedido = db.getPedidoDetalle(pedidoId);

        try {
            File jsonFile = guardarJsonFactura(pedido);

            byte[] imageBytes = pedido.captura;

            File imageFile = guardarImagen(imageBytes, "captura_pago_" + pedido.id);
            Uri jsonUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", jsonFile);
            Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", imageFile);

            ArrayList<Uri> archivos = new ArrayList<>();

            archivos.add(jsonUri);
            archivos.add(imageUri);

            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);

            intent.setPackage("com.google.android.gm");
            intent.setType("*/*");

            intent.putExtra(
                    Intent.EXTRA_EMAIL,
                    new String[]{getString(R.string.email)}
            );

            intent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Factura pedido " + pedido.id
            );

            intent.putParcelableArrayListExtra(
                    Intent.EXTRA_STREAM,
                    archivos
            );

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

            finish();
        } catch (IOException | JSONException e) {
            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Error al generar los archivos",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private File guardarJsonFactura(DatabaseHelper.PedidoDetalle pedido)
        throws JSONException, IOException {

        JSONObject factura = new JSONObject();

        factura.put("id_factura", pedido.id);
        factura.put("dirección_pedido", pedido.direccion);
        factura.put("telefono", pedido.telefono);
        factura.put("detalles", pedido.detallesMenus);
        factura.put("fecha", pedido.fecha);
        factura.put("subtotal", pedido.subtotal);
        factura.put("monto_envio", pedido.envio);
        factura.put("estado", pedido.estado);

        JSONObject pago = new JSONObject();
        pago.put("codigo_yappy", pedido.codigoPago);
        pago.put("numero_origen", pedido.numeroOrigen);
        pago.put("numero_destino", pedido.numeroDestino);
        pago.put("monto_total", pedido.montoPago);

        factura.put("pago", pago);

        String nombreArchivo = "factura_" + pedido.id + ".json";

        File archivo = new File(
                getExternalFilesDir(null),
                nombreArchivo
        );

        FileWriter writer = new FileWriter(archivo);
        writer.write(factura.toString(4));
        writer.close();

        return archivo;
    }

    private File guardarImagen(byte[] imageBytes, String nombreArchivo)
            throws IOException {

        File archivo = new File(
                getExternalFilesDir(null),
                nombreArchivo + ".jpg"
        );

        FileOutputStream fos = new FileOutputStream(archivo);
        fos.write(imageBytes);
        fos.flush();
        fos.close();

        return archivo;
    }

    private void setText(int id, String value) {
        ((TextView) findViewById(id)).setText(value == null ? "" : value);
    }

    private String formatMoney(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
