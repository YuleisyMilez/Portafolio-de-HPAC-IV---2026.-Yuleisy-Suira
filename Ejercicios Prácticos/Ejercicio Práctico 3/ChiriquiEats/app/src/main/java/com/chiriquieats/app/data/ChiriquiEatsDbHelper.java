package com.chiriquieats.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.util.ArrayList;
import java.util.List;

//Clase para configurar la base de datos
public class ChiriquiEatsDbHelper extends SQLiteOpenHelper {
    private final SyncManager syncManager = new SyncManager();

    //Definicion de las tablas utilizadas
    private static final String DATABASE_NAME = "chiriqui_eats.db";     //Se le asigna nombre
    private static final int DATABASE_VERSION = 1;      //versión

    public static final String TABLE_EMPRESAS = "empresas";
    public static final String TABLE_MENUS = "menus";
    public static final String TABLE_PEDIDOS = "pedidos";
    public static final String TABLE_PAGOS = "pagos";

    public ChiriquiEatsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    //Permitir la utilizacion de llaves foraneas
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creación de la tabla de empresas
        db.execSQL("CREATE TABLE " + TABLE_EMPRESAS + " ("
                + "ruc TEXT PRIMARY KEY, "
                + "nombre TEXT NOT NULL, "
                + "direccion TEXT NOT NULL, "
                + "categoria TEXT NOT NULL, "
                + "telefono TEXT NOT NULL, "
                + "correo TEXT NOT NULL"
                + ")");

        //Creación de la tabla de menús
        db.execSQL("CREATE TABLE " + TABLE_MENUS + " ("
                + "codigo_menu TEXT PRIMARY KEY, "
                + "ruc_empresa TEXT NOT NULL, "
                + "nombre TEXT NOT NULL, "
                + "contenido TEXT NOT NULL, "
                + "precio REAL NOT NULL, "
                + "FOREIGN KEY(ruc_empresa) REFERENCES " + TABLE_EMPRESAS + "(ruc) "        //conexion hacia la tabla empresas
                + "ON UPDATE CASCADE ON DELETE CASCADE"
                + ")");

        //Creación de la tabla de pedidos
        db.execSQL("CREATE TABLE " + TABLE_PEDIDOS + " ("
                + "codigo_pedido INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "ruc_empresa TEXT NOT NULL, "
                + "codigo_menu TEXT NOT NULL, "
                + "fecha TEXT NOT NULL, "
                + "monto_envio REAL NOT NULL, "
                + "nombre TEXT NOT NULL, "
                + "direccion TEXT NOT NULL, "
                + "FOREIGN KEY(ruc_empresa) REFERENCES " + TABLE_EMPRESAS + "(ruc) "        //conexion hacia la tabla empresas
                + "ON UPDATE CASCADE ON DELETE RESTRICT, "
                + "FOREIGN KEY(codigo_menu) REFERENCES " + TABLE_MENUS + "(codigo_menu) "   //conexión hacia la tabla menus
                + "ON UPDATE CASCADE ON DELETE RESTRICT"
                + ")");

