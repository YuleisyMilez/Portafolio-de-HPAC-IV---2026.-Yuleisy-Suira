package com.chiriquieats.app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chiriquieats.app.data.ChiriquiEatsDbHelper;
import com.chiriquieats.app.data.SpinnerOption;

public class AddMenuActivity extends AppCompatActivity {

    private ChiriquiEatsDbHelper dbHelper;
    private Spinner empresaSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu);
        dbHelper = new ChiriquiEatsDbHelper(this);

        //llenado de el spinner que muestra las empresas
        empresaSpinner = findViewById(R.id.spinner_add_menu_empresa);
        ArrayAdapter<SpinnerOption> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                dbHelper.getEmpresaOptions()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        empresaSpinner.setAdapter(adapter);

        //Botón de añadir un nuevo menú
        Button addButton = findViewById(R.id.button_submit_menu);
        addButton.setOnClickListener(view -> saveMenu());
    }

    //Metodo para cargar el menu nuevo en la base de datos
    private void saveMenu() {
        SpinnerOption selectedEmpresa = (SpinnerOption) empresaSpinner.getSelectedItem();
        String codigo = getInputText(R.id.input_codigo_menu);
        String nombre = getInputText(R.id.input_nombre_menu);
        String contenido = getInputText(R.id.input_contenido_menu);
        String precioText = getInputText(R.id.input_precio_menu);

        if (selectedEmpresa == null || selectedEmpresa.isPlaceholder()
                || codigo.isEmpty() || nombre.isEmpty() || contenido.isEmpty() || precioText.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double precio = Double.parseDouble(precioText);
            dbHelper.insertMenu(codigo, selectedEmpresa.getValue(), nombre, contenido, precio);
            Toast.makeText(this, "Menu guardado", Toast.LENGTH_SHORT).show();
            finish();
        } catch (NumberFormatException exception) {
            Toast.makeText(this, "Ingresa un precio valido", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Toast.makeText(this, "No se pudo guardar el menu", Toast.LENGTH_SHORT).show();
        }
    }

    private String getInputText(int viewId) {
        EditText editText = findViewById(viewId);
        return editText.getText().toString().trim();
    }
}
