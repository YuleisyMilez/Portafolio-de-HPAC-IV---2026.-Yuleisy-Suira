package com.chiriquieats.app.ui.pedidos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chiriquieats.app.AddPedidoActivity;
import com.chiriquieats.app.R;
import com.chiriquieats.app.ViewPedidoActivity;
import com.chiriquieats.app.data.ChiriquiEatsDbHelper;
import com.chiriquieats.app.data.SpinnerOption;
import com.chiriquieats.app.ui.TableViewRenderer;

public class PedidosFragment extends Fragment {

    private ChiriquiEatsDbHelper dbHelper;
    private Spinner empresaSpinner;
    private FrameLayout tableContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pedidos, container, false);
        dbHelper = new ChiriquiEatsDbHelper(requireContext());

        empresaSpinner = view.findViewById(R.id.spinner_empresa_pedidos);
        tableContainer = view.findViewById(R.id.pedidos_table_container);
        loadEmpresaSpinner();
        loadPedidosTable("");

        Button addPedidoButton = view.findViewById(R.id.button_add_pedido);
        addPedidoButton.setOnClickListener(buttonView -> {
            Intent intent = new Intent(requireContext(), AddPedidoActivity.class);
            startActivity(intent);
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (empresaSpinner != null) {
            loadEmpresaSpinner();
            loadPedidosTable(getSelectedEmpresaRuc());
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
                loadPedidosTable(selectedEmpresa.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadPedidosTable("");
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

    private void loadPedidosTable(String rucEmpresa) {
        if (tableContainer == null) {
            return;
        }

        TableViewRenderer.render(
                requireContext(),
                tableContainer,
                new String[]{"Empresa", "Menu", "Cliente"},
                dbHelper.getPedidosTableRows(rucEmpresa),
                "No hay pedidos registrados",
                1,
                row -> {
                    long codigoPedido = Long.parseLong(row[0]);
                    Intent intent = new Intent(requireContext(), ViewPedidoActivity.class);
                    intent.putExtra(ViewPedidoActivity.EXTRA_CODIGO_PEDIDO, codigoPedido);
                    startActivity(intent);
                }
        );
    }
}
