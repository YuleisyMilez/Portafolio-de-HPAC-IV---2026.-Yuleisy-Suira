package com.chiriquieats.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.chiriquieats.app.ui.empresas.EmpresasFragment;
import com.chiriquieats.app.ui.menu.MenuFragment;
import com.chiriquieats.app.ui.pedidos.PedidosFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Identificacion del BottomNavigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = getFragmentForItem(item.getItemId());
            showFragment(selectedFragment);
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_empresas);
        }
    }

    //Navegacion dentro del bottomNavigation
    @NonNull
    private Fragment getFragmentForItem(int itemId) {
        if (itemId == R.id.nav_menu) {
            return new MenuFragment();
        }
        if (itemId == R.id.nav_pedidos) {
            return new PedidosFragment();
        }
        return new EmpresasFragment();
    }

    //Metodo para mostrar los frangmentos
    private void showFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
