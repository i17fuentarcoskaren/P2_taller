package mx.tecnm.orizaba.tp.taller.persistencia;

import mx.tecnm.orizaba.tp.taller.modelo.Concepto;
import mx.tecnm.orizaba.tp.taller.modelo.Folio;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio JDBC de conceptos (tabla de herencia única: columna {@code tipo}).
 * <ul>
 *   <li>Escritura: {@code switch} con <em>pattern matching</em> exhaustivo sobre la jerarquía sellada.</li>
 *   <li>Lectura: mapa de reconstructores (lambdas) indexado por el nombre del subtipo.</li>
 * </ul>
 */
public class RepositorioConceptoSQLite implements Repositorio<Concepto, Folio> {

    /** Interfaz funcional privada anidada. */
    @FunctionalInterface
    private interface Reconstructor {
        Concepto crear(Folio clave, String nombre, double precio, String atributo);
    }

    private static final Map<String, Reconstructor> RECONSTRUCTORES = Map.of(
            "Refaccion", (c, n, p, a) -> new Concepto.Refaccion(c, n, p, a),
            "ManoDeObra", (c, n, p, a) -> new Concepto.ManoDeObra(c, n, p, Integer.parseInt(a)),
            "Diagnostico", (c, n, p, a) -> new Concepto.Diagnostico(c, n, p, a)
    );

    private final ConexionSQLite bd;
    private final MapeadorFila<Concepto> mapeador = rs -> {
        Reconstructor r = RECONSTRUCTORES.get(rs.getString("tipo"));
        if (r == null) {
            throw new SQLException("Tipo desconocido: " + rs.getString("tipo"));
        }
        return r.crear(Folio.parse(rs.getString("clave")), rs.getString("nombre"),
                rs.getDouble("precio_base"), rs.getString("atributo"));
    };

    public RepositorioConceptoSQLite(ConexionSQLite bd) {
        this.bd = bd;
    }

    /** Atributo específico del subtipo, obtenido con pattern matching. */
    static String atributoDe(Concepto e) {
        // TODO E13: switch (e) { case Concepto.Refaccion x -> ...; ... } sin default (la jerarquía es sellada).
        return switch (e) {
            case Concepto.Refaccion x-> x.marca(); //ajustar al campo real de mi modelo de refaccion
            case Concepto.ManoDeObra m -> String.valueOf(m.nivelTecnico()); // Ajusta al campo real de tu modelo ManoDeObra
            case Concepto.Diagnostico d -> d.equipo(); // Ajusta al campo real de tu modelo Diagnostico
        };
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public void guardar(Concepto e) {
        // TODO E13: INSERT ... ON CONFLICT(clave) DO UPDATE ... con PreparedStatement.
        String sql = """
                INSERT INTO concepto (clave, tipo, nombre, precio_base, atributo)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(clave) DO UPDATE SET
                nombre = excluded.nombre,
                precio_base = excluded.precio_base,
                atributo = excluded.atributo
                """;
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)){
            // Asignar los cinco valores
            ps.setString(1, e.clave().toString());
            ps.setString(2, e.tipo());
            ps.setString(3, e.nombre());
            ps.setDouble(4, e.precioBase()); // o precioBase() según esté definido en tu modelo Concepto
            ps.setString(5, atributoDe(e));

            //Ejecutar INSERT o UPDATE
            ps.executeUpdate();
        }
        catch (SQLException error){
            throw new PersistenciaException("Error al guardar el concepto", error);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Concepto> buscar(Folio clave) {
        // TODO E13: SELECT por clave; mapear con mapeador si hay fila.
        String sql = """
                SELECT clave, tipo, nombre, precio_base, atributo
                FROM concepto
                WHERE clave = ?
                """;
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)){
            ps.setString(1, clave.toString());
            //ejecutar la consulta
            try (ResultSet rs = ps.executeQuery()){
                // Comprobar si existe una fila
                if (rs.next()) {
                    Concepto concepto = mapeador.mapear(rs);
                    return Optional.of(concepto);
                }
                return Optional.empty();
            }
        }
        catch (SQLException error){
            throw new PersistenciaException("Error al buscar el concepto", error);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Concepto> todos() {

        // Crear una lista vacía
        List<Concepto> conceptos = new ArrayList<>();

        // Consulta SQL
        String sql = """
            SELECT clave, tipo, nombre, precio_base, atributo
            FROM concepto
            ORDER BY clave
            """;

        // Preparar y ejecutar la consulta
        try (PreparedStatement ps =
                     bd.conexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorrer todas las filas
            while (rs.next()) {
                conceptos.add(mapeador.mapear(rs));
            }

            return conceptos;

        } catch (SQLException error) {
            throw new PersistenciaException(
                    "Error al consultar los conceptos", error);
        }
    }

    @Override
    public boolean eliminar(Folio clave) {
        // TODO E13: DELETE por clave; devolver si se afectó alguna fila.
        String sql = "DELETE FROM concepto WHERE clave = ?";
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)){
            ps.setString(1, clave.toString());
            int filaAfectadas = ps.executeUpdate();
            return filaAfectadas > 0;
        }
        catch (SQLException error){
            throw new PersistenciaException("Error al eliminar el concepto", error);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }
}
