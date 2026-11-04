package mx.tecnm.orizaba.tp.taller.eventos;

import java.time.LocalDateTime;
import java.util.Objects;

/** Evento inmutable del dominio. */
public record Evento(TipoEvento tipo, String mensaje, LocalDateTime instante) {

    /**
     * Constructor compacto del record que valida que ninguno de los componentes
     * esenciales sea nulo para garantizar la consistencia del objeto.
     *
     * @throws NullPointerException si {@code tipo}, {@code mensaje} o {@code instante} son nulos.
     */

    public Evento {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(mensaje, "mensaje");
        Objects.requireNonNull(instante, "instante");
    }

    /** Fábrica estática (útil como referencia a método estático). */
    public static Evento de(TipoEvento tipo, String mensaje) {
        return new Evento(tipo, mensaje, LocalDateTime.now());
    }

    /**
     * Devuelve una representación en cadena del evento en formato {@code [TIPO] Mensaje}.
     *
     * @return Cadena formateada con el tipo y el mensaje del evento.
     */

    @Override
    public String toString() {
        return String.format("[%s] %s", tipo, mensaje);
    }
}
