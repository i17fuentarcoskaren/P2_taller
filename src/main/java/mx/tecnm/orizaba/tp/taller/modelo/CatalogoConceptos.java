package mx.tecnm.orizaba.tp.taller.modelo;

import mx.tecnm.orizaba.tp.taller.funcional.Regla;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * CatalogoConceptos: <strong>agregación</strong> de conceptos (las partes existen con independencia del
 * todo; véase {@code java_4_modelo/t4_jerarquia/t2_agregacion/Jerarquia02}).
 * Reúne en un solo lugar tres variantes de tipos internos:
 * clase interna privada ({@link Iterador}), clase anónima ({@link #porTarifa(int)}) y clase local
 * ({@link #resumen()}).
 */
public class CatalogoConceptos implements Iterable<Concepto>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<Folio, Concepto> elementos = new LinkedHashMap<>();

    /** Constructor con número variable de argumentos. */
    public CatalogoConceptos(Concepto... iniciales) {
        agregar(iniciales);
    }

    public final void agregar(Concepto... nuevos) {
        // TODO E09: Recorrer los argumentos; rechazar nulos y claves duplicadas (ReglaNegocioException); usar putIfAbsent.
        for (Concepto concepto: nuevos){
            //rechazar conceptos nulos
            if(concepto == null){
                throw new ReglaNegocioException("Concepto nulo");
            }
            //insertar mediante putIfAbsent
            Concepto anterior = elementos.putIfAbsent(concepto.clave(), concepto);
            //rechazar si la clave ya existia
            if (anterior != null){
                throw new ReglaNegocioException("Concepto duplicado");
            }
        }//throw new UnsupportedOperationException("TODO E09");
    }

    public Optional<Concepto> buscar(Folio clave) {
        return Optional.ofNullable(elementos.get(clave));
    }

    public boolean retirar(Folio clave) {
        return elementos.remove(clave) != null;
    }

    public int tamanio() {
        return elementos.size();
    }

    /** Filtrado con la interfaz funcional propia {@link Regla}. */
    public List<Concepto> filtrar(Regla<? super Concepto> regla) {
        // TODO E09: Stream de los valores filtrado con regla::evaluar y recolectado con toList().
        //obtener evaluar y recolectado con toList()
        return elementos.values().stream().filter(regla::evaluar).toList();
        //throw new UnsupportedOperationException("TODO E09");
    }

    /** Filtrado por subtipo con genéricos acotados: {@code deTipo(Concepto.Refaccion.class)}. */
    public <E extends Concepto> List<E> deTipo(Class<E> tipo) {
        // TODO E09: Usar filter(tipo::isInstance) y map(tipo::cast).
        return elementos.values().stream().filter(tipo::isInstance).map(tipo::cast).toList();
        //throw new UnsupportedOperationException("TODO E09");
    }

    public List<Concepto> ordenados(Comparator<? super Concepto> criterio) {
        return elementos.values().stream().sorted(criterio).toList();
    }

    /**
     * Comparador implementado con una <strong>clase anónima</strong> que captura la variable
     * {@code unidades} (efectivamente final; véase {@code Parcel09}).
     */
    public static Comparator<Concepto> porTarifa(final int unidades) {
        // TODO E10: Devolver new Comparator<>() { compare(a, b) → Double.compare(a.tarifa(unidades), b.tarifa(unidades)) }.
        return  new Comparator<Concepto>() {
            @Override
            public int compare(Concepto a, Concepto b){
                return Double.compare(a.tarifa(unidades), b.tarifa(unidades));
            }
        };
        //throw new UnsupportedOperationException("TODO E10");
    }

    /** Resumen calculado con una <strong>clase local</strong> (véase {@code Parcel05}). */
    public String resumen() {
        // TODO E10: Declarar dentro del método la clase local Acumulador (cuenta, suma, mínimo, máximo) y recorrer el catálogo.
        class Acumulador {
            int cuenta = 0;
            double suma = 0;
            double minimo = Double.POSITIVE_INFINITY;
            double maximo = Double.NEGATIVE_INFINITY;
        }
        Acumulador acumulador = new Acumulador();
        for (Concepto concepto : elementos.values()){
            //obtener precio/tarifa base
            double precio = concepto.tarifa(1);
            //actualizar atributos del acumulador
            acumulador.cuenta++;
            acumulador.suma += precio;
            acumulador.minimo = Math.min(acumulador.minimo, precio);
            acumulador.maximo = Math.max(acumulador.maximo, precio);
        }
        //Devolver resumen auque el catalogo este vacio
        if (acumulador.cuenta == 0){
            acumulador.minimo = 0;
            acumulador.maximo = 0;
        }
        return String.format("%d conceptos, suma: %.2f, mínimo: %.2f, máximo: %.2f", acumulador.cuenta, acumulador.suma, acumulador.minimo, acumulador.maximo);
        //throw new UnsupportedOperationException("TODO E10");
    }

    @Override
    public Iterator<Concepto> iterator() {
        return new Iterador();
    }

    /**
     * Clase interna privada que implementa una interfaz pública: el cliente sólo conoce
     * {@code Iterator<Concepto>} y la implementación queda oculta (véase {@code Parcel04}).
     * Itera sobre una instantánea, de modo que el catálogo no puede modificarse a través de ella.
     */
    private class Iterador implements Iterator<Concepto> {
        private final List<Concepto> instantanea = new ArrayList<>(elementos.values());
        private int cursor;

        @Override
        public boolean hasNext() {
            return cursor < instantanea.size();
        }

        @Override
        public Concepto next() {
            // TODO E10: Lanzar NoSuchElementException si no hay siguiente;
            //Devolver el elemento y avanzar el cursor
            if(!hasNext()){
                throw new NoSuchElementException("No hay más elementos en el catalogo");
            }
            //Devolver el concepto
            return instantanea.get(cursor++);
            //throw new UnsupportedOpdevolver el elemento y avanzar el cursor.erationException("TODO E10");
        }
    }
}
