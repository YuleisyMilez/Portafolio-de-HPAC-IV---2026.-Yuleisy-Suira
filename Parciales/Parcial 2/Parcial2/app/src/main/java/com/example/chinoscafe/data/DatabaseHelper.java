package com.example.chinoscafe.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la base de datos SQLite local.
 *
 * Patrón "local primero": cada operación de escritura persiste en SQLite
 * de forma síncrona y, si tiene éxito, dispara la réplica remota de manera
 * asíncrona a través de SyncManager sin bloquear la UI ni el hilo que llama.
 *
 * Las operaciones de lectura (GET) no se replican porque la fuente de verdad
 * para la app es SQLite; la API remota es solo el espejo de escritura.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG       = "DatabaseHelper";
    private static final String DB_NAME   = "chinos_cafe.db";
    private static final int    DB_VERSION = 1;

    // SyncManager es opcional: si es null, el helper funciona sin red.
    private final SyncManager sync;

    // ── Constructores ────────────────────────────────────────────────────────

    /** Constructor sin replicación (útil para tests o modo offline). */
    public DatabaseHelper(Context context) {
        this(context, null);
    }

    /** Constructor con replicación remota habilitada. */
    public DatabaseHelper(Context context, SyncManager syncManager) {
        super(context, DB_NAME, null, DB_VERSION);
        this.sync = syncManager;
    }

    // ── Ciclo de vida de la BD ───────────────────────────────────────────────

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE clientes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario TEXT NOT NULL UNIQUE, " +
                "contrasena TEXT NOT NULL)");

        db.execSQL("CREATE TABLE menus (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_cliente INTEGER NOT NULL, " +
                "nombre TEXT NOT NULL, " +
                "precio REAL NOT NULL, " +
                "FOREIGN KEY(id_cliente) REFERENCES clientes(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE pedidos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_cliente INTEGER NOT NULL, " +
                "direccion_entrega TEXT NOT NULL, " +
                "telefono_contacto TEXT NOT NULL, " +
                "subtotal REAL NOT NULL, " +
                "envio REAL NOT NULL, " +
                "fecha TEXT NOT NULL, " +
                "estado TEXT NOT NULL, " +
                "FOREIGN KEY(id_cliente) REFERENCES clientes(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE pagos (" +
                "codigo_pago INTEGER PRIMARY KEY, " +
                "id_pedido INTEGER NOT NULL UNIQUE, " +
                "numero_origen TEXT, " +
                "numero_destino TEXT, " +
                "monto REAL NOT NULL, " +
                "captura BLOB, " +
                "FOREIGN KEY(id_pedido) REFERENCES pedidos(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE menu_pedido (" +
                "id_menu INTEGER NOT NULL, " +
                "id_pedido INTEGER NOT NULL, " +
                "cantidad INTEGER NOT NULL, " +
                "PRIMARY KEY(id_menu, id_pedido), " +
                "FOREIGN KEY(id_menu) REFERENCES menus(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(id_pedido) REFERENCES pedidos(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE ingredientes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL UNIQUE, " +
                "precio REAL NOT NULL, " +
                "categoria TEXT NOT NULL, " +
                "disponible INTEGER NOT NULL DEFAULT 1)");

        db.execSQL("CREATE TABLE menu_ingredientes (" +
                "id_menu INTEGER NOT NULL, " +
                "id_ingrediente INTEGER NOT NULL, " +
                "PRIMARY KEY(id_menu, id_ingrediente), " +
                "FOREIGN KEY(id_menu) REFERENCES menus(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(id_ingrediente) REFERENCES ingredientes(id) ON DELETE CASCADE)");

        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS menu_ingredientes");
        db.execSQL("DROP TABLE IF EXISTS menu_pedido");
        db.execSQL("DROP TABLE IF EXISTS pagos");
        db.execSQL("DROP TABLE IF EXISTS pedidos");
        db.execSQL("DROP TABLE IF EXISTS menus");
        db.execSQL("DROP TABLE IF EXISTS ingredientes");
        db.execSQL("DROP TABLE IF EXISTS clientes");
        onCreate(db);
    }

    // ── Seed ────────────────────────────────────────────────────────────────

    private void seedInitialData(SQLiteDatabase db) {
        ContentValues cliente1 = new ContentValues();
        ContentValues cliente2 = new ContentValues();
        cliente1.put("usuario", "axelc");
        cliente1.put("contrasena", "123456");
        cliente2.put("usuario", "yuleisys");
        cliente2.put("contrasena", "123456");
        db.insert("clientes", null, cliente1);
        db.insert("clientes", null, cliente2);

        insertIngredient(db, "Masa clásica",                    4.00, "masa");
        insertIngredient(db, "Masa Romana",                     4.50, "masa");
        insertIngredient(db, "Masa clásica con bordes de queso",5.50, "masa");
        insertIngredient(db, "Salsa de tomate tradicional",     1.50, "salsa");
        insertIngredient(db, "Salsa Alfredo",                   2.00, "salsa");
        insertIngredient(db, "Salsa Buffalo",                   2.00, "salsa");
        insertIngredient(db, "Mozzarella",                      2.00, "queso");
        insertIngredient(db, "Provolone",                       2.25, "queso");
        insertIngredient(db, "Parmesano",                       3.10, "queso");
        insertIngredient(db, "Pepperoni",                       3.00, "topping");
        insertIngredient(db, "Pollo",                           3.00, "topping");
        insertIngredient(db, "Hongos",                          2.50, "topping");
        insertIngredient(db, "Pimientos",                       1.75, "topping");
        insertIngredient(db, "Aceitunas",                       1.75, "topping");
        insertIngredient(db, "6x Rollitos primavera",           3.50, "extra");
        insertIngredient(db, "1x Coca-Cola 2L",                 2.50, "extra");
        insertIngredient(db, "3x Cinnamon Rolls",               3.50, "extra");
    }

    private void insertIngredient(SQLiteDatabase db, String nombre, double precio, String categoria) {
        ContentValues v = new ContentValues();
        v.put("nombre", nombre);
        v.put("precio", precio);
        v.put("categoria", categoria);
        v.put("disponible", 1);
        db.insert("ingredientes", null, v);
    }

    // ── Helper de log para callbacks de sync ────────────────────────────────

    /**
     * Callback silencioso que solo loguea el resultado del sync remoto.
     * No interrumpe la operación local, que ya fue confirmada en SQLite.
     */
    private SyncManager.SyncCallback logCallback(String operacion) {
        return new SyncManager.SyncCallback() {
            @Override
            public void onSuccess(String responseBody) {
                Log.d(TAG, "[SYNC OK] " + operacion + " → " + responseBody);
            }
            @Override
            public void onFailure(String errorMessage) {
                // Solo se loguea. En una versión más robusta se podría
                // guardar en una tabla "pending_sync" para reintentar.
                Log.w(TAG, "[SYNC FAIL] " + operacion + " → " + errorMessage);
            }
        };
    }

    // ════════════════════════════════════════════════════════════════════════
    // ─── LECTURA (sin replicación) ──────────────────────────────────────────
    // ════════════════════════════════════════════════════════════════════════

    public long autenticarCliente(String usuario, String contrasena) {
        try (Cursor cursor = getReadableDatabase().query(
                "clientes", new String[]{"id"},
                "usuario = ? AND contrasena = ?",
                new String[]{usuario, contrasena},
                null, null, null)) {
            return cursor.moveToFirst() ? cursor.getLong(0) : -1;
        }
    }

    public List<Ingredient> getIngredientesPorCategoria(String categoria) {
        List<Ingredient> ingredientes = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                "ingredientes",
                new String[]{"id", "nombre", "precio", "categoria"},
                "categoria = ? AND disponible = 1",
                new String[]{categoria},
                null, null, "nombre ASC")) {
            while (cursor.moveToNext()) {
                ingredientes.add(new Ingredient(
                        cursor.getLong(0), cursor.getString(1),
                        cursor.getDouble(2), cursor.getString(3)));
            }
        }
        return ingredientes;
    }

    public List<Menu> getMenusPorCliente(long idCliente) {
        List<Menu> menus = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                "menus", new String[]{"id", "nombre", "precio"},
                "id_cliente = ?", new String[]{String.valueOf(idCliente)},
                null, null, "id DESC")) {
            while (cursor.moveToNext()) {
                menus.add(new Menu(cursor.getLong(0), cursor.getString(1), cursor.getDouble(2)));
            }
        }
        return menus;
    }

    public List<PedidoResumen> getPedidosPorCliente(long idCliente) {
        List<PedidoResumen> pedidos = new ArrayList<>();
        String sql = "SELECT pedidos.id, pedidos.subtotal, pedidos.envio, pedidos.fecha, pedidos.estado, " +
                "COALESCE(pagos.monto, 0) AS monto_pago " +
                "FROM pedidos LEFT JOIN pagos ON pedidos.id = pagos.id_pedido " +
                "WHERE pedidos.id_cliente = ? ORDER BY pedidos.id DESC";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(idCliente)})) {
            while (cursor.moveToNext()) {
                pedidos.add(new PedidoResumen(
                        cursor.getLong(0), cursor.getDouble(1), cursor.getDouble(2),
                        cursor.getString(3), cursor.getString(4), cursor.getDouble(5)));
            }
        }
        return pedidos;
    }

    public MenuDetalle getMenuDetalle(long menuId) {
        Menu menu = null;
        try (Cursor cursor = getReadableDatabase().query(
                "menus", new String[]{"id", "nombre", "precio"},
                "id = ?", new String[]{String.valueOf(menuId)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                menu = new Menu(cursor.getLong(0), cursor.getString(1), cursor.getDouble(2));
            }
        }
        if (menu == null) return null;

        List<Ingredient> ingredientes = new ArrayList<>();
        String sql = "SELECT ingredientes.id, ingredientes.nombre, ingredientes.precio, ingredientes.categoria " +
                "FROM ingredientes INNER JOIN menu_ingredientes " +
                "ON ingredientes.id = menu_ingredientes.id_ingrediente " +
                "WHERE menu_ingredientes.id_menu = ? ORDER BY ingredientes.categoria, ingredientes.nombre";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(menuId)})) {
            while (cursor.moveToNext()) {
                ingredientes.add(new Ingredient(
                        cursor.getLong(0), cursor.getString(1),
                        cursor.getDouble(2), cursor.getString(3)));
            }
        }
        return new MenuDetalle(menu, ingredientes);
    }

    public PedidoDetalle getPedidoDetalle(long pedidoId) {
        PedidoDetalle pedido = null;
        String sql = "SELECT pedidos.id, pedidos.direccion_entrega, pedidos.telefono_contacto, " +
                "pedidos.subtotal, pedidos.envio, pedidos.fecha, pedidos.estado, " +
                "pagos.codigo_pago, pagos.numero_origen, pagos.numero_destino, pagos.monto, pagos.captura " +
                "FROM pedidos LEFT JOIN pagos ON pedidos.id = pagos.id_pedido WHERE pedidos.id = ?";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(pedidoId)})) {
            if (cursor.moveToFirst()) {
                pedido = new PedidoDetalle(
                        cursor.getLong(0), cursor.getString(1), cursor.getString(2),
                        cursor.getDouble(3), cursor.getDouble(4), cursor.getString(5),
                        cursor.getString(6),
                        cursor.isNull(7) ? 0 : cursor.getLong(7),
                        cursor.getString(8), cursor.getString(9),
                        cursor.isNull(10) ? 0 : cursor.getDouble(10),
                        cursor.getBlob(11));
            }
        }
        if (pedido == null) return null;

        String detallesSql = "SELECT menus.nombre, menu_pedido.cantidad, menus.precio " +
                "FROM menu_pedido INNER JOIN menus ON menus.id = menu_pedido.id_menu " +
                "WHERE menu_pedido.id_pedido = ? ORDER BY menus.nombre";
        try (Cursor cursor = getReadableDatabase().rawQuery(detallesSql, new String[]{String.valueOf(pedidoId)})) {
            while (cursor.moveToNext()) {
                pedido.detallesMenus.add(cursor.getString(0) + " x" + cursor.getInt(1) +
                        " - $" + String.format("%.2f", cursor.getDouble(2) * cursor.getInt(1)));
            }
        }
        return pedido;
    }

    // ════════════════════════════════════════════════════════════════════════
    // ─── MENÚS — escritura + sync ────────────────────────────────────────────
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Guarda un menú localmente y lo replica en la API remota.
     *
     * @return ID local del menú creado, o -1 si falla la inserción local.
     */
    public long guardarMenu(long idCliente, String nombre, List<Ingredient> ingredientes) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        long menuId = -1;
        double precio = 0;
        try {
            for (Ingredient i : ingredientes) precio += i.precio;

            ContentValues menuValues = new ContentValues();
            menuValues.put("id_cliente", idCliente);
            menuValues.put("nombre", nombre);
            menuValues.put("precio", precio);
            menuId = db.insertOrThrow("menus", null, menuValues);

            for (Ingredient i : ingredientes) {
                ContentValues rel = new ContentValues();
                rel.put("id_menu", menuId);
                rel.put("id_ingrediente", i.id);
                db.insertWithOnConflict("menu_ingredientes", null, rel, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        // Sync remoto (solo si el insert local fue exitoso)
        if (menuId != -1 && sync != null) {
            final double precioFinal = precio;
            sync.createMenu((int) idCliente, nombre, precioFinal,
                    logCallback("createMenu nombre=" + nombre));
        }

        return menuId;
    }

    /**
     * Actualiza un menú localmente y sincroniza el cambio con la API.
     *
     * @return true si la actualización local tuvo efecto.
     */
    public boolean actualizarMenu(long menuId, String nombre, List<Ingredient> ingredientes) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        boolean updated = false;
        double precio = 0;
        try {
            for (Ingredient i : ingredientes) precio += i.precio;

            ContentValues menuValues = new ContentValues();
            menuValues.put("nombre", nombre);
            menuValues.put("precio", precio);
            updated = db.update("menus", menuValues, "id = ?",
                    new String[]{String.valueOf(menuId)}) > 0;

            db.delete("menu_ingredientes", "id_menu = ?", new String[]{String.valueOf(menuId)});
            for (Ingredient i : ingredientes) {
                ContentValues rel = new ContentValues();
                rel.put("id_menu", menuId);
                rel.put("id_ingrediente", i.id);
                db.insertWithOnConflict("menu_ingredientes", null, rel, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (updated && sync != null) {
            sync.updateMenu((int) menuId, nombre, precio,
                    logCallback("updateMenu id=" + menuId));
        }

        return updated;
    }

    /**
     * Elimina un menú localmente y lo borra también en la API.
     *
     * @return true si el borrado local tuvo efecto.
     */
    public boolean eliminarMenu(long menuId) {
        boolean deleted = getWritableDatabase()
                .delete("menus", "id = ?", new String[]{String.valueOf(menuId)}) > 0;

        if (deleted && sync != null) {
            sync.deleteMenu((int) menuId, logCallback("deleteMenu id=" + menuId));
        }

        return deleted;
    }

    // ════════════════════════════════════════════════════════════════════════
    // ─── PEDIDOS — escritura + sync ──────────────────────────────────────────
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Guarda un pedido localmente (con su detalle de menús) y lo replica.
     *
     * @return ID local del pedido, o -1 si falla.
     */
    public long guardarPedido(long idCliente, String direccion, String telefono,
                              List<OrderItem> items, double subtotal, double envio,
                              String fecha, String estado) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        long pedidoId = -1;
        try {
            ContentValues pedidoValues = new ContentValues();
            pedidoValues.put("id_cliente",         idCliente);
            pedidoValues.put("direccion_entrega",   direccion);
            pedidoValues.put("telefono_contacto",   telefono);
            pedidoValues.put("subtotal",            subtotal);
            pedidoValues.put("envio",               envio);
            pedidoValues.put("fecha",               fecha);
            pedidoValues.put("estado",              estado);
            pedidoId = db.insertOrThrow("pedidos", null, pedidoValues);

            for (OrderItem item : items) {
                ContentValues rel = new ContentValues();
                rel.put("id_menu",    item.menu.id);
                rel.put("id_pedido",  pedidoId);
                rel.put("cantidad",   item.cantidad);
                db.insertOrThrow("menu_pedido", null, rel);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (pedidoId != -1 && sync != null) {
            sync.createPedido((int) pedidoId, (int) idCliente, direccion, telefono,
                    subtotal, envio, fecha, estado,
                    logCallback("createPedido cliente=" + idCliente));
        }

        return pedidoId;
    }

    /**
     * Actualiza el estado de un pedido localmente y lo sincroniza.
     *
     * @return true si hubo cambio local.
     */
    public boolean actualizarEstadoPedido(long pedidoId, String nuevoEstado) {
        ContentValues estado = new ContentValues();
        estado.put("estado", nuevoEstado);
        boolean updated = getWritableDatabase()
                .update("pedidos", estado, "id = ?",
                        new String[]{String.valueOf(pedidoId)}) > 0;

        if (updated && sync != null) {
            sync.updatePedido((int) pedidoId, nuevoEstado,
                    logCallback("updatePedido id=" + pedidoId));
        }

        return updated;
    }

    /**
     * Elimina un pedido localmente (cascada borra pagos y menu_pedido) y lo replica.
     *
     * @return true si el borrado local tuvo efecto.
     */
    public boolean eliminarPedido(long pedidoId) {
        boolean deleted = getWritableDatabase()
                .delete("pedidos", "id = ?", new String[]{String.valueOf(pedidoId)}) > 0;

        if (deleted && sync != null) {
            sync.deletePedido((int) pedidoId, logCallback("deletePedido id=" + pedidoId));
        }

        return deleted;
    }

    // ════════════════════════════════════════════════════════════════════════
    // ─── PAGOS — escritura + sync ─────────────────────────────────────────────
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Guarda un pago localmente y lo replica en la API.
     *
     * Nota: el esquema local usa campos propios de Yappy (numero_origen,
     * numero_destino, captura). La API remota usa metodo_pago y referencia_yappy,
     * por lo que se mapean los campos más relevantes en la llamada de sync.
     *
     * @return ID local del pago, o -1 si falla.
     */
    public long guardarPago(long codigoPago, long idPedido, String numeroOrigen,
                            String numeroDestino, double monto, byte[] captura) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        boolean exito = false;
        try {
            ContentValues pagoValues = new ContentValues();
            pagoValues.put("codigo_pago",     codigoPago);
            pagoValues.put("id_pedido",       idPedido);
            pagoValues.put("numero_origen",   numeroOrigen);
            pagoValues.put("numero_destino",  numeroDestino);
            pagoValues.put("monto",           monto);
            pagoValues.put("captura",         captura);
            db.insertOrThrow("pagos", null, pagoValues);

            // Marca el pedido como pagado también en local
            ContentValues estado = new ContentValues();
            estado.put("estado", "En proceso");
            db.update("pedidos", estado, "id = ?", new String[]{String.valueOf(idPedido)});

            db.setTransactionSuccessful();
            exito = true;
        } finally {
            db.endTransaction();
        }

        if (exito && sync != null) {
            sync.createPago(
                    codigoPago,
                    (int) idPedido,
                    numeroOrigen,    // número de teléfono origen YAPPY
                    numeroDestino,   // número de teléfono destino YAPPY
                    monto,
                    captura,         // imagen en bytes
                    logCallback("createPago pedido=" + idPedido)
            );
            sync.updatePedido((int) idPedido, "En proceso",
                    logCallback("updatePedido→En proceso id=" + idPedido));
        }

        return codigoPago;
    }

    // ════════════════════════════════════════════════════════════════════════
    // ─── Modelos internos ────────────────────────────────────════════════════
    // ════════════════════════════════════════════════════════════════════════

    public static class Ingredient {
        public final long   id;
        public final String nombre;
        public final double precio;
        public final String categoria;

        public Ingredient(long id, String nombre, double precio, String categoria) {
            this.id        = id;
            this.nombre    = nombre;
            this.precio    = precio;
            this.categoria = categoria;
        }

        @Override public String toString() {
            return nombre + " - $" + String.format("%.2f", precio);
        }
    }

    public static class Menu {
        public final long   id;
        public final String nombre;
        public final double precio;

        public Menu(long id, String nombre, double precio) {
            this.id     = id;
            this.nombre = nombre;
            this.precio = precio;
        }

        @Override public String toString() {
            return nombre + " - $" + String.format("%.2f", precio);
        }
    }

    public static class OrderItem {
        public final Menu menu;
        public final int  cantidad;

        public OrderItem(Menu menu, int cantidad) {
            this.menu     = menu;
            this.cantidad = cantidad;
        }
    }

    public static class PedidoResumen {
        public final long   id;
        public final double subtotal;
        public final double envio;
        public final String fecha;
        public final String estado;
        public final double montoPago;

        public PedidoResumen(long id, double subtotal, double envio,
                             String fecha, String estado, double montoPago) {
            this.id        = id;
            this.subtotal  = subtotal;
            this.envio     = envio;
            this.fecha     = fecha;
            this.estado    = estado;
            this.montoPago = montoPago;
        }
    }

    public static class MenuDetalle {
        public final Menu             menu;
        public final List<Ingredient> ingredientes;

        public MenuDetalle(Menu menu, List<Ingredient> ingredientes) {
            this.menu         = menu;
            this.ingredientes = ingredientes;
        }
    }

    public static class PedidoDetalle {
        public final long        id;
        public final String      direccion;
        public final String      telefono;
        public final double      subtotal;
        public final double      envio;
        public final String      fecha;
        public final String      estado;
        public final long        codigoPago;
        public final String      numeroOrigen;
        public final String      numeroDestino;
        public final double      montoPago;
        public final byte[]      captura;
        public final List<String> detallesMenus = new ArrayList<>();

        public PedidoDetalle(long id, String direccion, String telefono,
                             double subtotal, double envio, String fecha, String estado,
                             long codigoPago, String numeroOrigen, String numeroDestino,
                             double montoPago, byte[] captura) {
            this.id            = id;
            this.direccion     = direccion;
            this.telefono      = telefono;
            this.subtotal      = subtotal;
            this.envio         = envio;
            this.fecha         = fecha;
            this.estado        = estado;
            this.codigoPago    = codigoPago;
            this.numeroOrigen  = numeroOrigen;
            this.numeroDestino = numeroDestino;
            this.montoPago     = montoPago;
            this.captura       = captura;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
// ─── SINCRONIZACIÓN DESCENDENTE (MySQL → SQLite) ─────────────────────
// ════════════════════════════════════════════════════════════════════════

    /**
     * Sincroniza menus desde la API remota hacia SQLite local.
     * Llama a GET /menus y actualiza la BD local.
     */
    public void syncMenusDesdeServidor() {
        if (sync == null) return;
        sync.getMenus(new SyncManager.SyncCallback() {
            @Override
            public void onSuccess(String responseBody) {
                try {
                    org.json.JSONArray menus = new org.json.JSONArray(responseBody);
                    SQLiteDatabase db = getWritableDatabase();
                    db.beginTransaction();
                    try {
                        db.delete("menu_ingredientes", null, null);
                        db.delete("menus", null, null);
                        for (int i = 0; i < menus.length(); i++) {
                            org.json.JSONObject menu = menus.getJSONObject(i);
                            ContentValues values = new ContentValues();
                            values.put("id",         menu.getLong("id"));
                            values.put("id_cliente", menu.getLong("id_cliente"));
                            values.put("nombre",     menu.getString("nombre"));
                            values.put("precio",     menu.getDouble("precio"));
                            db.insertWithOnConflict("menus", null, values,
                                    SQLiteDatabase.CONFLICT_REPLACE);
                        }
                        db.setTransactionSuccessful();
                        Log.d(TAG, "[SYNC DOWN] Menus sincronizados: " + menus.length());
                    } finally {
                        db.endTransaction();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "[SYNC DOWN] Error al sincronizar menus: " + e.getMessage());
                }
            }
            @Override
            public void onFailure(String errorMessage) {
                Log.w(TAG, "[SYNC DOWN] Error al obtener menus: " + errorMessage);
            }
        });
    }

    /**
     * Sincroniza pedidos desde la API remota hacia SQLite local.
     */
    public void syncPedidosDesdeServidor() {
        if (sync == null) return;
        sync.getPedidos(new SyncManager.SyncCallback() {
            @Override
            public void onSuccess(String responseBody) {
                try {
                    org.json.JSONArray pedidos = new org.json.JSONArray(responseBody);
                    SQLiteDatabase db = getWritableDatabase();
                    db.beginTransaction();
                    try {
                        db.delete("menu_pedido", null, null);
                        db.delete("pagos", null, null);
                        db.delete("pedidos", null, null);
                        for (int i = 0; i < pedidos.length(); i++) {
                            org.json.JSONObject pedido = pedidos.getJSONObject(i);
                            ContentValues values = new ContentValues();
                            values.put("id",                 pedido.getLong("id"));
                            values.put("id_cliente",         pedido.getLong("id_cliente"));
                            values.put("direccion_entrega",  pedido.getString("direccion_entrega"));
                            values.put("telefono_contacto",  pedido.getString("telefono_contacto"));
                            values.put("subtotal",           pedido.getDouble("subtotal"));
                            values.put("envio",              pedido.getDouble("envio"));
                            values.put("fecha",              pedido.getString("fecha"));
                            values.put("estado",             pedido.getString("estado"));
                            db.insertWithOnConflict("pedidos", null, values,
                                    SQLiteDatabase.CONFLICT_REPLACE);
                        }
                        db.setTransactionSuccessful();
                        Log.d(TAG, "[SYNC DOWN] Pedidos sincronizados: " + pedidos.length());
                        syncPagosDesdeServidor();
                    } finally {
                        db.endTransaction();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "[SYNC DOWN] Error al sincronizar pedidos: " + e.getMessage());
                }
            }
            @Override
            public void onFailure(String errorMessage) {
                Log.w(TAG, "[SYNC DOWN] Error al obtener pedidos: " + errorMessage);
            }
        });
    }

    /**
     * Sincroniza pagos desde la API remota hacia SQLite local.
     */
    public void syncPagosDesdeServidor() {
        if (sync == null) return;
        sync.getPagos(new SyncManager.SyncCallback() {
            @Override
            public void onSuccess(String responseBody) {
                try {
                    org.json.JSONArray pagos = new org.json.JSONArray(responseBody);
                    SQLiteDatabase db = getWritableDatabase();
                    db.beginTransaction();
                    try {
                        db.delete("pagos", null, null);
                        for (int i = 0; i < pagos.length(); i++) {
                            org.json.JSONObject pago = pagos.getJSONObject(i);
                            ContentValues values = new ContentValues();
                            values.put("codigo_pago",     pago.getLong("codigo_pago"));
                            values.put("id_pedido",       pago.getLong("id_pedido"));
                            values.put("numero_origen",   pago.optString("numero_origen", ""));
                            values.put("numero_destino",  pago.optString("numero_destino", ""));
                            values.put("monto",           pago.getDouble("monto"));
                            // captura viene en Base64, convertir a byte[]
                            if (!pago.isNull("captura")) {
                                byte[] capturaBytes = android.util.Base64.decode(
                                        pago.getString("captura"), android.util.Base64.DEFAULT);
                                values.put("captura", capturaBytes);
                            }
                            db.insertWithOnConflict("pagos", null, values,
                                    SQLiteDatabase.CONFLICT_REPLACE);
                        }
                        db.setTransactionSuccessful();
                        Log.d(TAG, "[SYNC DOWN] Pagos sincronizados: " + pagos.length());
                    } finally {
                        db.endTransaction();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "[SYNC DOWN] Error al sincronizar pagos: " + e.getMessage());
                }
            }
            @Override
            public void onFailure(String errorMessage) {
                Log.w(TAG, "[SYNC DOWN] Error al obtener pagos: " + errorMessage);
            }
        });
    }
}