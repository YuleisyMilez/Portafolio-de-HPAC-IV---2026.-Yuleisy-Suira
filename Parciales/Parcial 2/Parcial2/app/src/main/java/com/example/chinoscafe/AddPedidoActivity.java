package com.example.chinoscafe;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chinoscafe.data.DatabaseHelper;
import com.example.chinoscafe.data.SyncManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddPedidoActivity extends AppCompatActivity {
    private static final double ENVIO_FIJO = 2.50;
    private static final int MAX_CANTIDAD_MENU = 20;
    private static final String MENU_PLACEHOLDER = "Seleccionar menu";

    private DatabaseHelper db;
    private long clienteId;
    private Spinner spnMenu1;
    private Spinner spnMenu2;
    private Spinner spnMenu3;
    private EditText qtyMenu1;
    private EditText qtyMenu2;
    private EditText qtyMenu3;
    private boolean ajustandoCantidad;
    private final List<DynamicRow> menuRows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(this, new SyncManager());
        clienteId = getIntent().getLongExtra(HomeActivity.EXTRA_CLIENTE_ID, -1);

        List<DatabaseHelper.Menu> menus = db.getMenusPorCliente(clienteId);
        if (menus.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Sin menus")
                    .setMessage("Primero crea al menos un menu.")
                    .setPositiveButton("Aceptar", (dialog, which) -> finish())
                    .show();
            return;
        }

        setContentView(R.layout.activity_add_pedido);
        llenarSpinnersDesdeBaseDeDatos(menus);
        configurarCamposCantidad();
        configurarGrupoDinamicoMenus();
        setText(R.id.et_envio, formatMoney(ENVIO_FIJO));
        findViewById(R.id.btn_createPedido).setOnClickListener(v -> guardarPedido());
    }

    private void llenarSpinnersDesdeBaseDeDatos(List<DatabaseHelper.Menu> menus) {
        spnMenu1 = setupMenuSpinner(R.id.spn_menu1_pedido, menus);
        spnMenu2 = setupMenuSpinner(R.id.spn_menu2_pedido, menus);
        spnMenu3 = setupMenuSpinner(R.id.spn_menu3_pedido, menus);
    }

    private Spinner setupMenuSpinner(int spinnerId, List<DatabaseHelper.Menu> menus) {
        Spinner spinner = findViewById(spinnerId);
        List<Object> options = new ArrayList<>();
        options.add(MENU_PLACEHOLDER);
        options.addAll(menus);

        android.widget.ArrayAdapter<Object> adapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                actualizarSubtotal();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                actualizarSubtotal();
            }
        });
        return spinner;
    }

    private void configurarCamposCantidad() {
        qtyMenu1 = findViewById(R.id.et_menu1_pedido);
        qtyMenu2 = findViewById(R.id.et_menu2_pedido);
        qtyMenu3 = findViewById(R.id.et_menu3_pedido);
        addSubtotalWatcher(qtyMenu1);
        addSubtotalWatcher(qtyMenu2);
        addSubtotalWatcher(qtyMenu3);
    }

    private void configurarGrupoDinamicoMenus() {
        menuRows.add(new DynamicRow(R.id.layout_menu1_pedido, R.id.spn_menu1_pedido, R.id.et_menu1_pedido, R.id.btn_menu1_pedido));
        menuRows.add(new DynamicRow(R.id.layout_menu2_pedido, R.id.spn_menu2_pedido, R.id.et_menu2_pedido, R.id.btn_menu2_pedido));
        menuRows.add(new DynamicRow(R.id.layout_menu3_pedido, R.id.spn_menu3_pedido, R.id.et_menu3_pedido, R.id.btn_menu3_pedido));

        for (DynamicRow row : menuRows) {
            ImageButton button = findViewById(row.buttonId);
            button.setEnabled(true);
            button.setOnClickListener(v -> onDynamicButtonClick(row));
        }
        actualizarGrupoDinamicoMenus();
    }

    private void onDynamicButtonClick(DynamicRow clickedRow) {
        int lastVisibleIndex = getLastVisibleIndex();
        int clickedIndex = menuRows.indexOf(clickedRow);
        if (clickedIndex == lastVisibleIndex && clickedIndex < menuRows.size() - 1) {
            findViewById(menuRows.get(clickedIndex + 1).layoutId).setVisibility(View.VISIBLE);
        } else {
            for (int i = clickedIndex; i < menuRows.size(); i++) {
                Spinner spinner = findViewById(menuRows.get(i).spinnerId);
                EditText quantity = findViewById(menuRows.get(i).quantityId);
                spinner.setSelection(0);
                quantity.setText("");
                if (i > 0) {
                    findViewById(menuRows.get(i).layoutId).setVisibility(View.GONE);
                }
            }
        }
        actualizarGrupoDinamicoMenus();
        actualizarSubtotal();
    }

    private void actualizarGrupoDinamicoMenus() {
        int lastVisibleIndex = getLastVisibleIndex();
        for (int i = 0; i < menuRows.size(); i++) {
            DynamicRow row = menuRows.get(i);
            View layout = findViewById(row.layoutId);
            ImageButton button = findViewById(row.buttonId);
            if (layout.getVisibility() != View.VISIBLE) {
                button.setImageResource(R.drawable.vc_add);
                continue;
            }

            boolean canAddNext = i == lastVisibleIndex && i < menuRows.size() - 1;
            button.setImageResource(canAddNext ? R.drawable.vc_add : R.drawable.vc_remove);
        }
    }

    private int getLastVisibleIndex() {
        int lastVisibleIndex = 0;
        for (int i = 0; i < menuRows.size(); i++) {
            if (findViewById(menuRows.get(i).layoutId).getVisibility() == View.VISIBLE) {
                lastVisibleIndex = i;
            }
        }
        return lastVisibleIndex;
    }

    private void guardarPedido() {
        String direccion = getInputText(R.id.et_direccion);
        String telefono = getInputText(R.id.et_contact);
        if (direccion.isEmpty() || telefono.isEmpty()) {
            toast("Completa direccion y telefono");
            return;
        }

        List<DatabaseHelper.OrderItem> items = getPedidoItems();
        if (items.isEmpty()) {
            toast("Agrega al menos un menu con cantidad valida");
            return;
        }

        double subtotal = calcularSubtotal(items);
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        long pedidoId = db.guardarPedido(clienteId, direccion, telefono, items, subtotal, ENVIO_FIJO, fecha, "Pendiente de pago");

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_PEDIDO_ID, pedidoId);
        intent.putExtra(PaymentActivity.EXTRA_TOTAL, subtotal + ENVIO_FIJO);
        startActivity(intent);
        finish();
    }

    private List<DatabaseHelper.OrderItem> getPedidoItems() {
        List<DatabaseHelper.OrderItem> items = new ArrayList<>();
        addPedidoItem(items, R.id.layout_menu1_pedido, spnMenu1, qtyMenu1);
        addPedidoItem(items, R.id.layout_menu2_pedido, spnMenu2, qtyMenu2);
        addPedidoItem(items, R.id.layout_menu3_pedido, spnMenu3, qtyMenu3);
        return items;
    }

    private void addPedidoItem(List<DatabaseHelper.OrderItem> items, int rowId, Spinner spinner, EditText qty) {
        View row = findViewById(rowId);
        if (row.getVisibility() != View.VISIBLE) {
            return;
        }
        int cantidad = parseCantidad(qty.getText().toString());
        if (cantidad <= 0 || !(spinner.getSelectedItem() instanceof DatabaseHelper.Menu)) {
            return;
        }
        items.add(new DatabaseHelper.OrderItem((DatabaseHelper.Menu) spinner.getSelectedItem(), cantidad));
    }

    private double calcularSubtotal(List<DatabaseHelper.OrderItem> items) {
        double subtotal = 0;
        for (DatabaseHelper.OrderItem item : items) {
            subtotal += item.menu.precio * item.cantidad;
        }
        return subtotal;
    }

    private void addSubtotalWatcher(EditText target) {
        target.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                limitarCantidad(target);
                actualizarSubtotal();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void actualizarSubtotal() {
        setText(R.id.et_subtotal, formatMoney(calcularSubtotal(getPedidoItems())));
    }

    private void limitarCantidad(EditText target) {
        if (ajustandoCantidad) {
            return;
        }

        int cantidad = parseCantidad(target.getText().toString());
        if (cantidad > MAX_CANTIDAD_MENU) {
            ajustandoCantidad = true;
            target.setText(String.valueOf(MAX_CANTIDAD_MENU));
            target.setSelection(target.getText().length());
            ajustandoCantidad = false;
            toast("Maximo " + MAX_CANTIDAD_MENU + " unidades por menu");
        }
    }

    private int parseCantidad(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getInputText(int id) {
        EditText editText = findViewById(id);
        return editText.getText().toString().trim();
    }

    private void setText(int id, String value) {
        EditText editText = findViewById(id);
        editText.setText(value);
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatMoney(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private static class DynamicRow {
        final int layoutId;
        final int spinnerId;
        final int quantityId;
        final int buttonId;

        DynamicRow(int layoutId, int spinnerId, int quantityId, int buttonId) {
            this.layoutId = layoutId;
            this.spinnerId = spinnerId;
            this.quantityId = quantityId;
            this.buttonId = buttonId;
        }
    }
}
