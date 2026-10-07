package mx.tecnm.orizaba.tp.taller.modelo;

import java.util.Comparator;

/**
 * Abstracción raíz del dominio: todo lo que puede cotizarse expone una clave,
 * un nombre, un precio base y una tarifa por unidad (unidades).
 * <p>Abstracción (Booch): define el <em>qué</em>, no el <em>cómo</em>
 * (véase {@code java_4_modelo/t1_abstraccion/Abstraccion03_interfaces}).</p>
 */
public interface Cotizable {

    /** Constante de interfaz: implícitamente {@code public static final}. */
    double TASA_IMPUESTO = 0.16;

    Folio clave();

    String nombre();

    double precioBase();

    /** Tarifa (sin impuestos) para la cantidad de unidades indicada. */
    double tarifa(int unidades);

    /** Método por defecto (extensión de la interfaz sin romper implementaciones). */
    default double tarifaConImpuesto(int unidades) {
        double total = tarifa(unidades) * (1 + TASA_IMPUESTO);
        return Math.round(total * 100) / 100.00;
        // TODO E02: Aplicar TASA_IMPUESTO a tarifa(unidades) y redondear a 2 decimales.
        //throw new UnsupportedOperationException("TODO E02");
    }

    /** Método estático de interfaz: comparador por nombre (referencia a método de objeto arbitrario). */
    static Comparator<Cotizable> porNombre() {
        return Comparator.comparing(
                Cotizable::nombre, String.CASE_INSENSITIVE_ORDER);
        // TODO E02: Usar Comparator.comparing(Cotizable::nombre, String.CASE_INSENSITIVE_ORDER).
        //throw new UnsupportedOperationException("TODO E02");
    }
}
