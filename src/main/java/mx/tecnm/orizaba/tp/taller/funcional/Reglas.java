package mx.tecnm.orizaba.tp.taller.funcional;

import mx.tecnm.orizaba.tp.taller.modelo.Concepto;

import java.util.Locale;

/**
 * Biblioteca de reglas reutilizables (clase utilitaria no instanciable).
 */
public final class Reglas {

    /**
     * Constructor privado para prevenir la instanciación de esta clase utilitaria.
     *
     * @throws AssertionError Siempre que se intente invocar mediante reflexión.
     */

    private Reglas() {
        throw new AssertionError("No instanciable");
    }

    /**
     * Crea una regla para filtrar conceptos cuyo precio base sea menor o igual al valor máximo especificado.
     *
     * @param maximo El precio base límite permitido.
     * @return Una {@link Regla} que evalúa si el precio base es menor o igual al máximo.
     */

    public static Regla<Concepto> precioHasta(double maximo) {
        // TODO E11: Lambda que acepta los elementos cuyo precioBase() sea menor o igual que maximo.

        // Lambda que acepta los elementos cuyo precioBase() sea menor o igual que maximo.
        return c -> c.precioBase() <= maximo;
        //throw new UnsupportedOperationException("TODO E11");
    }

    /**
     * Crea una regla para filtrar conceptos cuyo nombre contenga el texto especificado,
     * ignorando diferencias entre mayúsculas y minúsculas.
     *
     * @param texto El texto o subcadena que se desea buscar en el nombre del concepto.
     * @return Una {@link Regla} que evalúa si el nombre contiene el texto dado (insensible a mayúsculas/minúsculas).
     */

    public static Regla<Concepto> nombreContiene(String texto) {
        // TODO E11: Lambda insensible a mayúsculas/minúsculas sobre nombre().
        return concepto -> concepto.nombre().toLowerCase().contains(texto.toLowerCase());
        //throw new UnsupportedOperationException("TODO E11");
    }

    /** Elementos que implementan una capacidad (p. ej. {@code ConGarantia.class}). */
    public static Regla<Concepto> conCapacidad(Class<?> capacidad) {
        // TODO E11: Referencia a método de instancia particular: capacidad::isInstance.
        return capacidad::isInstance;
        //throw new UnsupportedOperationException("TODO E11");
    }
}
