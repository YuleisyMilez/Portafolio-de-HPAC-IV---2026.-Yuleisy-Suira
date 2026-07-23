package com.chiriquieats.app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chiriquieats.app.data.ChiriquiEatsDbHelper;

public class AddEmpresaActivity extends AppCompatActivity {

    private ChiriquiEatsDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_empresa);
        dbHelper = new ChiriquiEatsDbHelper(this);

        //Llenado del spinner de la categoría de la empresa
        Spinner categoriaSpinner = findViewById(R.id.spinner_categoria_empresa);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.categorias_empresa_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriaSpinner.setAdapter(adapter);

        //Botón de añadir empresa
        Button addButton = findViewById(R.id.button_submit_empresa);
        addButton.setOnClickListener(view -> saveEmpresa(categoriaSpinner));
    }

    //Metodo para guardar una nueva empresa
    private void saveEmpresa(Spinner categoriaSpinner) {
        String ruc = getInputText(R.id.input_ruc);
        String nombre = getInputText(R.id.input_nombre);
        String direccion = getInputText(R.id.input_direccion);
        String telefono = getInputText(R.id.input_telefono);
        String correo = getInputText(R.id.input_correo);
        String categoria = categoriaSpinner.getSelectedItem().toString();

        if (ruc.isEmpty() || nombre.isEmpty() || direccion.isEmpty()
                || telefono.isEmpty() || correo.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            dbHelper.insertEmpresa(ruc, nombre, direccion, categoria, telefono, correo);
            Toast.makeText(this, "Empresa guardada", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception exception) {
            Toast.makeText(this, "No se pudo guardar la empresa", Toast.LENGTH_SHORT).show();
        }
    }

    private String getInputText(int viewId) {
        EditText editText = findViewById(viewId);
        return editText.getText().toString().trim();
    }
}
