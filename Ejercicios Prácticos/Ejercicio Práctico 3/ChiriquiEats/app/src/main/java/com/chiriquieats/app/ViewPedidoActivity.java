package com.chiriquieats.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.chiriquieats.app.data.ChiriquiEatsDbHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ViewPedidoActivity extends AppCompatActivity {

    public static final String EXTRA_CODIGO_PEDIDO = "codigo_pedido";

    private ChiriquiEatsDbHelper dbHelper;
    private String[] pedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pedido);
        dbHelper = new ChiriquiEatsDbHelper(this);

        long codigoPedido = getIntent().getLongExtra(EXTRA_CODIGO_PEDIDO, -1);
        loadPedido(codigoPedido);

        //Boton de enviar JSON
        Button sendButton = findViewById(R.id.button_enviarJSON);
        sendButton.setOnClickListener(view -> enviarJSON());

        //Boton de aceptar
        Button acceptButton = findViewById(R.id.button_accept_view_pedido);
        acceptButton.setOnClickListener(view -> finish());
    }

    //Metodo para cargar los datos del pedido
    private void loadPedido(long codigoPedido) {
        if (codigoPedido == -1) {
            Toast.makeText(this, "No se encontro el pedido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pedido = dbHelper.getPedidoDetalleByCodigo(codigoPedido);
        if (pedido == null) {
            Toast.makeText(this, "No se encontro el pedido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setText(R.id.text_view_codigo_pedido, pedido[0]);
        setText(R.id.text_view_fecha_pedido, pedido[1]);
        setText(R.id.text_view_empresa_pedido, pedido[2]);
        setText(R.id.text_view_menu_pedido, pedido[3]);
        setText(R.id.text_view_contenido_menu_pedido, pedido[4]);
        setText(R.id.text_view_precio_menu_pedido, pedido[5]);
        setText(R.id.text_view_cliente_pedido, pedido[6]);
        setText(R.id.text_view_direccion_pedido, pedido[7]);
        setText(R.id.text_view_monto_envio_pedido, pedido[8]);
        setText(R.id.text_view_codigo_pago, emptyFallback(pedido[9]));
        setText(R.id.text_view_numero_origen_pago, emptyFallback(pedido[10]));
        setText(R.id.text_view_numero_destino_pago, emptyFallback(pedido[11]));
        setText(R.id.text_view_monto_pago, emptyFallback(pedido[12]));

        String capturaBase64 = pedido[13];

        if (capturaBase64 != null && !capturaBase64.isEmpty()) {

            byte[] imageBytes = Base64.decode(
                    capturaBase64,
                    Base64.DEFAULT
            );

            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    imageBytes,
                    0,
                    imageBytes.length
            );

            if (bitmap != null) {
                ImageView captura = findViewById(R.id.image_view_captura_pago);
                captura.setImageBitmap(bitmap);
            }
        }
    }

    //Metodo para crear un JSON y enviarlo por correo
    private void enviarJSON() {
        try {

            File jsonFile = guardarJsonFactura();

            byte[] imageBytes = Base64.decode(
                    pedido[13],
                    Base64.DEFAULT
            );

            File imageFile = guardarImagen(
                    imageBytes,
                    "captura_pago_" + pedido[0]
            );

            Uri jsonUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    jsonFile
            );

            Uri imageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    imageFile
            );

            ArrayList<Uri> archivos = new ArrayList<>();

            archivos.add(jsonUri);
            archivos.add(imageUri);

            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);

            intent.setPackage("com.google.android.gm");
            intent.setType("*/*");

            intent.putExtra(
                    Intent.EXTRA_EMAIL,
                    new String[]{"axelc1405@gmail.com"}
            );

            intent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Factura Pedido " + pedido[0]
            );

            intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Cliente: " + pedido[6] + "\nDirección: " + pedido[7]
            );

            intent.putParcelableArrayListExtra(
                    Intent.EXTRA_STREAM,
                    archivos
            );

            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

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

    //Metodo para guardar la factura como un JSON
    private File guardarJsonFactura()
            throws JSONException, IOException {

        JSONObject factura = new JSONObject();

        factura.put("codigo_pedido", pedido[0]);
        factura.put("fecha", pedido[1]);
        factura.put("empresa", pedido[2]);
        factura.put("menu", pedido[3]);
        factura.put("contenido", pedido[4]);
        factura.put("precio", pedido[5]);
        factura.put("cliente", pedido[6]);
        factura.put("direccion", pedido[7]);
        factura.put("monto_envio", pedido[8]);

        JSONObject pago = new JSONObject();
        pago.put("codigo_pago", pedido[9]);
        pago.put("numero_origen", pedido[10]);
        pago.put("numero_destino", pedido[11]);
        pago.put("monto", pedido[12]);

        factura.put("pago", pago);

        String nombreArchivo =
                "factura_" + pedido[0] + ".json";

        File archivo = new File(
                getExternalFilesDir(null),
                nombreArchivo
        );

        FileWriter writer = new FileWriter(archivo);
        writer.write(factura.toString(4)); // JSON formateado
        writer.close();

        return archivo;
    }

    //Metodo para guardar la captura como jpg
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

    //Metodo para cargar los datos en los textView
    private void setText(int viewId, String value) {
        TextView textView = findViewById(viewId);
        textView.setText(value);
    }

    //Metodo por si encuentra registros vacíos
    private String emptyFallback(String value) {
        return value == null || value.isEmpty() ? "Sin pago registrado" : value;
    }
}
