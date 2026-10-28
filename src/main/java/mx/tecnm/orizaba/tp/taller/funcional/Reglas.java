package mx.tecnm.orizaba.tp.taller.funcional;

import mx.tecnm.orizaba.tp.taller.modelo.Concepto;

import java.util.Locale;

/**
 * Biblioteca de reglas reutilizables (clase utilitaria no instanciable).
 */
public final class Reglas {

    private Reglas() {
        throw new AssertionError("No instanciable");
    }

    public static Regla<Concepto> precioHasta(double maximo) {
        // TODO E11: Lambda que acepta los elementos cuyo precioBase() sea menor o igual que maximo.
        return c -> c.precioBase() <= maximo;
        //throw new UnsupportedOperationException("TODO E11");
    }

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
