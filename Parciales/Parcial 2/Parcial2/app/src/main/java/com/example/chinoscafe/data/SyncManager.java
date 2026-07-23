package com.example.chinoscafe.data;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SyncManager {

    private static final String TAG = "SyncManager";
    private static final String BASE_URL = "http://172.16.10.93:3000";
    private static final String API_KEY  = "apiCC-P22026";
    private static final MediaType JSON  = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    // ── Interfaz de callback genérica ────────────────────────────────────────

    public interface SyncCallback {
        void onSuccess(String responseBody);
        void onFailure(String errorMessage);
    }

    // ── Métodos privados de utilidad ─────────────────────────────────────────

    /** Construye un Request.Builder con los headers comunes ya configurados. */
    private Request.Builder baseRequest(String endpoint) {
        return new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("x-api-key", API_KEY)
                .addHeader("Content-Type", "application/json");
    }

    /** Ejecuta una llamada OkHttp de forma asíncrona y delega el resultado al callback. */
    private void enqueue(Request request, SyncCallback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error de red: " + e.getMessage());
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    callback.onSuccess(body);
                } else {
                    Log.e(TAG, "Error HTTP " + response.code() + ": " + body);
                    callback.onFailure("HTTP " + response.code() + ": " + body);
                }
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ─── CLIENTES ────────────────────────────────────────────────────────────
    // ═════════════════════════════════════════════════════════════════════════

    /** GET /clientes — Obtiene todos los clientes. */
    public void getClientes(SyncCallback callback) {
        Request request = baseRequest("/clientes").get().build();
        enqueue(request, callback);
    }

    /**
     * POST /clientes — Crea un nuevo cliente.
     *
     * @param usuario   Nombre de usuario.
     * @param contrasena Contraseña del cliente.
     */
    public void createCliente(String usuario, String contrasena, SyncCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("usuario",    usuario);
        json.addProperty("contrasena", contrasena);

        RequestBody body    = RequestBody.create(json.toString(), JSON);
        Request     request = baseRequest("/clientes").post(body).build();
        enqueue(request, callback);
    }

    /**
     * PUT /clientes/:id — Actualiza un cliente existente.
     *
     * @param id         ID del cliente a actualizar.
     * @param usuario    Nuevo nombre de usuario.
     * @param contrasena Nueva contraseña.
     */
    public void updateCliente(int id, String usuario, String contrasena, SyncCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("usuario",    usuario);
        json.addProperty("contrasena", contrasena);

        RequestBody body    = RequestBody.create(json.toString(), JSON);
        Request     request = baseRequest("/clientes/" + id).put(body).build();
        enqueue(request, callback);
    }

    /**
     * DELETE /clientes/:id — Elimina un cliente.
     *
     * @param id ID del cliente a eliminar.
     */
    public void deleteCliente(int id, SyncCallback callback) {
        Request request = baseRequest("/clientes/" + id).delete().build();
        enqueue(request, callback);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ─── MENÚS ───────────────────────────────────────────────────────────────
    // ═════════════════════════════════════════════════════════════════════════

    /** GET /menus — Obtiene todos los menús. */
    public void getMenus(SyncCallback callback) {
        Request request = baseRequest("/menus").get().build();
        enqueue(request, callback);
    }

    /**
     * POST /menus — Crea un nuevo ítem de menú.
     *
     * @param idCliente ID del cliente propietario del menú.
     * @param nombre    Nombre del ítem.
     * @param precio    Precio del ítem.
     */
    public void createMenu(int idCliente, String nombre, double precio, SyncCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("id_cliente", idCliente);
        json.addProperty("nombre",     nombre);
        json.addProperty("precio",     precio);

        RequestBody body    = RequestBody.create(json.toString(), JSON);
        Request     request = baseRequest("/menus").post(body).build();
        enqueue(request, callback);
    }

    /**
     * PUT /menus/:id — Actualiza un ítem de menú.
     *
     * @param id     ID del ítem a actualizar.
     * @param nombre Nuevo nombre.
     * @param precio Nuevo precio.
     */
    public void updateMenu(int id, String nombre, double precio, SyncCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("nombre", nombre);
        json.addProperty("precio", precio);

        RequestBody body    = RequestBody.create(json.toString(), JSON);
        Request     request = baseRequest("/menus/" + id).put(body).build();
        enqueue(request, callback);
    }

    /**
     * DELETE /menus/:id — Elimina un ítem de menú.
     *
     * @param id ID del ítem a eliminar.
     */
    public void deleteMenu(int id, SyncCallback callback) {
        Request request = baseRequest("/menus/" + id).delete().build();
        enqueue(request, callback);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ─── PEDIDOS ─────────────────────────────────────────────────────────────
    // ═════════════════════════════════════════════════════════════════════════

    /** GET /pedidos — Obtiene todos los pedidos. */
    public void getPedidos(SyncCallback callback) {
        Request request = baseRequest("/pedidos").get().build();
        enqueue(request, callback);
    }

    /**
     * POST /pedidos — Crea un nuevo pedido.
     *
     * @param idCliente        ID del cliente que realiza el pedido.
     * @param direccionEntrega Dirección de entrega.
     * @param telefonoContacto Teléfono de contacto.
     * @param subtotal         Subtotal del pedido.
     * @param envio            Costo de envío.
     * @param fecha            Fecha del pedido (ej. "2026-06-14").
     * @param estado           Estado del pedido (ej. "pendiente").
     */
    public void createPedido(int idPedido, int idCliente, String direccionEntrega, String telefonoContacto,
                             double subtotal, double envio, String fecha,
                             String estado, SyncCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("id",                  idPedido);
        json.addProperty("id_cliente",          idCliente);
        json.addProperty("direccion_entrega",   direccionEntrega);
        json.addProperty("telefono_contacto",   telefonoContacto);
        json.addProperty("subtotal",            subtotal);
        json.addProperty("envio",               envio);
        json.addProperty("fecha",               fecha);
        json.addProperty("estado",              estado);

        RequestBody body    = RequestBody.create(json.toString(), JSON);
        Request     request = baseRequest("/pedidos").post(body).build();
        enqueue(request, callback);
    }

    /**
     * PUT /pedidos/:id — Actualiza el estado de un pedido.
     *
     * @param id     ID del pedido.
     * @param estado Nuevo estado (ej. "en preparación", "entregado").
     */
    public void updatePedido(int id, String estado, SyncCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("estado", estado);

        RequestBody body    = RequestBody.create(json.toString(), JSON);
        Request     request = baseRequest("/pedidos/" + id).put(body).build();
        enqueue(request, callback);
    }

    /**
     * DELETE /pedidos/:id — Elimina un pedido.
     *
     * @param id ID del pedido a eliminar.
     */
    public void deletePedido(int id, SyncCallback callback) {
        Request request = baseRequest("/pedidos/" + id).delete().build();
        enqueue(request, callback);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ─── PAGOS ───────────────────────────────────────────────────────────────
    // ═════════════════════════════════════════════════════════════════════════

    /** GET /pagos — Obtiene todos los pagos. */
    public void getPagos(SyncCallback callback) {
        Request request = baseRequest("/pagos").get().build();
        enqueue(request, callback);
    }

    /**
     * POST /pagos — Registra un nuevo pago.
     *
     * @param idPedido         ID del pedido asociado.
     * @param numeroOrigen     Número de origen del pago
     * @param numeroDestino    Número de destino del pago
     * @param monto            monto asociado al pago
     */
    public void createPago(long codigoPago, int idPedido, String numeroOrigen, String numeroDestino,
                           double monto, byte[] captura, SyncCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("codigo_pago",     codigoPago);
        json.addProperty("id_pedido",       idPedido);
        json.addProperty("numero_origen",   numeroOrigen);
        json.addProperty("numero_destino",  numeroDestino);
        json.addProperty("monto",           monto);
        // Convierte captura (byte[]) a Base64 para enviar por JSON
        if (captura != null) {
            String capturaBase64 = android.util.Base64.encodeToString(captura, android.util.Base64.DEFAULT);
            json.addProperty("captura", capturaBase64);
        }
        RequestBody body    = RequestBody.create(json.toString(), JSON);
        Request     request = baseRequest("/pagos").post(body).build();
        enqueue(request, callback);
    }
}