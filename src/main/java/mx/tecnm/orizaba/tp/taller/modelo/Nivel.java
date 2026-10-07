package mx.tecnm.orizaba.tp.taller.modelo;

/**
 * Nivel del cliente; cada constante encapsula su etiqueta y su descuento.
 * <p>Tipificación fuerte: un {@code enum} es un tipo cerrado con estado y comportamiento.</p>
 */
public enum Nivel {
    OCASIONAL("Ocasional", 0.0),
    FRECUENTE("Frecuente", 0.08),
    FLOTILLA("Flotilla", 0.15);

    private final String etiqueta;
    private final double descuento;

    Nivel(String etiqueta, double descuento) {
        this.etiqueta = etiqueta;
        this.descuento = descuento;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public double descuento() {
        return descuento;
    }

    /** Aplica el descuento del nivel a un monto, redondeando a 2 decimales. */
    public double aplicar(double monto) {
        // TODO E06: Restar el porcentaje de descuento al monto y redondear a 2 decimales.
        throw new UnsupportedOperationException("TODO E06");
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
