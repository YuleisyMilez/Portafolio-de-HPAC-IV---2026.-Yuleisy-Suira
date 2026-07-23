package com.chiriquieats.app.data;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SyncManager {
    private static final String API_URL = "http://10.10.10.174:3000/api";
    private static final String API_TOKEN = "Bearer ChiriquiEatsAcYs#2026Token";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    //Metodo de sincronizacion de las empresas nuevas
    public void sincronizarEmpresa(String ruc, String nombre, String direccion,
                                   String categoria, String telefono, String correo) {
        Map<String, Object> map = new HashMap<>();
        map.put("ruc", ruc);
        map.put("nombre", nombre);
        map.put("direccion", direccion);
        map.put("categoria", categoria);
        map.put("telefono", telefono);
        map.put("correo", correo);
        enviarDatos(API_URL + "/empresas", map);
    }

    //Metodo de sincronizacion de actualizacion de empresas
    public void sincronizarUpdateEmpresa(String ruc, String nombre, String direccion,
                                         String categoria, String telefono, String correo) {
        Map<String, Object> map = new HashMap<>();
        map.put("ruc", ruc);
        map.put("nombre", nombre);
        map.put("direccion", direccion);
        map.put("categoria", categoria);
        map.put("telefono", telefono);
        map.put("correo", correo);
        actualizarDatos(API_URL + "/empresas/" + ruc, map);
    }

    //Metodo de sincronizacion para la eliminacion de empresas
    public void sincronizarDeleteEmpresa(String ruc) {
        eliminarDatos(API_URL + "/empresas/" + ruc);
    }

    //Metodo de sincronizacion de menus nuevos
    public void sincronizarMenu(String codigoMenu, String rucEmpresa, String nombre,
                                String contenido, double precio) {
        Map<String, Object> map = new HashMap<>();
        map.put("codigo_menu", codigoMenu);
        map.put("ruc_empresa", rucEmpresa);
        map.put("nombre", nombre);
        map.put("contenido", contenido);
        map.put("precio", precio);
        enviarDatos(API_URL + "/menus", map);
    }

    //Metodo de sincronizacion de actualizacion de menus
    public void sincronizarUpdateMenu(String codigoMenu, String rucEmpresa, String nombre,
                                      String contenido, double precio) {
        Map<String, Object> map = new HashMap<>();
        map.put("codigo_menu", codigoMenu);
        map.put("ruc_empresa", rucEmpresa);
        map.put("nombre", nombre);
        map.put("contenido", contenido);
        map.put("precio", precio);
        actualizarDatos(API_URL + "/menus/" + codigoMenu, map);
    }

    //Metodo de sincronizacion para la eliminacion de menus
    public void sincronizarDeleteMenu(String codigoMenu) {
        eliminarDatos(API_URL + "/menus/" + codigoMenu);
    }

    //Metodo de sincronizacion de los pedidos
    public void sincronizarPedido(String rucEmpresa, String codigoMenu, String fecha,
                                  double montoEnvio, String nombre, String direccion) {
        Map<String, Object> map = new HashMap<>();
        map.put("ruc_empresa", rucEmpresa);
        map.put("codigo_menu", codigoMenu);
        map.put("fecha", fecha);
        map.put("monto_envio", montoEnvio);
        map.put("nombre", nombre);
        map.put("direccion", direccion);
        enviarDatos(API_URL + "/pedidos", map);
    }

    //Metodos de sincronizacion de los pagos
    public void sincronizarPago(long codigoPago, long codigoPedido, String numeroOrigen,
                                String numeroDestino, double monto, byte[] captura) {
        Map<String, Object> map = new HashMap<>();
        map.put("codigo_pago", codigoPago);
        map.put("codigo_pedido", codigoPedido);
        map.put("numero_origen", numeroOrigen);
        map.put("numero_destino", numeroDestino);
        map.put("monto", monto);
        // El BLOB se convierte a Base64 para poder enviarlo como JSON
        map.put("captura", Base64.encodeToString(captura, Base64.DEFAULT));
        enviarDatos(API_URL + "/pagos", map);
    }

    //Metodo de envio de los datos
    private void enviarDatos(String url, Map<String, Object> map) {
        String json = gson.toJson(map);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", API_TOKEN)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                // Sincronizado exitosamente
            }
            @Override
            public void onFailure(Call call, IOException e) {
                // Guardar para sincronizar después
            }
        });
    }

    //Metodo de envio para actualizacion de datos
    private void actualizarDatos(String url, Map<String, Object> map) {
        String json = gson.toJson(map);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", API_TOKEN)
                .put(body)          // PUT en lugar de POST
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) {}
            @Override
            public void onFailure(Call call, IOException e) {}
        });
    }

    //Metodo de envio para la eliminacion de datos
    private void eliminarDatos(String url) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", API_TOKEN)
                .delete()           // DELETE sin body
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) {}
            @Override
            public void onFailure(Call call, IOException e) {}
        });
    }
}