package mx.tecnm.orizaba.tp.taller.funcional;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Interfaz funcional propia (un solo método abstracto) con métodos por defecto de composición
 * (véase {@code java_5_lambdas/b_interfaces_funcionales} y {@code d_orden_superior/Superior2}).
 *
 * @param <T> tipo evaluado
 */
@FunctionalInterface
public interface Regla<T> {

    /**
     * Evalúa una condición sobre el elemento proporcionado.
     *
     * @param t El elemento de tipo {@code T} a evaluar.
     * @return {@code true} si se cumple la condición; {@code false} en caso contrario.
     */

    boolean evaluar(T t);

    /**
     * Compone la regla actual con otra regla mediante una operación lógica AND (Y).
     *
     * @param otra La otra regla a combinar. No debe ser nula.
     * @return Una nueva regla compuesta que se cumple si ambas reglas se cumplen.
     * @throws NullPointerException si {@code otra} es nula.
     */

    default Regla<T> y(Regla<? super T> otra) {
        // TODO E11: Devolver la lambda t -> evaluar(t) && otra.evaluar(t) (validar otra no nula).
        Objects.requireNonNull(otra, "La otra regla no puede ser nula");
        return t -> evaluar(t) && otra.evaluar(t);
        //throw new UnsupportedOperationException("TODO E11");
    }

    /**
     * Compone la regla actual con otra regla mediante una operación lógica OR (O).
     *
     * @param otra La otra regla a combinar. No debe ser nula.
     * @return Una nueva regla compuesta que se cumple si al menos una de las reglas se cumple.
     * @throws NullPointerException si {@code otra} es nula.
     */

    default Regla<T> o(Regla<? super T> otra) {
        // TODO E11: Devolver la lambda t -> evaluar(t) || otra.evaluar(t).
        Objects.requireNonNull(otra, "La otra regla no puede ser nula");
        return t->evaluar(t) || otra.evaluar(t);
        //throw new UnsupportedOperationException("TODO E11");
    }

    /**
     * Niega la regla actual mediante una operación lógica NOT (NO).
     *
     * @return Una nueva regla que se cumple cuando la regla actual no se cumple.
     */


    default Regla<T> negar() {
        // TODO E11: Devolver la lambda t -> !evaluar(t).
            return t->!evaluar(t);
        //throw new UnsupportedOperationException("TODO E11");
    }

    /** Adaptador hacia {@link Predicate} mediante referencia a método de instancia particular. */
    default Predicate<T> comoPredicado() {
        return this::evaluar;
    }

    static <T> Regla<T> todas() {
        return t -> true;
    }

    /** Adaptador desde {@link Predicate}. */
    static <T> Regla<T> de(Predicate<? super T> p) {
        return p::test;
    }
}
