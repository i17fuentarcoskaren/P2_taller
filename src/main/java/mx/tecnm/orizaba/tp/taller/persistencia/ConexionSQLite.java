package mx.tecnm.orizaba.tp.taller.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Administra la conexión JDBC con SQLite y el esquema de la base de datos.
 * <p>Proceso: el bloque estático carga el controlador la primera vez que la clase se inicializa
 * (véase {@code java_2_proceso/Proceso07}: uso activo de una clase).</p>
 */
public final class ConexionSQLite implements AutoCloseable {

    private static final String[] ESQUEMA = {
        """
        CREATE TABLE IF NOT EXISTS concepto (
            clave        TEXT PRIMARY KEY,
            tipo         TEXT NOT NULL,
            nombre       TEXT NOT NULL,
            precio_base  REAL NOT NULL CHECK (precio_base >= 0),
            atributo     TEXT NOT NULL
        )""",
        """
        CREATE TABLE IF NOT EXISTS cliente (
            id      INTEGER PRIMARY KEY,
            nombre  TEXT NOT NULL,
            correo  TEXT NOT NULL UNIQUE,
            nivel   TEXT NOT NULL
        )""",
        """
        CREATE TABLE IF NOT EXISTS orden (
            id        INTEGER PRIMARY KEY,
            cliente_id  INTEGER NOT NULL REFERENCES cliente(id),
            fecha     TEXT NOT NULL,
            estado    TEXT NOT NULL
        )""",
        """
        CREATE TABLE IF NOT EXISTS orden_linea (
            orden_id   INTEGER NOT NULL REFERENCES orden(id) ON DELETE CASCADE,
            clave_concepto TEXT NOT NULL REFERENCES concepto(clave),
            unidades        INTEGER NOT NULL CHECK (unidades > 0),
            PRIMARY KEY (orden_id, clave_concepto)
        )"""
    };

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("No se encontró el controlador sqlite-jdbc en el classpath");
        }
    }

    private final Connection conexion;

    /** @param rutaArchivo archivo .db (se crea si no existe) */
    public ConexionSQLite(String rutaArchivo) {
        // TODO E13: Abrir jdbc:sqlite:<ruta>, activar PRAGMA foreign_keys = ON y crear el esquema.
        try{
            //conexion de sqlite
            conexion = DriverManager.getConnection("jdbc:sqlite:" + rutaArchivo);
            //crear el statement
            try(Statement sentencia = conexion().createStatement()){
                sentencia.execute("PRAGMA foreign_keys = ON");
                for(String sql : ESQUEMA){
                    sentencia.execute(sql);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al iniciar SQlite", e);
            //throw new RuntimeException(e);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    public Connection conexion() {
        return conexion;
    }

    /**
     * Ejecuta un trabajo dentro de una transacción: confirma si termina bien y revierte ante error.
     */
    public <R> R enTransaccion(TrabajoSQL<R> trabajo) {
        // TODO E13: setAutoCommit(false); ejecutar; commit; ante SQLException rollback y PersistenciaException; finalmente setAutoCommit(true).
        try {
            //Desactivar la confirmación automatica
            conexion.setAutoCommit(false);
            //ejecutar el trabajo
            R resultado = trabajo.ejecutar(conexion);
            //confirmar con commit()
            conexion.commit();
            //devolver el resultado
            return resultado;
        }
        catch (SQLException e){
            try {
                conexion.rollback();
            }
            catch (SQLException errorRollback){
                e.addSuppressed(errorRollback);
            }
            throw new PersistenciaException("Error al ejecutar la transacción", e);
        }
        catch (RuntimeException e){
            //revertir si ocurre un error de ejecucion en tiempo de ejecucion
            try{
                conexion.rollback();
            } catch (SQLException errorRollback) {
                e.addSuppressed(errorRollback);
            }
            throw e;
        }
        finally {
            //restaurar la confirmacion automatica
            try{
                conexion.setAutoCommit(true);
            }
            catch (SQLException e){
                throw new PersistenciaException("Error al restaurar autocommit", e);
            }
        }

        //throw new UnsupportedOperationException("TODO E13");
    }

    /** Interfaz funcional miembro: trabajo que puede lanzar {@link SQLException}. */
    @FunctionalInterface
    public interface TrabajoSQL<R> {
        R ejecutar(Connection c) throws SQLException;
    }

    @Override
    public void close() {
        try {
            conexion.close();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al cerrar la conexión", e);
        }
    }
}
