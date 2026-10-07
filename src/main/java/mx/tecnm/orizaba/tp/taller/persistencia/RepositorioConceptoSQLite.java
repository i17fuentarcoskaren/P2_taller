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
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public void guardar(Concepto e) {
        // TODO E13: INSERT ... ON CONFLICT(clave) DO UPDATE ... con PreparedStatement.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Concepto> buscar(Folio clave) {
        // TODO E13: SELECT por clave; mapear con mapeador si hay fila.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Concepto> todos() {
        // TODO E13: SELECT ordenado por clave; mapear cada fila.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(Folio clave) {
        // TODO E13: DELETE por clave; devolver si se afectó alguna fila.
        throw new UnsupportedOperationException("TODO E13");
    }
}
