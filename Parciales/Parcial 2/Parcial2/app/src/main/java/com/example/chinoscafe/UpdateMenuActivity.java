package com.example.chinoscafe;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chinoscafe.data.DatabaseHelper;
import com.example.chinoscafe.data.SyncManager;

import java.util.ArrayList;
import java.util.List;

public class UpdateMenuActivity extends AppCompatActivity {
    public static final String EXTRA_MENU_ID = "menu_id";
    private static final String PLACEHOLDER = "Seleccionar ingrediente";

    private DatabaseHelper db;
    private long menuId;
    private final List<SpinnerGroup> spinnerGroups = new ArrayList<>();
    private final List<DynamicRow> cheeseRows = new ArrayList<>();
    private final List<DynamicRow> toppingRows = new ArrayList<>();
    private final List<DynamicRow> extraRows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_menu);
        db = new DatabaseHelper(this, new SyncManager());
        menuId = getIntent().getLongExtra(EXTRA_MENU_ID, -1);

        llenarSpinners();
        configurarGrupos();
        cargarMenu();
        findViewById(R.id.btn_updateMenu).setOnClickListener(v -> actualizarMenu());
        findViewById(R.id.btn_deleteMenu).setOnClickListener(v -> eliminarMenu());
    }

    private void llenarSpinners() {
        addIngredientSpinner(-1, R.id.spn_masa_update, "masa");
        addIngredientSpinner(-1, R.id.spn_salsa_update, "salsa");
        addIngredientSpinner(R.id.layout_cheese1_update, R.id.spn_cheese1_update, "queso");
        addIngredientSpinner(R.id.layout_cheese2_update, R.id.spn_cheese2_update, "queso");
        addIngredientSpinner(R.id.layout_cheese3_update, R.id.spn_cheese3_update, "queso");
        addIngredientSpinner(R.id.layout_topping1_update, R.id.spn_topping1_update, "topping");
        addIngredientSpinner(R.id.layout_topping2_update, R.id.spn_topping2_update, "topping");
        addIngredientSpinner(R.id.layout_topping3_update, R.id.spn_topping3_update, "topping");
        addIngredientSpinner(R.id.layout_topping4_update, R.id.spn_topping4_update, "topping");
        addIngredientSpinner(R.id.layout_topping5_update, R.id.spn_topping5_update, "topping");
        addIngredientSpinner(R.id.layout_extra1_update, R.id.spn_extra1_update, "extra");
        addIngredientSpinner(R.id.layout_extra2_update, R.id.spn_extra2_update, "extra");
    }

    private void addIngredientSpinner(int layoutId, int spinnerId, String categoria) {
        Spinner spinner = findViewById(spinnerId);
        List<Object> options = new ArrayList<>();
        options.add(PLACEHOLDER);
        options.addAll(db.getIngredientesPorCategoria(categoria));
        ArrayAdapter<Object> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinnerGroups.add(new SpinnerGroup(layoutId, spinner));
    }

    private void configurarGrupos() {
        cheeseRows.add(new DynamicRow(R.id.layout_cheese1_update, R.id.spn_cheese1_update, R.id.btn_cheese1_update));
        cheeseRows.add(new DynamicRow(R.id.layout_cheese2_update, R.id.spn_cheese2_update, R.id.btn_cheese2_update));
        cheeseRows.add(new DynamicRow(R.id.layout_cheese3_update, R.id.spn_cheese3_update, R.id.btn_cheese3_update));
        toppingRows.add(new DynamicRow(R.id.layout_topping1_update, R.id.spn_topping1_update, R.id.btn_topping1_update));
        toppingRows.add(new DynamicRow(R.id.layout_topping2_update, R.id.spn_topping2_update, R.id.btn_topping2_update));
        toppingRows.add(new DynamicRow(R.id.layout_topping3_update, R.id.spn_topping3_update, R.id.btn_topping3_update));
        toppingRows.add(new DynamicRow(R.id.layout_topping4_update, R.id.spn_topping4_update, R.id.btn_topping4_update));
        toppingRows.add(new DynamicRow(R.id.layout_topping5_update, R.id.spn_topping5_update, R.id.btn_topping5_update));
        extraRows.add(new DynamicRow(R.id.layout_extra1_update, R.id.spn_extra1_update, R.id.btn_extra1_update));
        extraRows.add(new DynamicRow(R.id.layout_extra2_update, R.id.spn_extra2_update, R.id.btn_extra2_update));
        configurarGrupo(cheeseRows);
        configurarGrupo(toppingRows);
        configurarGrupo(extraRows);
    }

    private void configurarGrupo(List<DynamicRow> rows) {
        for (DynamicRow row : rows) {
            ImageButton button = findViewById(row.buttonId);
            button.setEnabled(true);
            button.setOnClickListener(v -> onDynamicButtonClick(rows, row));
        }
        actualizarGrupo(rows);
    }

    private void cargarMenu() {
        DatabaseHelper.MenuDetalle detalle = db.getMenuDetalle(menuId);
        if (detalle == null) {
            toast("Menu no encontrado");
            finish();
            return;
        }
        ((EditText) findViewById(R.id.input_nombre_update)).setText(detalle.menu.nombre);
        for (DatabaseHelper.Ingredient ingrediente : detalle.ingredientes) {
            seleccionarIngrediente(ingrediente);
        }
        actualizarGrupo(cheeseRows);
        actualizarGrupo(toppingRows);
        actualizarGrupo(extraRows);
    }

    private void seleccionarIngrediente(DatabaseHelper.Ingredient ingrediente) {
        List<DynamicRow> rows = getRowsPorCategoria(ingrediente.categoria);
        if (rows == null) {
            selectSpinnerByIngredient(getSpinnerPorCategoriaFija(ingrediente.categoria), ingrediente.id);
            return;
        }
        for (DynamicRow row : rows) {
            Spinner spinner = findViewById(row.spinnerId);
            if (spinner.getSelectedItemPosition() == 0) {
                findViewById(row.layoutId).setVisibility(View.VISIBLE);
                selectSpinnerByIngredient(spinner, ingrediente.id);
                return;
            }
        }
    }

    private Spinner getSpinnerPorCategoriaFija(String categoria) {
        if ("masa".equals(categoria)) {
            return findViewById(R.id.spn_masa_update);
        }
        if ("salsa".equals(categoria)) {
            return findViewById(R.id.spn_salsa_update);
        }
        return null;
    }

    private List<DynamicRow> getRowsPorCategoria(String categoria) {
        if ("queso".equals(categoria)) {
            return cheeseRows;
        }
        if ("topping".equals(categoria)) {
            return toppingRows;
        }
        if ("extra".equals(categoria)) {
            return extraRows;
        }
        return null;
    }

    private void selectSpinnerByIngredient(Spinner spinner, long ingredientId) {
        if (spinner == null) {
            return;
        }
        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item instanceof DatabaseHelper.Ingredient && ((DatabaseHelper.Ingredient) item).id == ingredientId) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void onDynamicButtonClick(List<DynamicRow> rows, DynamicRow clickedRow) {
        int lastVisibleIndex = getLastVisibleIndex(rows);
        int clickedIndex = rows.indexOf(clickedRow);
        if (clickedIndex == lastVisibleIndex && clickedIndex < rows.size() - 1) {
            findViewById(rows.get(clickedIndex + 1).layoutId).setVisibility(View.VISIBLE);
        } else {
            for (int i = clickedIndex; i < rows.size(); i++) {
                ((Spinner) findViewById(rows.get(i).spinnerId)).setSelection(0);
                if (i > 0) {
                    findViewById(rows.get(i).layoutId).setVisibility(View.GONE);
                }
            }
        }
        actualizarGrupo(rows);
    }

    private void actualizarGrupo(List<DynamicRow> rows) {
        int lastVisibleIndex = getLastVisibleIndex(rows);
        for (int i = 0; i < rows.size(); i++) {
            DynamicRow row = rows.get(i);
            View layout = findViewById(row.layoutId);
            ImageButton button = findViewById(row.buttonId);
            boolean canAddNext = layout.getVisibility() == View.VISIBLE && i == lastVisibleIndex && i < rows.size() - 1;
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

    private void actualizarMenu() {
        String nombre = ((EditText) findViewById(R.id.input_nombre_update)).getText().toString().trim();
        if (nombre.isEmpty()) {
            toast("Escribe un nombre para el menu");
            return;
        }
        if (db.actualizarMenu(menuId, nombre, getIngredientesSeleccionados())) {
            toast("Menu actualizado");
            finish();
        }
    }

    private void eliminarMenu() {
        if (db.eliminarMenu(menuId)) {
            toast("Menu eliminado");
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
}