        //Tabla de pagos
        db.execSQL("CREATE TABLE " + TABLE_PAGOS + " ("
                + "codigo_pago INTEGER PRIMARY KEY, "
                + "codigo_pedido INTEGER NOT NULL UNIQUE, "
                + "numero_origen TEXT NOT NULL, "
                + "numero_destino TEXT NOT NULL, "
                + "monto REAL NOT NULL, "
                + "captura BLOB NOT NULL, "
                + "FOREIGN KEY(codigo_pedido) REFERENCES " + TABLE_PEDIDOS + "(codigo_pedido) "     //conexion hacia la tabla pedidos
                + "ON UPDATE CASCADE ON DELETE CASCADE"
                + ")");
    }

    //Actualización de las tablas
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAGOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEDIDOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENUS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPRESAS);
        onCreate(db);
    }

    //Metodo para insertar una nueva empresa
    public long insertEmpresa(String ruc, String nombre, String direccion, String categoria,
                              String telefono, String correo) {
        ContentValues values = new ContentValues();
        values.put("ruc", ruc);
        values.put("nombre", nombre);
        values.put("direccion", direccion);
        values.put("categoria", categoria);
        values.put("telefono", telefono);
        values.put("correo", correo);
        long id = getWritableDatabase().insertOrThrow(TABLE_EMPRESAS, null, values);
        syncManager.sincronizarEmpresa(ruc, nombre, direccion, categoria, telefono, correo);
        return id;
    }

    //Metodo para filtrar según la empresa
    public String[] getEmpresaByRuc(String ruc) {
        Cursor cursor = getReadableDatabase().query(
                TABLE_EMPRESAS,
                new String[]{"ruc", "nombre", "direccion", "categoria", "telefono", "correo"},
                "ruc = ?",
                new String[]{ruc},
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return new String[]{
                        cursor.getString(cursor.getColumnIndexOrThrow("ruc")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                        cursor.getString(cursor.getColumnIndexOrThrow("direccion")),
                        cursor.getString(cursor.getColumnIndexOrThrow("categoria")),
                        cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                        cursor.getString(cursor.getColumnIndexOrThrow("correo"))
                };
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    //Metodo para actualizar los datos de las empresas
    public int updateEmpresa(String ruc, String nombre, String direccion, String categoria,
                             String telefono, String correo) {
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("direccion", direccion);
        values.put("categoria", categoria);
        values.put("telefono", telefono);
        values.put("correo", correo);
        int filas = getWritableDatabase().update(
                TABLE_EMPRESAS,
                values,
                "ruc = ?",
                new String[]{ruc}
        );
        if (filas > 0) {
            syncManager.sincronizarUpdateEmpresa(ruc, nombre, direccion, categoria, telefono, correo);
        }
        return filas;
    }
    //Metodo para eliminar una empresa de la base de datos
    public int deleteEmpresa(String ruc) {
        int filas = getWritableDatabase().delete(
                TABLE_EMPRESAS,
                "ruc = ?",
                new String[]{ruc}
        );
        if (filas > 0) {
            syncManager.sincronizarDeleteEmpresa(ruc);
        }
        return filas;
    }

    //Metodo para insertar nuevos menus
    public long insertMenu(String codigoMenu, String rucEmpresa, String nombre,
                           String contenido, double precio) {
        ContentValues values = new ContentValues();
        values.put("codigo_menu", codigoMenu);
        values.put("ruc_empresa", rucEmpresa);
        values.put("nombre", nombre);
        values.put("contenido", contenido);
        values.put("precio", precio);
        long id = getWritableDatabase().insertOrThrow(TABLE_MENUS, null, values);
        syncManager.sincronizarMenu(codigoMenu, rucEmpresa, nombre, contenido, precio);
        return id;
    }

    //Metodo para seleccionar un menu al agregar un pedido
    public String[] getMenuByCodigo(String codigoMenu) {
        Cursor cursor = getReadableDatabase().query(
                TABLE_MENUS,
                new String[]{"codigo_menu", "ruc_empresa", "nombre", "contenido", "precio"},
                "codigo_menu = ?",
                new String[]{codigoMenu},
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return new String[]{
                        cursor.getString(cursor.getColumnIndexOrThrow("codigo_menu")),
                        cursor.getString(cursor.getColumnIndexOrThrow("ruc_empresa")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                        cursor.getString(cursor.getColumnIndexOrThrow("contenido")),
                        String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("precio")))
                };
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    //Metodo para actualizar los datos del menu
    public int updateMenu(String codigoMenu, String rucEmpresa, String nombre,
                          String contenido, double precio) {
        ContentValues values = new ContentValues();
        values.put("ruc_empresa", rucEmpresa);
        values.put("nombre", nombre);
        values.put("contenido", contenido);
        values.put("precio", precio);
        int filas = getWritableDatabase().update(
                TABLE_MENUS,
                values,
                "codigo_menu = ?",
                new String[]{codigoMenu}
        );
        if (filas > 0) {
            syncManager.sincronizarUpdateMenu(codigoMenu, rucEmpresa, nombre, contenido, precio);
        }
        return filas;
    }

    //Metodo para eliminar un menu
    public int deleteMenu(String codigoMenu) {
        int filas = getWritableDatabase().delete(
                TABLE_MENUS,
                "codigo_menu = ?",
                new String[]{codigoMenu}
        );
        if (filas > 0) {
            syncManager.sincronizarDeleteMenu(codigoMenu);
        }
        return filas;
    }

    //Metodo para crear un nuevo pedido
    public long insertPedido(String rucEmpresa, String codigoMenu, String fecha,
                             double montoEnvio, String nombre, String direccion) {
        ContentValues values = new ContentValues();
        values.put("ruc_empresa", rucEmpresa);
        values.put("codigo_menu", codigoMenu);
        values.put("fecha", fecha);
        values.put("monto_envio", montoEnvio);
        values.put("nombre", nombre);
        values.put("direccion", direccion);
        long id = getWritableDatabase().insertOrThrow(TABLE_PEDIDOS, null, values);
        syncManager.sincronizarPedido(rucEmpresa, codigoMenu, fecha, montoEnvio, nombre, direccion);
        return id;
    }

    //Metodo para anexar el pago
    public long insertPago(long codigoPago, long codigoPedido, String numeroOrigen,
                           String numeroDestino, double monto, byte[] captura) {
        ContentValues values = new ContentValues();
        values.put("codigo_pago", codigoPago);
        values.put("codigo_pedido", codigoPedido);
        values.put("numero_origen", numeroOrigen);
        values.put("numero_destino", numeroDestino);
        values.put("monto", monto);
        values.put("captura", captura);
        long id = getWritableDatabase().insertOrThrow(TABLE_PAGOS, null, values);
        syncManager.sincronizarPago(codigoPago, codigoPedido, numeroOrigen, numeroDestino, monto, captura);
        return id;
    }

    //Metodo para leer las empresas y mostrarlas en los Spinner de menu y pedido
    public List<SpinnerOption> getEmpresaOptions() {
        List<SpinnerOption> options = new ArrayList<>();
        options.add(new SpinnerOption("", "Seleccione una empresa"));

        Cursor cursor = getReadableDatabase().query(
                TABLE_EMPRESAS,
                new String[]{"ruc", "nombre"},
                null,
                null,
                null,
                null,
                "nombre ASC"
        );

        try {
            while (cursor.moveToNext()) {
                String ruc = cursor.getString(cursor.getColumnIndexOrThrow("ruc"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                options.add(new SpinnerOption(ruc, nombre + " - " + ruc));
            }
        } finally {
            cursor.close();
        }

        return options;
    }

    //Metodo para leer los menus en el Spinner de agregar pedido
    public List<SpinnerOption> getMenuOptions(String rucEmpresa) {
        List<SpinnerOption> options = new ArrayList<>();
        options.add(new SpinnerOption("", "Seleccione un menu"));

        String selection = null;
        String[] selectionArgs = null;
        if (rucEmpresa != null && !rucEmpresa.isEmpty()) {
            selection = "ruc_empresa = ?";
            selectionArgs = new String[]{rucEmpresa};
        }

        Cursor cursor = getReadableDatabase().query(
                TABLE_MENUS,
                new String[]{"codigo_menu", "nombre"},
                selection,
                selectionArgs,
                null,
                null,
                "nombre ASC"
        );

        try {
            while (cursor.moveToNext()) {
                String codigo = cursor.getString(cursor.getColumnIndexOrThrow("codigo_menu"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                options.add(new SpinnerOption(codigo, nombre + " - " + codigo));
            }
        } finally {
            cursor.close();
        }

        return options;
    }

    //Metodo para leer el contenido del menu y mostrarlo en el textView de agregar pedido
    public String getMenuContenido(String codigoMenu) {
        if (codigoMenu == null || codigoMenu.isEmpty()) {
            return "";
        }

        Cursor cursor = getReadableDatabase().query(
                TABLE_MENUS,
                new String[]{"contenido"},
                "codigo_menu = ?",
                new String[]{codigoMenu},
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("contenido"));
            }
            return "";
        } finally {
            cursor.close();
        }
    }

    //Metodo para recuperar el precio del menu seleccionado
    public String getMenuPrecio(String codigoMenu) {
        if (codigoMenu == null || codigoMenu.isEmpty()) {
            return "";
        }

        Cursor cursor = getReadableDatabase().query(
                TABLE_MENUS,
                new String[] {"precio"},
                "codigo_menu = ?",
                new String[] {codigoMenu},
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("precio"));
            }
            return "";
        } finally {
            cursor.close();
        }
    }

    //Metodo para listar las empresas en los cardview
    public List<String[]> getEmpresasTableRows() {
        List<String[]> rows = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(
                TABLE_EMPRESAS,
                new String[]{"ruc", "nombre", "direccion", "categoria", "telefono", "correo"},
                null,
                null,
                null,
                null,
                "nombre ASC"
        );

        try {
            while (cursor.moveToNext()) {
                rows.add(new String[]{
                        cursor.getString(cursor.getColumnIndexOrThrow("ruc")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                        cursor.getString(cursor.getColumnIndexOrThrow("direccion")),
                        cursor.getString(cursor.getColumnIndexOrThrow("categoria")),
                        cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                        cursor.getString(cursor.getColumnIndexOrThrow("correo"))
                });
            }
        } finally {
            cursor.close();
        }

        return rows;
    }

    //Metodo para listar los menús en los cardview
    public List<String[]> getMenusTableRows(String rucEmpresa) {
        List<String[]> rows = new ArrayList<>();
        String selection = null;
        String[] selectionArgs = null;
        if (rucEmpresa != null && !rucEmpresa.isEmpty()) {
            selection = TABLE_MENUS + ".ruc_empresa = ?";
            selectionArgs = new String[]{rucEmpresa};
        }

        String query = "SELECT menus.codigo_menu, empresas.nombre AS empresa, menus.nombre, "
                + "menus.contenido, menus.precio "
                + "FROM " + TABLE_MENUS + " "
                + "INNER JOIN " + TABLE_EMPRESAS + " ON empresas.ruc = menus.ruc_empresa "
                + (selection == null ? "" : "WHERE " + selection + " ")
                + "ORDER BY menus.nombre ASC";

        Cursor cursor = getReadableDatabase().rawQuery(query, selectionArgs);
        try {
            while (cursor.moveToNext()) {
                rows.add(new String[]{
                        cursor.getString(cursor.getColumnIndexOrThrow("codigo_menu")),
                        cursor.getString(cursor.getColumnIndexOrThrow("empresa")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                        cursor.getString(cursor.getColumnIndexOrThrow("contenido")),
                        String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("precio")))
                });
            }
        } finally {
            cursor.close();
        }

        return rows;
    }

    //Metodo para listar los pedidos en una tabla
    public List<String[]> getPedidosTableRows(String rucEmpresa) {
        List<String[]> rows = new ArrayList<>();
        String selection = null;
        String[] selectionArgs = null;
        if (rucEmpresa != null && !rucEmpresa.isEmpty()) {
            selection = TABLE_PEDIDOS + ".ruc_empresa = ?";
            selectionArgs = new String[]{rucEmpresa};
        }

        String query = "SELECT pedidos.codigo_pedido, empresas.nombre AS empresa, menus.nombre AS menu, "
                + "pedidos.nombre AS cliente "
                + "FROM " + TABLE_PEDIDOS + " "
                + "INNER JOIN " + TABLE_EMPRESAS + " ON empresas.ruc = pedidos.ruc_empresa "
                + "INNER JOIN " + TABLE_MENUS + " ON menus.codigo_menu = pedidos.codigo_menu "
                + (selection == null ? "" : "WHERE " + selection + " ")
                + "ORDER BY pedidos.codigo_pedido DESC";

        Cursor cursor = getReadableDatabase().rawQuery(query, selectionArgs);
        try {
            while (cursor.moveToNext()) {
                rows.add(new String[]{
                        String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("codigo_pedido"))),
                        cursor.getString(cursor.getColumnIndexOrThrow("empresa")),
                        cursor.getString(cursor.getColumnIndexOrThrow("menu")),
                        cursor.getString(cursor.getColumnIndexOrThrow("cliente"))
                });
            }
        } finally {
            cursor.close();
        }

        return rows;
    }

    //Metodo para mostrar los detalles de cada pedido
    public String[] getPedidoDetalleByCodigo(long codigoPedido) {
        String query = "SELECT pedidos.codigo_pedido, pedidos.fecha, pedidos.nombre AS cliente, "
                + "pedidos.direccion, pedidos.monto_envio, empresas.nombre AS empresa, "
                + "menus.nombre AS menu, menus.contenido, menus.precio, pagos.codigo_pago, "
                + "pagos.numero_origen, pagos.numero_destino, pagos.monto, pagos.captura "
                + "FROM " + TABLE_PEDIDOS + " "
                + "INNER JOIN " + TABLE_EMPRESAS + " ON empresas.ruc = pedidos.ruc_empresa "
                + "INNER JOIN " + TABLE_MENUS + " ON menus.codigo_menu = pedidos.codigo_menu "
                + "LEFT JOIN " + TABLE_PAGOS + " ON pagos.codigo_pedido = pedidos.codigo_pedido "
                + "WHERE pedidos.codigo_pedido = ?";

        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(codigoPedido)});
        try {
            if (cursor.moveToFirst()) {
                return new String[]{
                        String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("codigo_pedido"))),
                        cursor.getString(cursor.getColumnIndexOrThrow("fecha")),
                        cursor.getString(cursor.getColumnIndexOrThrow("empresa")),
                        cursor.getString(cursor.getColumnIndexOrThrow("menu")),
                        cursor.getString(cursor.getColumnIndexOrThrow("contenido")),
                        String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("precio"))),
                        cursor.getString(cursor.getColumnIndexOrThrow("cliente")),
                        cursor.getString(cursor.getColumnIndexOrThrow("direccion")),
                        String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("monto_envio"))),
                        getNullableString(cursor, "codigo_pago"),
                        getNullableString(cursor, "numero_origen"),
                        getNullableString(cursor, "numero_destino"),
                        getNullableDouble(cursor, "monto"),
                        getBlobAsBase64(cursor, "captura")
                };
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    private String getNullableString(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndexOrThrow(columnName);
        return cursor.isNull(columnIndex) ? "" : cursor.getString(columnIndex);
    }

    private String getNullableDouble(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndexOrThrow(columnName);
        return cursor.isNull(columnIndex) ? "" : String.valueOf(cursor.getDouble(columnIndex));
    }

    //Medoto auxiliar para recuperar el BLOB de la imagen como texto
    private String getBlobAsBase64(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);

        if (index == -1 || cursor.isNull(index)) {
            return null;
        }

        byte[] blob = cursor.getBlob(index);

        return Base64.encodeToString(blob, Base64.DEFAULT);
    }
}
