package mx.tecnm.orizaba.tp.taller.modelo;

/**
 * Nivel del cliente; cada constante encapsula su etiqueta y su descuento.
 * <p>Tipificación fuerte: un {@code enum} es un tipo cerrado con estado y comportamiento.</p>
 */
public enum Nivel {
    /** Nivel de cliente ocasional sin descuento aplicable (0%). */
    OCASIONAL("Ocasional", 0.0),
    /** Nivel de cliente frecuente con un 8% de descuento aplicable. */
    FRECUENTE("Frecuente", 0.08),
    /** Nivel de cliente de flotilla con un 15% de descuento aplicable. */
    FLOTILLA("Flotilla", 0.15);

    /** Etiqueta descriptiva legible del nivel. */
    private final String etiqueta;
    /** Porcentaje de descuento asociado al nivel (expresado en decimal). */
    private final double descuento;

    /**
     * Constructor del enum que inicializa la etiqueta y el porcentaje de descuento.
     *
     * @param etiqueta  Etiqueta descriptiva del nivel.
     * @param descuento Porcentaje de descuento en formato decimal.
     */

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
        //calcular el monto de descuento
        monto = monto - (monto  * descuento);
        //redondear ese resultado a 2 decimales
        monto = Math.round(monto * 100) / 100.0;
        //devolver el resultado con rerturn
        return monto;
        //throw new UnsupportedOperationException("TODO E06");
    }

    /**
     * Devuelve la etiqueta descriptiva del nivel como su representación en cadena.
     *
     * @return Cadena con la etiqueta del nivel.
     */
    @Override
    public String toString() {
        return etiqueta;
    }
}
