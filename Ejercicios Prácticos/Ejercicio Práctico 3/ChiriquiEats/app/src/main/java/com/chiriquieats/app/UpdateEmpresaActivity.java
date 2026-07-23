package com.chiriquieats.app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chiriquieats.app.data.ChiriquiEatsDbHelper;

public class UpdateEmpresaActivity extends AppCompatActivity {

    public static final String EXTRA_RUC = "ruc_empresa";

    private ChiriquiEatsDbHelper dbHelper;
    private Spinner categoriaSpinner;
    private String ruc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_empresa);
        dbHelper = new ChiriquiEatsDbHelper(this);

        //Carga los datos de la empresa y las categorías del Spinner
        ruc = getIntent().getStringExtra(EXTRA_RUC);
        categoriaSpinner = findViewById(R.id.spinner_update_categoria_empresa);
        setupCategoriaSpinner();
        loadEmpresa();

        //Boton para actualizar los datos
        Button updateButton = findViewById(R.id.button_update_empresa);
        updateButton.setOnClickListener(view -> updateEmpresa());

        //Boton para eliminar la empresa
        Button deleteButton = findViewById(R.id.button_delete_empresa);
        deleteButton.setOnClickListener(view -> deleteEmpresa());
    }

    //Metodo para cargar los datos del spinner
    private void setupCategoriaSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.categorias_empresa_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriaSpinner.setAdapter(adapter);
    }

    //Metodo para cargar los datos de la empresa
    private void loadEmpresa() {
        if (ruc == null || ruc.isEmpty()) {
            Toast.makeText(this, "No se encontro la empresa", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String[] empresa = dbHelper.getEmpresaByRuc(ruc);
        if (empresa == null) {
            Toast.makeText(this, "No se encontro la empresa", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EditText rucInput = findViewById(R.id.input_update_ruc);
        rucInput.setText(empresa[0]);
        rucInput.setEnabled(false);
        findInput(R.id.input_update_nombre).setText(empresa[1]);
        findInput(R.id.input_update_direccion).setText(empresa[2]);
        findInput(R.id.input_update_telefono).setText(empresa[4]);
        findInput(R.id.input_update_correo).setText(empresa[5]);
        selectCategoria(empresa[3]);
    }

    //Metodo para utilizar la seleccion mediante spinner
    private void selectCategoria(String categoria) {
        for (int index = 0; index < categoriaSpinner.getCount(); index++) {
            if (categoriaSpinner.getItemAtPosition(index).toString().equals(categoria)) {
                categoriaSpinner.setSelection(index);
                return;
            }
        }
    }

    //Metodo para el boton de actualizar los datos de la empresa
    private void updateEmpresa() {
        String nombre = getInputText(R.id.input_update_nombre);
        String direccion = getInputText(R.id.input_update_direccion);
        String telefono = getInputText(R.id.input_update_telefono);
        String correo = getInputText(R.id.input_update_correo);
        String categoria = categoriaSpinner.getSelectedItem().toString();

        if (nombre.isEmpty() || direccion.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int rows = dbHelper.updateEmpresa(ruc, nombre, direccion, categoria, telefono, correo);
            if (rows > 0) {
                Toast.makeText(this, "Empresa actualizada", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "No se pudo actualizar la empresa", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception exception) {
            Toast.makeText(this, "No se pudo actualizar la empresa", Toast.LENGTH_SHORT).show();
        }
    }

    //Metodo para el boton de eliminar la empresa
    private void deleteEmpresa() {
        try {
            int rows = dbHelper.deleteEmpresa(ruc);
            if (rows > 0) {
                Toast.makeText(this, "Empresa borrada", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "No se pudo borrar la empresa", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception exception) {
            Toast.makeText(this, "No se puede borrar una empresa con pedidos asociados", Toast.LENGTH_LONG).show();
        }
    }

    private EditText findInput(int viewId) {
        return findViewById(viewId);
    }

    private String getInputText(int viewId) {
        return findInput(viewId).getText().toString().trim();
    }
}
