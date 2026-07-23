package com.example.sucesosymas;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link inicio#newInstance} factory method to
 * create an instance of this fragment.
 */
public class inicio extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    contacto noticias = new contacto();

    public inicio() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment inicio.
     */
    // TODO: Rename and change types and number of parameters
    public static inicio newInstance(String param1, String param2) {
        inicio fragment = new inicio();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        Button butVerMas = view.findViewById(R.id.butVerMas);
        CardView cardNotP1 = view.findViewById(R.id.cardNotP1);
        CardView cardNotP2 = view.findViewById(R.id.cardNotP2);

        //Metodos para abrir las diferentes aplicaciones
        //Metodo para abrir noticia grande
        butVerMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noticias.abrirLink(
                        requireContext(),
                        "https://cnnespanol.cnn.com/2026/04/20/eeuu/analisis-amenazas-alto-fuego-iran-trax",
                        "https://cnnespanol.cnn.com/2026/04/20/eeuu/analisis-amenazas-alto-fuego-iran-trax"
                );
            }
        });

        //Metodo para abrir noticia pequeña1
        cardNotP1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noticias.abrirLink(
                        requireContext(),
                        "https://www.jornada.com.mx/noticia/2026/04/19/mundo/corea-del-norte-realiza-nuevos-ensayos-de-misiles-balisticos",
                        "https://www.jornada.com.mx/noticia/2026/04/19/mundo/corea-del-norte-realiza-nuevos-ensayos-de-misiles-balisticos"
                );
            }
        });

        //Metodo para abrir noticia pequeña2
        cardNotP2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noticias.abrirLink(
                        requireContext(),
                        "https://www.milenio.com/tecnologia/whatsapp-liquid-glass-como-activar-la-nueva-interfaz-en-ios",
                        "https://www.milenio.com/tecnologia/whatsapp-liquid-glass-como-activar-la-nueva-interfaz-en-ios"
                );
            }
        });

        return view;
    }
}