package mx.tecnm.orizaba.tp.taller.modelo;

import java.io.Serial;

/**
 * Excepción no comprobada que señala la violación de una invariante del dominio.
 * <p>Encapsulamiento (Booch): los objetos protegen su estado rechazando valores inválidos.</p>
 */
public class ReglaNegocioException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Construye una nueva excepción de regla de negocio con el mensaje descriptivo especificado.
     *
     * @param mensaje Detalle explicativo sobre la violación de la regla de negocio.
     */

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
