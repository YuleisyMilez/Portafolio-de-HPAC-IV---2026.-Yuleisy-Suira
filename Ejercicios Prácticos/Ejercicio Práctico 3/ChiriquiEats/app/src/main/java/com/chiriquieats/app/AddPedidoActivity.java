package com.chiriquieats.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chiriquieats.app.data.ChiriquiEatsDbHelper;
import com.chiriquieats.app.data.SpinnerOption;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddPedidoActivity extends AppCompatActivity {

    public static final String EXTRA_CODIGO_PEDIDO = "codigo_pedido";

    private ChiriquiEatsDbHelper dbHelper;
    private Spinner empresaSpinner;
    private Spinner menuSpinner;
    private TextView menuContentPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pedido);
        dbHelper = new ChiriquiEatsDbHelper(this);

        //identificacion de los spinner y el preview
        empresaSpinner = findViewById(R.id.spinner_pedido_empresa);
        menuSpinner = findViewById(R.id.spinner_pedido_menu);
        menuContentPreview = findViewById(R.id.text_menu_content_preview);

        setupEmpresaSpinner();
        setupMenuSpinner("");

        //botón de continuar con el pago
        Button paymentButton = findViewById(R.id.button_continue_payment);
        paymentButton.setOnClickListener(view -> savePedidoAndContinue());
    }

    //llenado del spinner de la empresa
    private void setupEmpresaSpinner() {
        ArrayAdapter<SpinnerOption> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                dbHelper.getEmpresaOptions()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        empresaSpinner.setAdapter(adapter);
        empresaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view,
                                       int position, long id) {
                SpinnerOption selectedEmpresa = (SpinnerOption) parent.getItemAtPosition(position);
                setupMenuSpinner(selectedEmpresa.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setupMenuSpinner("");
            }
        });
    }

    //llenado del spinner del menú
    private void setupMenuSpinner(String rucEmpresa) {
        List<SpinnerOption> options = dbHelper.getMenuOptions(rucEmpresa);
        ArrayAdapter<SpinnerOption> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        menuSpinner.setAdapter(adapter);
        menuContentPreview.setText("Contenido del menu");

        menuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view,
                                       int position, long id) {
                SpinnerOption selectedMenu = (SpinnerOption) parent.getItemAtPosition(position);
                String contenido = dbHelper.getMenuContenido(selectedMenu.getValue());
                menuContentPreview.setText(contenido.isEmpty() ? "Contenido del menu" : contenido);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                menuContentPreview.setText("Contenido del menu");
            }
        });
    }

    //Botón para cargar el pedido en la base de datos
    private void savePedidoAndContinue() {
        SpinnerOption selectedEmpresa = (SpinnerOption) empresaSpinner.getSelectedItem();
        SpinnerOption selectedMenu = (SpinnerOption) menuSpinner.getSelectedItem();
        String nombre = getInputText(R.id.input_pedido_nombre);
        String direccion = getInputText(R.id.input_pedido_direccion);
        String montoEnvioText = getInputText(R.id.input_monto_envio);

        if (selectedEmpresa == null || selectedEmpresa.isPlaceholder()
                || selectedMenu == null || selectedMenu.isPlaceholder()
                || nombre.isEmpty() || direccion.isEmpty() || montoEnvioText.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double montoEnvio = Double.parseDouble(montoEnvioText);
            String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
            long codigoPedido = dbHelper.insertPedido(
                    selectedEmpresa.getValue(),
                    selectedMenu.getValue(),
                    fecha,
                    montoEnvio,
                    nombre,
                    direccion
            );

            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra(EXTRA_CODIGO_PEDIDO, codigoPedido);
            startActivity(intent);
        } catch (NumberFormatException exception) {
            Toast.makeText(this, "Ingresa un monto de envio valido", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Toast.makeText(this, "No se pudo guardar el pedido", Toast.LENGTH_SHORT).show();
        }
    }

    private String getInputText(int viewId) {
        EditText editText = findViewById(viewId);
        return editText.getText().toString().trim();
    }
}
