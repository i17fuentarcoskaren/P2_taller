package mx.tecnm.orizaba.tp.taller.modelo;

/**
 * Interfaz de capacidad (clasificación múltiple, véase {@code java_4_modelo/t4_jerarquia/t1_clasificacion}).
 * Declara un método por defecto {@code descripcionCorta()} que colisiona deliberadamente con el de la otra
 * capacidad; quien implemente ambas debe resolver la colisión con {@code Interfaz.super.descripcionCorta()}
 * (véase {@code java_5_lambdas/a_interfaces_default/Default3}).
 */
public interface ConGarantia {
    /**
     * Obtiene el número de meses de garantía asociados al elemento.
     *
     * @return Cantidad de meses de garantía.
     */
    int mesesGarantia();

    /**
     * Proporciona una descripción corta predeterminada basada en los meses de garantía.
     *
     * @return Una cadena formateada que incluye el valor de los meses de garantía.
     */

    default String descripcionCorta() {
        // TODO E05: Devolver "Garantía (meses): " concatenado con el valor de mesesGarantia().
        return "Garantía (meses): " + mesesGarantia();
        //throw new UnsupportedOperationException("TODO E05");
    }
}
