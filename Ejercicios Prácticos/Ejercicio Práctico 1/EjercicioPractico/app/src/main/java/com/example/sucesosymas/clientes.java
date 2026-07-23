package com.example.sucesosymas;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.net.CacheRequest;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link clientes#newInstance} factory method to
 * create an instance of this fragment.
 */
public class clientes extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    contacto contacto = new contacto();

    public clientes() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment clientes.
     */
    // TODO: Rename and change types and number of parameters
    public static clientes newInstance(String param1, String param2) {
        clientes fragment = new clientes();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_clientes, container, false);

        CardView clickTigo = view.findViewById(R.id.clickTigo);
        CardView clickSup99 = view.findViewById(R.id.clickSup99);
        CardView clickSub = view.findViewById(R.id.clickSub);
        CardView clickAthenas = view.findViewById(R.id.clickAthenas);

        //Metodo para abrir cliente tigo
        clickTigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contacto.abrirLink(
                        requireContext(),
                        "https://www.tigo.com.pa", "https://www.tigo.com.pa"
                );
            }
        });

        //Metodo para abrir cliente super99
        clickSup99.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contacto.abrirLink(
                        requireContext(),
                        "https://www.super99.com", "https://www.super99.com"
                );
            }
        });

        //Metodo para abrir cliente subasta ganadera
        clickSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contacto.abrirLink(
                        requireContext(),
                        "https://subastaganadera.com", "https://subastaganadera.com"
                );
            }
        });

        //Metodo para abrir cliente athenas
        clickAthenas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contacto.abrirLink(
                        requireContext(),
                        "instagram://user?username=athanasiadischiriqui", "https://www.instagram.com/athanasiadischiriqui"
                );
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}