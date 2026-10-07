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
        throw new UnsupportedOperationException("TODO E07");
    }

    public boolean esTerminal() {
        return this == CERRADA || this == CANCELADA;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
