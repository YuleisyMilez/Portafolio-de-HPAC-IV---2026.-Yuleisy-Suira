package com.chiriquieats.app.ui.empresas;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.chiriquieats.app.AddEmpresaActivity;
import com.chiriquieats.app.R;
import com.chiriquieats.app.UpdateEmpresaActivity;
import com.chiriquieats.app.data.ChiriquiEatsDbHelper;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class EmpresasFragment extends Fragment {

    private ChiriquiEatsDbHelper dbHelper;
    private FrameLayout tableContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_empresas, container, false);
        dbHelper = new ChiriquiEatsDbHelper(requireContext());
        tableContainer = view.findViewById(R.id.empresas_table_container);

        Button addEmpresaButton = view.findViewById(R.id.button_add_empresa);
        addEmpresaButton.setOnClickListener(buttonView -> {
            Intent intent = new Intent(requireContext(), AddEmpresaActivity.class);
            startActivity(intent);
        });

        loadEmpresasTable();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tableContainer != null) {
            loadEmpresasTable();
        }
    }

    private void loadEmpresasTable() {
        tableContainer.removeAllViews();

        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout cardList = new LinearLayout(requireContext());
        cardList.setOrientation(LinearLayout.VERTICAL);
        cardList.setPadding(dp(12), dp(12), dp(12), dp(12));

        List<String[]> empresas = dbHelper.getEmpresasTableRows();
        if (empresas.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("No hay empresas registradas");
            emptyView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ce_muted));
            emptyView.setTextSize(15);
            emptyView.setPadding(dp(12), dp(12), dp(12), dp(12));
            cardList.addView(emptyView);
        } else {
            for (String[] empresa : empresas) {
                cardList.addView(createEmpresaCard(empresa));
            }
        }

        scrollView.addView(cardList);
        tableContainer.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    private MaterialCardView createEmpresaCard(String[] empresa) {
        MaterialCardView cardView = new MaterialCardView(requireContext());
        cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ce_surface));
        cardView.setRadius(dp(8));
        cardView.setStrokeWidth(dp(1));
        cardView.setStrokeColor(0xFFE1E8E4);
        cardView.setCardElevation(dp(1));
        cardView.setClickable(true);
        cardView.setFocusable(true);
        cardView.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), UpdateEmpresaActivity.class);
            intent.putExtra(UpdateEmpresaActivity.EXTRA_RUC, empresa[0]);
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
        title.setText(empresa[1]);
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.ce_text));
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        content.addView(title);

        TextView details = new TextView(requireContext());
        details.setText("Direccion: " + empresa[2]
                + "\nTelefono: " + empresa[4]
                + "\nCorreo: " + empresa[5]
                + "\nCategoria: " + empresa[3]);
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
