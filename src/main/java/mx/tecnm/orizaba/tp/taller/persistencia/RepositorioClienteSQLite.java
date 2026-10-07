package mx.tecnm.orizaba.tp.taller.persistencia;

import mx.tecnm.orizaba.tp.taller.modelo.Cliente;
import mx.tecnm.orizaba.tp.taller.modelo.Nivel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Repositorio JDBC de clientes. */
public class RepositorioClienteSQLite implements Repositorio<Cliente, Integer> {

    private final ConexionSQLite bd;
    private final MapeadorFila<Cliente> mapeador = rs -> new Cliente(rs.getInt("id"),
            rs.getString("nombre"), rs.getString("correo"), Nivel.valueOf(rs.getString("nivel")));

    public RepositorioClienteSQLite(ConexionSQLite bd) {
        this.bd = bd;
    }

    @Override
    public void guardar(Cliente a) {
        // TODO E13: Upsert por id con PreparedStatement.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Cliente> buscar(Integer id) {
        // TODO E13: SELECT por id.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Cliente> todos() {
        // TODO E13: SELECT ordenado por id.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(Integer id) {
        // TODO E13: DELETE por id.
        throw new UnsupportedOperationException("TODO E13");
    }
}
