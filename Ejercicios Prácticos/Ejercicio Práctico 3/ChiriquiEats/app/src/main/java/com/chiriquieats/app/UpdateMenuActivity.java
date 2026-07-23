package com.chiriquieats.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chiriquieats.app.data.ChiriquiEatsDbHelper;

public class UpdateMenuActivity extends AppCompatActivity {

    public static final String EXTRA_CODIGO_MENU = "codigo_menu";

    private ChiriquiEatsDbHelper dbHelper;
    private String codigoMenu;
    private String rucEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_menu);
        dbHelper = new ChiriquiEatsDbHelper(this);

        //Cargar los datos del menu y el spinner
        codigoMenu = getIntent().getStringExtra(EXTRA_CODIGO_MENU);
        loadMenu();

        //Boton para actualizar los datos del menu
        Button updateButton = findViewById(R.id.button_update_menu);
        updateButton.setOnClickListener(view -> updateMenu());

        //Boton para eliminar los datos del menu
        Button deleteButton = findViewById(R.id.button_delete_menu);
        deleteButton.setOnClickListener(view -> deleteMenu());
    }

    //Metodo para cargar los datos del menu en los campos
    private void loadMenu() {
        if (codigoMenu == null || codigoMenu.isEmpty()) {
            Toast.makeText(this, "No se encontro el menu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String[] menu = dbHelper.getMenuByCodigo(codigoMenu);
        if (menu == null) {
            Toast.makeText(this, "No se encontro el menu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EditText codigoInput = findViewById(R.id.input_update_codigo_menu);
        codigoInput.setText(menu[0]);
        codigoInput.setEnabled(false);
        rucEmpresa = menu[1];
        EditText empresaInput = findViewById(R.id.input_update_menu_empresa);
        empresaInput.setText(getEmpresaLabel(rucEmpresa));
        empresaInput.setEnabled(false);
        findInput(R.id.input_update_nombre_menu).setText(menu[2]);
        findInput(R.id.input_update_contenido_menu).setText(menu[3]);
        findInput(R.id.input_update_precio_menu).setText(menu[4]);
    }

    private String getEmpresaLabel(String rucEmpresa) {
        String[] empresa = dbHelper.getEmpresaByRuc(rucEmpresa);
        if (empresa == null) {
            return rucEmpresa;
        }
        return empresa[1] + " - " + empresa[0];
    }

    //Metodo para el funcionamiento del boton de actualizar los datos del menu
    private void updateMenu() {
        String nombre = getInputText(R.id.input_update_nombre_menu);
        String contenido = getInputText(R.id.input_update_contenido_menu);
        String precioText = getInputText(R.id.input_update_precio_menu);

        if (rucEmpresa == null || rucEmpresa.isEmpty()
                || nombre.isEmpty() || contenido.isEmpty() || precioText.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double precio = Double.parseDouble(precioText);
            int rows = dbHelper.updateMenu(
                    codigoMenu,
                    rucEmpresa,
                    nombre,
                    contenido,
                    precio
            );
            if (rows > 0) {
                Toast.makeText(this, "Menu actualizado", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "No se pudo actualizar el menu", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException exception) {
            Toast.makeText(this, "Ingresa un precio valido", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Toast.makeText(this, "No se pudo actualizar el menu", Toast.LENGTH_SHORT).show();
        }
    }

    //Metodo para el funcionamiento del boton de eliminar el menu
    private void deleteMenu() {
        try {
            int rows = dbHelper.deleteMenu(codigoMenu);
            if (rows > 0) {
                Toast.makeText(this, "Menu borrado", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "No se pudo borrar el menu", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception exception) {
            Toast.makeText(this, "No se puede borrar un menu con pedidos asociados", Toast.LENGTH_LONG).show();
        }
    }

    private EditText findInput(int viewId) {
        return findViewById(viewId);
    }

    private String getInputText(int viewId) {
        return findInput(viewId).getText().toString().trim();
    }
}
