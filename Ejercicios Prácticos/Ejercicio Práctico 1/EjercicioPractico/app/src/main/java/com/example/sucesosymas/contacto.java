package com.example.sucesosymas;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link contacto#newInstance} factory method to
 * create an instance of this fragment.
 */
public class contacto extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public contacto() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment contacto.
     */
    // TODO: Rename and change types and number of parameters
    public static contacto newInstance(String param1, String param2) {
        contacto fragment = new contacto();
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
        View view = inflater.inflate(R.layout.fragment_contacto, container, false);

        CardView instagram = view.findViewById(R.id.clickInstagram);
        CardView facebook = view.findViewById(R.id.clickFacebook);
        CardView whatsapp = view.findViewById(R.id.clickWhatsapp);
        CardView youtube = view.findViewById(R.id.clickYouTube);

        //Metodos para abrir las diferentes aplicaciones
        //Metodo para abrir aplicacion de instagram
        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirLink(
                        requireContext(),
                        "instagram://user?username=sucesosymasch", "https://www.instagram.com/sucesosymasch"
                );
            }
        });

        //Metodo para abrir aplicacion de facebook
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirLink(
                        requireContext(),
                        "facebook=//user?username=sucesosymasch", "https://www.facebook.com/sucesosymasch"
                );
            }
        });

        //Metodo para abrir aplicacion de whatsapp
        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirLink(
                        requireContext(),
                        "whatsapp://send?phone=+507XXXXXXXX",
                        "https://wa.me/+507XXXXXXXX"
                );
            }
        });

        //Metodo para abrir aplicacion de youtube
       youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirLink(
                        requireContext(),
                        "facebook=//user?username=rosacano5525",
                        "https://www.youtube.com/@rosacano5525"
                );
            }
        });

        return view;
    }

    //Funcion reutilizable para abrir aplicaciones, si encuentra la app abre el link en la app, sino la abrira en la web
   public void abrirLink(Context context, String appUrl, String webUrl) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
