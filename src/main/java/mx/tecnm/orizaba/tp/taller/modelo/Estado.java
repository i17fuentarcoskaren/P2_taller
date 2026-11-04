package mx.tecnm.orizaba.tp.taller.modelo;

/**
 * Ciclo de vida de un(a) orden de servicio: máquina de estados finita.
 * <pre>ABIERTA → EN_CURSO → CERRADA;  ABIERTA|EN_CURSO → CANCELADA</pre>
 */
public enum Estado {
    ABIERTA("Recibida"), EN_CURSO("En reparación"), CERRADA("Entregada"), CANCELADA("Cancelada");

    private final String etiqueta;

    Estado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }

    /** Indica si la transición hacia {@code destino} es válida (switch como expresión). */
    public boolean permiteTransicionA(Estado destino) {
        // TODO E07: Implementar con switch-expresión las transiciones del diagrama; los estados terminales no permiten ninguna.
        return switch (this){
            case ABIERTA -> destino==Estado.EN_CURSO || destino == Estado.CANCELADA;
            case EN_CURSO -> destino == Estado.CERRADA || destino == Estado.CANCELADA;
            case CERRADA, CANCELADA -> false;
        };
        //throw new UnsupportedOperationException("TODO E07");
    }

    /**
     * Comprueba si el estado actual corresponde a un estado terminal (sin transiciones posteriores).
     *
     * @return {@code true} si es CERRADA o CANCELADA; {@code false} en caso contrario.
     */

    public boolean esTerminal() {
        return this == CERRADA || this == CANCELADA;
    }
    /**
     * Devuelve la etiqueta descriptiva del estado como su representación en cadena.
     *
     * @return Cadena con la etiqueta del estado.
     */

    @Override
    public String toString() {
        return etiqueta;
    }
}
