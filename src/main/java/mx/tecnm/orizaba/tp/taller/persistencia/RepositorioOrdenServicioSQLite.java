package mx.tecnm.orizaba.tp.taller.persistencia;

import mx.tecnm.orizaba.tp.taller.modelo.Cliente;
import mx.tecnm.orizaba.tp.taller.modelo.Concepto;
import mx.tecnm.orizaba.tp.taller.modelo.Folio;
import mx.tecnm.orizaba.tp.taller.modelo.Estado;
import mx.tecnm.orizaba.tp.taller.modelo.OrdenServicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JDBC de órdenes de servicio y sus líneas. Guardar es <strong>atómico</strong>:
 * encabezado y líneas se escriben en una sola transacción (commit/rollback).
 */
public class RepositorioOrdenServicioSQLite implements Repositorio<OrdenServicio, Integer> {

    private final ConexionSQLite bd;
    private final Repositorio<Cliente, Integer> clientes;
    private final Repositorio<Concepto, Folio> elementos;

    public RepositorioOrdenServicioSQLite(ConexionSQLite bd, Repositorio<Cliente, Integer> clientes,
                         Repositorio<Concepto, Folio> elementos) {
        this.bd = bd;
        this.clientes = clientes;
        this.elementos = elementos;
    }

    @Override
    public void guardar(OrdenServicio t) {
        // TODO E13: En bd.enTransaccion: upsert del encabezado, borrar sus líneas e insertarlas en lote (addBatch/executeBatch).
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<OrdenServicio> buscar(Integer id) {
        // TODO E13: Leer el encabezado, resolver el actor y los elementos con sus repositorios, agregar líneas y restaurar el estado.
        throw new UnsupportedOperationException("TODO E13");
    }

    private OrdenServicio reconstruir(Connection c, ResultSet rs) throws SQLException {
        // TODO E13: Construir el objeto en estado ABIERTA, agregar las líneas y al final restaurarEstado(estado leído).
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<OrdenServicio> todos() {
        // TODO E13: Recorrer los ids ordenados y reconstruir cada registro.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(Integer id) {
        // TODO E13: DELETE del encabezado (las líneas se eliminan en cascada).
        throw new UnsupportedOperationException("TODO E13");
    }
}
