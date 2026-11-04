package mx.tecnm.orizaba.tp.taller.persistencia;

import java.io.Serial;

/** Envuelve los errores de acceso a datos en una excepción no comprobada del dominio. */
public class PersistenciaException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Construye una nueva excepción de persistencia con el mensaje descriptivo y la causa subyacente especificados.
     *
     * @param mensaje Detalle explicativo sobre el error de acceso a datos.
     * @param causa   La excepción raíz o causa original que originó el fallo (por ejemplo, {@code SQLException}).
     */
    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
