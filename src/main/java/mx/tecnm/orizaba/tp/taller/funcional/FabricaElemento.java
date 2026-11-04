package mx.tecnm.orizaba.tp.taller.funcional;

import mx.tecnm.orizaba.tp.taller.modelo.Concepto;

/**
 * Fábrica genérica acotada, diseñada para recibir <strong>referencias a constructor</strong>:
 * {@code FabricaElemento<Concepto.Refaccion, String> f = Concepto.Refaccion::new;}
 * (véase {@code java_5_lambdas/c_referencias_metodos/Referencias4}).
 *
 * @param <E> subtipo concreto de Concepto
 * @param <A> tipo del atributo específico del subtipo
 */
@FunctionalInterface
public interface FabricaElemento<E extends Concepto, A> {

    /**
     * Crea y retorna una nueva instancia del subtipo concreto de {@code Concepto}.
     *
     * @param nombre     Nombre descriptivo o clave del elemento.
     * @param precioBase Precio base inicial del elemento.
     * @param atributo   Atributo específico adicional requerido por el subtipo concreto.
     * @return Una nueva instancia del elemento de tipo {@code E}.
     */

    E crear(String nombre, double precioBase, A atributo);
}
