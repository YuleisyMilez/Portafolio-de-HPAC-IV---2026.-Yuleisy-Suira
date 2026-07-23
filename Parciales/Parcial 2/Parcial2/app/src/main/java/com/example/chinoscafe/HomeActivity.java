package com.example.chinoscafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.chinoscafe.data.DatabaseHelper;
import com.example.chinoscafe.data.SyncManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    public static final String EXTRA_CLIENTE_ID = "cliente_id";

    private boolean vieneDePedidoDetalle = false;
    private DatabaseHelper db;
    private long clienteId;
    private int selectedNavItem = R.id.nav_menus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(this, new SyncManager());
        clienteId = getIntent().getLongExtra(EXTRA_CLIENTE_ID, -1);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.scarlet));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.snow));
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            selectedNavItem = item.getItemId();
            if (selectedNavItem == R.id.nav_menus) {
                db.syncMenusDesdeServidor();
                mostrarMenus();
                return true;
            }
            if (selectedNavItem == R.id.nav_pedidos) {
                //db.syncPedidosDesdeServidor();
                mostrarPedidos();
                return true;
            }
            return false;
        });
        bottomNavigation.setSelectedItemId(R.id.nav_menus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (clienteId == -1 || findViewById(R.id.fragment_container) == null) {
            return;
        }

        if (vieneDePedidoDetalle) {
            vieneDePedidoDetalle = false;
            mostrarPedidos(); // solo refresca UI sin sincronizar
            return;
        }

        if (selectedNavItem == R.id.nav_pedidos) {
            //db.syncPedidosDesdeServidor();
            mostrarPedidos();
        } else {
            db.syncMenusDesdeServidor();
            mostrarMenus();
        }
    }

    private void mostrarMenus() {
        FrameLayout container = findViewById(R.id.fragment_container);
        View view = LayoutInflater.from(this).inflate(R.layout.fragment_menu, container, false);
        container.removeAllViews();
        container.addView(view);

        LinearLayout list = crearLista(view.findViewById(R.id.menus_container));
        List<DatabaseHelper.Menu> menus = db.getMenusPorCliente(clienteId);
        if (menus.isEmpty()) {
            addRow(list, "No hay menus guardados todavia.");
        } else {
            for (DatabaseHelper.Menu menu : menus) {
                MaterialCardView card = crearCard();
                LinearLayout content = crearCardContent();
                content.addView(crearTitulo(menu.nombre));
                content.addView(crearDetalle("Precio: $" + formatMoney(menu.precio)));
                card.addView(content);
                card.setOnClickListener(v -> {
                    Intent intent = new Intent(this, UpdateMenuActivity.class);
                    intent.putExtra(UpdateMenuActivity.EXTRA_MENU_ID, menu.id);
                    startActivity(intent);
                });
                list.addView(card);
            }
        }

        view.findViewById(R.id.btn_addMenu).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMenuActivity.class);
            intent.putExtra(EXTRA_CLIENTE_ID, clienteId);
            startActivity(intent);
        });
    }

    private void mostrarPedidos() {
        FrameLayout container = findViewById(R.id.fragment_container);
        View view = LayoutInflater.from(this).inflate(R.layout.fragment_pedido, container, false);
        container.removeAllViews();
        container.addView(view);

        LinearLayout list = crearLista(view.findViewById(R.id.pedidos_container));
        List<DatabaseHelper.PedidoResumen> pedidos = db.getPedidosPorCliente(clienteId);
        if (pedidos.isEmpty()) {
            addRow(list, "No hay pedidos guardados todavia.");
        } else {
            for (DatabaseHelper.PedidoResumen pedido : pedidos) {
                MaterialCardView card = crearCard();
                LinearLayout content = crearCardContent();
                content.addView(crearTitulo("Pedido #" + pedido.id));
                content.addView(crearDetalle("Fecha: " + pedido.fecha));
                content.addView(crearDetalle("Monto pagado: $" + formatMoney(pedido.montoPago)));
                content.addView(crearDetalle("Estado: " + pedido.estado));
                card.addView(content);
                card.setOnClickListener(v -> {
                    vieneDePedidoDetalle = true;
                    Intent intent = new Intent(this, ViewPedidoActivity.class);
                    intent.putExtra(ViewPedidoActivity.EXTRA_PEDIDO_ID, pedido.id);
                    startActivity(intent);
                });
                list.addView(card);
            }
        }

        view.findViewById(R.id.btn_addPedido).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPedidoActivity.class);
            intent.putExtra(EXTRA_CLIENTE_ID, clienteId);
            startActivity(intent);
        });
    }

    private LinearLayout crearLista(FrameLayout frame) {
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(16, 16, 16, 16);
        frame.removeAllViews();
        frame.addView(list);
        return list;
    }

    private void addRow(LinearLayout list, String text) {
        TextView row = new TextView(this);
        row.setText(text);
        row.setTextSize(16);
        row.setPadding(12, 12, 12, 12);
        list.addView(row);
    }

    private MaterialCardView crearCard() {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 12);
        card.setLayoutParams(params);
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.snow));
        card.setStrokeColor(ContextCompat.getColor(this, R.color.pinky));
        card.setStrokeWidth(2);
        card.setRadius(8);
        card.setClickable(true);
        card.setFocusable(true);
        return card;
    }

    private LinearLayout crearCardContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(18, 16, 18, 16);
        return content;
    }

    private TextView crearTitulo(String text) {
        TextView title = new TextView(this);
        title.setText(text);
        title.setTextColor(ContextCompat.getColor(this, R.color.mahogany));
        title.setTextSize(18);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        return title;
    }

    private TextView crearDetalle(String text) {
        TextView detail = new TextView(this);
        detail.setText(text);
        detail.setTextColor(ContextCompat.getColor(this, R.color.black));
        detail.setTextSize(15);
        detail.setPadding(0, 4, 0, 0);
        return detail;
    }

    private String formatMoney(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
