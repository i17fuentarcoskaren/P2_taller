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
 * Repositorio JDBC de órdenes de servicio y sus líneas. Guardar es
 * <strong>atómico</strong>:
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
        String sqlOrden = """
                INSERT INTO orden (id, cliente_id, fecha, estado)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                cliente_id = excluded.cliente_id,
                fecha = excluded.fecha,
                estado = excluded.estado
                """;
        // Borrar líneas
        String sqlBorrarLineas = "DELETE FROM orden_linea WHERE orden_id = ?";

        // Corregido: Agregado el paréntesis de cierre que faltaba en (unidades)
        String sqlLinea = """
                INSERT INTO orden_linea (orden_id, clave_concepto, unidades)
                VALUES (?, ?, ?)
                """;

        bd.enTransaccion(c -> {
            try (PreparedStatement psOrden = c.prepareStatement(sqlOrden)) {
                psOrden.setInt(1, t.id());
                psOrden.setInt(2, t.cliente().id());
                psOrden.setString(3, t.fecha().toString());
                psOrden.setString(4, t.estado().name());
                psOrden.executeUpdate();
            }

            // Borrar líneas existentes para evitar duplicados al actualizar
            try (PreparedStatement psDelete = c.prepareStatement(sqlBorrarLineas)) {
                psDelete.setInt(1, t.id());
                psDelete.executeUpdate();
            }

            // Insertar las nuevas líneas en lote
            if (!t.lineas().isEmpty()) {
                try (PreparedStatement psLinea = c.prepareStatement(sqlLinea)) {
                    for (var linea : t.lineas()) {
                        psLinea.setInt(1, t.id());
                        psLinea.setString(2, linea.elemento().clave().toString());
                        psLinea.setInt(3, linea.unidades());
                        psLinea.addBatch();
                    }
                    psLinea.executeBatch();
                }
            }
            return null;
        });
    }

    @Override
    public Optional<OrdenServicio> buscar(Integer id) {
        // TODO E13: Leer el encabezado, resolver el actor y los elementos con sus repositorios, agregar líneas y restaurar el estado.
        String sql = "SELECT id, cliente_id, fecha, estado FROM orden WHERE id = ?";

        // Corregido: Sintaxis correcta de la lambda en enTransaccion
        return bd.enTransaccion(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(reconstruir(c, rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }

    private OrdenServicio reconstruir(Connection c, ResultSet rs) throws SQLException {
        // TODO E13: Construir el objeto en estado ABIERTA, agregar las líneas y al final restaurarEstado(estado leído).
        int id = rs.getInt("id");
        int clientId = rs.getInt("cliente_id");
        LocalDate fecha = LocalDate.parse(rs.getString("fecha"));
        String estadoStr = rs.getString("estado");

        // Resolver el cliente usando un repositorio
        Cliente cliente = clientes.buscar(clientId)
                .orElseThrow(() -> new SQLException("Cliente no encontrado con id " + clientId));

        // Crear la orden en estado inicial
        OrdenServicio orden = new OrdenServicio(id, cliente, fecha);

        // Consultar y agregar las líneas de la orden usando el repositorio 'elementos'
        String sqlLineas = "SELECT clave_concepto, unidades FROM orden_linea WHERE orden_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sqlLineas)) {
            ps.setInt(1, id);
            try (ResultSet rsLineas = ps.executeQuery()) {
                // Corregido: Eliminado el doble while anidado redundante
                while (rsLineas.next()) {
                    Folio claveConcepto = Folio.parse(rsLineas.getString("clave_concepto"));
                    int unidades = rsLineas.getInt("unidades");

                    Concepto elemento = elementos.buscar(claveConcepto)
                            .orElseThrow(() -> new SQLException("Concepto no encontrado con clave: " + claveConcepto));

                    orden.agregar(elemento, unidades);
                }
            }
        }

        // Restaurar el estado de la orden si es diferente al inicial
        Estado estado = Estado.valueOf(estadoStr);
        //restaurar estaod
        orden.restaurarEstado(estado);
        return orden;
    }

    @Override
    public List<OrdenServicio> todos() {
        // TODO E13: Recorrer los ids ordenados y reconstruir cada registro.
        String sql = "SELECT id, cliente_id, fecha, estado FROM orden ORDER BY id";

        // Corregido: Agregadas las llaves y paréntesis de cierre faltantes para la lambda y el método
        return bd.enTransaccion(c -> {
            List<OrdenServicio> ordenes = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ordenes.add(reconstruir(c, rs));
                }
            }
            return ordenes;
        });
    }

    @Override
    public boolean eliminar(Integer id) {
        // TODO E13: DELETE del encabezado (las líneas se eliminan en cascada).
        String sql = "DELETE FROM orden WHERE id = ?";

        return bd.enTransaccion(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        });
    }
}