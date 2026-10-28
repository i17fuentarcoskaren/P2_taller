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
        //SQL para insertart o actualizar cliente (Upsert)
        String sql= """
        INSERT INTO cliente (id, nombre, correo, nivel)
        VALUES (?, ?, ?, ?)
        ON CONFLICT(id) DO UPDATE SET
            nombre = excluded.nombre,
            correo = excluded.correo,
            nivel = excluded.nivel
        """;
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)) {
            //asignar parametros
            ps.setInt(1, a.id());
            ps.setString(2, a.nombre());
            ps.setString(3, a.correo());
            ps.setString(4, a.nivel().name());
            ps.executeUpdate();
        }
        catch (SQLException error){
            throw new PersistenciaException("Error al giardar el cliente", error);

        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Cliente> buscar(Integer id) {
        // TODO E13: SELECT por id.
        String sql = """
                SELECT id, nombre,correo, nivel
                FROM cliente
                WHERE id = ?
                """;
        try (PreparedStatement ps= bd.conexion().prepareStatement(sql)){
            ps.setInt(1, id);
            //ejecutar la consulta
            try (ResultSet rs = ps.executeQuery()){
                //comprobar si existe una fila
                if (rs.next()){
                    return Optional.of(mapeador.mapear(rs));
                }
                return Optional.empty();
            }
        }
        catch (SQLException error){
            throw new PersistenciaException("Error al buscar el cliente", error);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Cliente> todos() {
        // TODO E13: SELECT ordenado por id.
        //crear lista vacia
        List<Cliente> clientes = new ArrayList<>();
        String sql = """
                SELECT id, nombre, correo, nivel
                FROM cliente
                ORDER BY id
                """;
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql); ResultSet rs = ps.executeQuery()){
            // Recorrer las filas
            while (rs.next()){
                clientes.add(mapeador.mapear(rs));
            }
            return clientes;
        }
        catch (SQLException error){
            throw new PersistenciaException("Error al consultar los clientes", error);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(Integer id) {
        // TODO E13: DELETE por id.
        String sql = "DELETE FROM cliente WHERE id = ?";
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)){
            // Asignar identificador
            ps.setInt(1, id);
            // Eliminar
            int filasEliminadas = ps.executeUpdate();
            return filasEliminadas > 0;

        }
        catch (SQLException error){
            throw new PersistenciaException("Error al eliminar el cliente", error);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }
}
