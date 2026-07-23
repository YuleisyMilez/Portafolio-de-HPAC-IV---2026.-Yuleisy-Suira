package com.example.chinoscafe;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chinoscafe.data.DatabaseHelper;
import com.example.chinoscafe.data.SyncManager;

import java.util.ArrayList;
import java.util.List;

public class AddMenuActivity extends AppCompatActivity {
    private static final String INGREDIENT_PLACEHOLDER = "Seleccionar ingrediente";

    private DatabaseHelper db;
    private long clienteId;
    private final List<SpinnerGroup> spinnerGroups = new ArrayList<>();
    private final List<DynamicRow> cheeseRows = new ArrayList<>();
    private final List<DynamicRow> toppingRows = new ArrayList<>();
    private final List<DynamicRow> extraRows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu);

        db = new DatabaseHelper(this, new SyncManager());
        clienteId = getIntent().getLongExtra(HomeActivity.EXTRA_CLIENTE_ID, -1);

        llenarSpinnersDesdeBaseDeDatos();
        configurarGruposDeIngredientes();
        findViewById(R.id.btn_createMenu).setOnClickListener(v -> guardarMenu());
    }

    private void llenarSpinnersDesdeBaseDeDatos() {
        addIngredientSpinner(-1, R.id.spn_masa_create, "masa");
        addIngredientSpinner(-1, R.id.spn_salsa_create, "salsa");
        addIngredientSpinner(R.id.layout_cheese1_create, R.id.spn_cheese1_create, "queso");
        addIngredientSpinner(R.id.layout_cheese2_create, R.id.spn_cheese2_create, "queso");
        addIngredientSpinner(R.id.layout_cheese3_create, R.id.spn_cheese3_create, "queso");
        addIngredientSpinner(R.id.layout_topping1_create, R.id.spn_topping1_create, "topping");
        addIngredientSpinner(R.id.layout_topping2_create, R.id.spn_topping2_create, "topping");
        addIngredientSpinner(R.id.layout_topping3_create, R.id.spn_topping3_create, "topping");
        addIngredientSpinner(R.id.layout_topping4_create, R.id.spn_topping4_create, "topping");
        addIngredientSpinner(R.id.layout_topping5_create, R.id.spn_topping5_create, "topping");
        addIngredientSpinner(R.id.layout_extra1_create, R.id.spn_extra1_create, "extra");
        addIngredientSpinner(R.id.layout_extra2_create, R.id.spn_extra2_create, "extra");
    }

    private void addIngredientSpinner(int layoutId, int spinnerId, String categoria) {
        Spinner spinner = findViewById(spinnerId);
        List<Object> options = new ArrayList<>();
        options.add(INGREDIENT_PLACEHOLDER);
        options.addAll(db.getIngredientesPorCategoria(categoria));

        IngredientAdapter adapter = new IngredientAdapter(this, options);
        spinner.setAdapter(adapter);
        spinnerGroups.add(new SpinnerGroup(layoutId, spinner));
    }

    private void configurarGruposDeIngredientes() {
        cheeseRows.add(new DynamicRow(R.id.layout_cheese1_create, R.id.spn_cheese1_create, R.id.btn_cheese1_create));
        cheeseRows.add(new DynamicRow(R.id.layout_cheese2_create, R.id.spn_cheese2_create, R.id.btn_cheese2_create));
        cheeseRows.add(new DynamicRow(R.id.layout_cheese3_create, R.id.spn_cheese3_create, R.id.btn_cheese3_create));

        toppingRows.add(new DynamicRow(R.id.layout_topping1_create, R.id.spn_topping1_create, R.id.btn_topping1_create));
        toppingRows.add(new DynamicRow(R.id.layout_topping2_create, R.id.spn_topping2_create, R.id.btn_topping2_create));
        toppingRows.add(new DynamicRow(R.id.layout_topping3_create, R.id.spn_topping3_create, R.id.btn_topping3_create));
        toppingRows.add(new DynamicRow(R.id.layout_topping4_create, R.id.spn_topping4_create, R.id.btn_topping4_create));
        toppingRows.add(new DynamicRow(R.id.layout_topping5_create, R.id.spn_topping5_create, R.id.btn_topping5_create));

        extraRows.add(new DynamicRow(R.id.layout_extra1_create, R.id.spn_extra1_create, R.id.btn_extra1_create));
        extraRows.add(new DynamicRow(R.id.layout_extra2_create, R.id.spn_extra2_create, R.id.btn_extra2_create));

        configurarGrupoDinamico(cheeseRows);
        configurarGrupoDinamico(toppingRows);
        configurarGrupoDinamico(extraRows);
    }

    private void configurarGrupoDinamico(List<DynamicRow> rows) {
        for (DynamicRow row : rows) {
            ImageButton button = findViewById(row.buttonId);
            button.setEnabled(true);
            button.setOnClickListener(v -> onDynamicButtonClick(rows, row));
        }
        actualizarGrupoDinamico(rows);
    }

    private void onDynamicButtonClick(List<DynamicRow> rows, DynamicRow clickedRow) {
        int lastVisibleIndex = getLastVisibleIndex(rows);
        int clickedIndex = rows.indexOf(clickedRow);
        if (clickedIndex == lastVisibleIndex && clickedIndex < rows.size() - 1) {
            findViewById(rows.get(clickedIndex + 1).layoutId).setVisibility(View.VISIBLE);
        } else {
            for (int i = clickedIndex; i < rows.size(); i++) {
                Spinner spinner = findViewById(rows.get(i).spinnerId);
                spinner.setSelection(0);
                if (i > 0) {
                    findViewById(rows.get(i).layoutId).setVisibility(View.GONE);
                }
            }
        }
        actualizarGrupoDinamico(rows);
    }

    private void actualizarGrupoDinamico(List<DynamicRow> rows) {
        int lastVisibleIndex = getLastVisibleIndex(rows);
        for (int i = 0; i < rows.size(); i++) {
            DynamicRow row = rows.get(i);
            View layout = findViewById(row.layoutId);
            ImageButton button = findViewById(row.buttonId);
            if (layout.getVisibility() != View.VISIBLE) {
                button.setImageResource(R.drawable.vc_add);
                continue;
            }

            boolean canAddNext = i == lastVisibleIndex && i < rows.size() - 1;
            button.setImageResource(canAddNext ? R.drawable.vc_add : R.drawable.vc_remove);
        }
    }

    private int getLastVisibleIndex(List<DynamicRow> rows) {
        int lastVisibleIndex = 0;
        for (int i = 0; i < rows.size(); i++) {
            if (findViewById(rows.get(i).layoutId).getVisibility() == View.VISIBLE) {
                lastVisibleIndex = i;
            }
        }
        return lastVisibleIndex;
    }

    private static class IngredientAdapter extends android.widget.ArrayAdapter<Object> {
        IngredientAdapter(android.content.Context context, List<Object> items) {
            super(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    items);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
    }

    private static class DynamicRow {
        final int layoutId;
        final int spinnerId;
        final int buttonId;

        DynamicRow(int layoutId, int spinnerId, int buttonId) {
            this.layoutId = layoutId;
            this.spinnerId = spinnerId;
            this.buttonId = buttonId;
        }
    }

    private void guardarMenu() {
        String nombre = getInputText(R.id.input_nombre_create);
        if (nombre.isEmpty()) {
            toast("Escribe un nombre para el menu");
            return;
        }

        List<DatabaseHelper.Ingredient> ingredientes = getIngredientesSeleccionados();
        long menuId = db.guardarMenu(clienteId, nombre, ingredientes);
        if (menuId > 0) {
            toast("Menu guardado");
            finish();
        }
    }

    private List<DatabaseHelper.Ingredient> getIngredientesSeleccionados() {
        List<DatabaseHelper.Ingredient> ingredientes = new ArrayList<>();
        for (SpinnerGroup group : spinnerGroups) {
            View row = group.layoutId == -1 ? null : findViewById(group.layoutId);
            boolean isVisible = row == null || row.getVisibility() == View.VISIBLE;
            if (isVisible && group.spinner.getSelectedItem() instanceof DatabaseHelper.Ingredient) {
                ingredientes.add((DatabaseHelper.Ingredient) group.spinner.getSelectedItem());
            }
        }
        return ingredientes;
    }

    private String getInputText(int id) {
        EditText editText = findViewById(id);
        return editText.getText().toString().trim();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private static class SpinnerGroup {
        final int layoutId;
        final Spinner spinner;

        SpinnerGroup(int layoutId, Spinner spinner) {
            this.layoutId = layoutId;
            this.spinner = spinner;
        }
    }
}
