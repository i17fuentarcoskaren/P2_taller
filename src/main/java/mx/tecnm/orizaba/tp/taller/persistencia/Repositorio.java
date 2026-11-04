package mx.tecnm.orizaba.tp.taller.persistencia;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico de repositorio (patrón Repository).
 * Contiene un <strong>tipo miembro de interfaz</strong> ({@link MapeadorFila}), implícitamente
 * {@code public static} (véase {@code java_3_tipos_internos/t2_jerarquias/J05_Tipos_Miembro_Interfaz}).
 *
 * @param <T> entidad
 * @param <K> clave
 */
public interface Repositorio<T, K> {

    /**
     * Guarda o actualiza una entidad en el repositorio.
     *
     * @param entidad La entidad que se va a guardar.
     */

    void guardar(T entidad);
    /**
     * Busca y recupera una entidad utilizando su clave única.
     *
     * @param clave La clave o identificador de la entidad a buscar.
     * @return Un {@link Optional} que contiene la entidad si es encontrada; o vacío en caso contrario.
     */

    Optional<T> buscar(K clave);
    /**
     * Obtiene una lista con todas las entidades almacenadas en el repositorio.
     *
     * @return Una lista de entidades.
     */

    List<T> todos();

    /**
     * Elimina una entidad del repositorio basándose en su clave única.
     *
     * @param clave La clave de la entidad a eliminar.
     * @return {@code true} si la entidad fue encontrada y eliminada con éxito; {@code false} en caso contrario.
     */

    boolean eliminar(K clave);

    default boolean existe(K clave) {
        return buscar(clave).isPresent();
    }

    default void guardarTodos(Iterable<? extends T> entidades) {
        entidades.forEach(this::guardar);
    }

    /** Convierte la fila actual de un {@link ResultSet} en un objeto. */
    @FunctionalInterface
    interface MapeadorFila<R> {
        R mapear(ResultSet rs) throws SQLException;
    }
}
