package com.chiriquieats.app.ui.menu;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.chiriquieats.app.AddMenuActivity;
import com.chiriquieats.app.R;
import com.chiriquieats.app.UpdateMenuActivity;
import com.chiriquieats.app.data.ChiriquiEatsDbHelper;
import com.chiriquieats.app.data.SpinnerOption;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class MenuFragment extends Fragment {

    private ChiriquiEatsDbHelper dbHelper;
    private Spinner empresaSpinner;
    private FrameLayout tableContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        dbHelper = new ChiriquiEatsDbHelper(requireContext());

        empresaSpinner = view.findViewById(R.id.spinner_empresa_menu);
        tableContainer = view.findViewById(R.id.menu_table_container);
        loadEmpresaSpinner();
        loadMenusTable("");

        Button addMenuButton = view.findViewById(R.id.button_add_menu);
        addMenuButton.setOnClickListener(buttonView -> {
            Intent intent = new Intent(requireContext(), AddMenuActivity.class);
            startActivity(intent);
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (empresaSpinner != null) {
            loadEmpresaSpinner();
            loadMenusTable(getSelectedEmpresaRuc());
        }
    }

    private void loadEmpresaSpinner() {
        ArrayAdapter<SpinnerOption> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                dbHelper.getEmpresaOptions()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        empresaSpinner.setAdapter(adapter);
        empresaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerOption selectedEmpresa = (SpinnerOption) parent.getItemAtPosition(position);
                loadMenusTable(selectedEmpresa.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadMenusTable("");
            }
        });
    }

    private String getSelectedEmpresaRuc() {
        Object selectedItem = empresaSpinner.getSelectedItem();
        if (selectedItem instanceof SpinnerOption) {
            return ((SpinnerOption) selectedItem).getValue();
        }
        return "";
    }

    private void loadMenusTable(String rucEmpresa) {
        if (tableContainer == null) {
            return;
        }

        tableContainer.removeAllViews();

        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout cardList = new LinearLayout(requireContext());
        cardList.setOrientation(LinearLayout.VERTICAL);
        cardList.setPadding(dp(12), dp(12), dp(12), dp(12));

        List<String[]> menus = dbHelper.getMenusTableRows(rucEmpresa);
        if (menus.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("No hay menus registrados");
            emptyView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ce_muted));
            emptyView.setTextSize(15);
            emptyView.setPadding(dp(12), dp(12), dp(12), dp(12));
            cardList.addView(emptyView);
        } else {
            for (String[] menu : menus) {
                cardList.addView(createMenuCard(menu));
            }
        }

        scrollView.addView(cardList);
        tableContainer.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    private MaterialCardView createMenuCard(String[] menu) {
        MaterialCardView cardView = new MaterialCardView(requireContext());
        cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ce_surface));
        cardView.setRadius(dp(8));
        cardView.setStrokeWidth(dp(1));
        cardView.setStrokeColor(0xFFE1E8E4);
        cardView.setCardElevation(dp(1));
        cardView.setClickable(true);
        cardView.setFocusable(true);
        cardView.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), UpdateMenuActivity.class);
            intent.putExtra(UpdateMenuActivity.EXTRA_CODIGO_MENU, menu[0]);
            startActivity(intent);
        });

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        cardView.setLayoutParams(cardParams);

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(14), dp(16), dp(14));

        TextView title = new TextView(requireContext());
        title.setText(menu[2]);
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.ce_text));
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        content.addView(title);

        TextView details = new TextView(requireContext());
        details.setText("Empresa: " + menu[1]
                + "\nContenido: " + menu[3]
                + "\nPrecio: $" + menu[4]);
        details.setTextColor(ContextCompat.getColor(requireContext(), R.color.ce_muted));
        details.setTextSize(14);
        details.setLineSpacing(dp(2), 1.0f);
        details.setPadding(0, dp(8), 0, 0);
        content.addView(details);

        cardView.addView(content);
        return cardView;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
