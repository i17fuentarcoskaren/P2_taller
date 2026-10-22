package mx.tecnm.orizaba.tp.taller.modelo;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Objeto valor inmutable que identifica a un concepto (formato {@code ABC-0001}).
 * <ul>
 *   <li>Encapsulamiento: {@code record} con constructor compacto que valida invariantes.</li>
 *   <li>Proceso: bloque de inicialización estática (véase {@code java_2_proceso/Proceso03}).</li>
 *   <li>Concurrencia: secuencias atómicas seguras entre hilos.</li>
 * </ul>
 */
public record Folio(String prefijo, int numero) implements Comparable<Folio>, Serializable {

    private static final Map<String, AtomicInteger> SECUENCIAS;

    static {
        // Inicialización estática: se ejecuta una sola vez, al primer uso activo de la clase.
        SECUENCIAS = new ConcurrentHashMap<>();
    }

    public Folio {
        // TODO E03: Validar que el prefijo tenga exactamente 3 letras mayúsculas y que el número sea positivo (IllegalArgumentException).
        if (prefijo == null || !prefijo.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("El prefijo invalido");
        }
        if (numero <=0){
            throw new IllegalArgumentException("El número del folio debe ser positivo.");
        }
        //throw new UnsupportedOperationException("TODO E03");
    }

    /** Genera la siguiente clave del prefijo indicado (operación atómica). */
    public static Folio siguiente(String prefijo){
        // TODO E03: Obtener (o crear) la secuencia del prefijo con computeIfAbsent e incrementarla atómicamente.
        AtomicInteger secuencia = SECUENCIAS.computeIfAbsent(prefijo, valorprefijo -> new AtomicInteger(0));
        //Incrementa la secuencia y obtiene el nuevo número
        int numero = secuencia.incrementAndGet();
        //construir y devolver el nuevo folio
        return new Folio(prefijo, numero);
        //throw new UnsupportedOperationException("TODO E03");
    }

    /** Ajusta la secuencia para que nunca genere una clave ya existente (p. ej., tras leer la BD). */
    public static void registrar(Folio clave) {
        // TODO E03: Actualizar la secuencia del prefijo con accumulateAndGet(numero, Math::max).
        AtomicInteger secuencia = SECUENCIAS.computeIfAbsent(clave.prefijo(), valorprefijo-> new AtomicInteger(0));
        secuencia.accumulateAndGet(clave.numero(), Math::max);
        //throw new UnsupportedOperationException("TODO E03");
    }

    /** Convierte un texto {@code ABC-0001} en una clave. */
    public static Folio parse(String texto) {
        // TODO E03: Validar el formato con la expresión [A-Z]{3}-\d{4,} y construir la clave.
        if (texto == null || !texto.matches("[A-Z]{3}-\\d{4,}")){
            throw new IllegalArgumentException("Formato incorrecto");
        }
        //extraer prefijo
        String prefijo = texto.substring(0, 3);
        //Extraer y convertir el número
        String partenumerica = texto.substring(4);
        //convertir el String a int
        int numero = Integer.parseInt(partenumerica);
        //construir y devolver el objeto folio
        return new Folio(prefijo, numero);
        //throw new UnsupportedOperationException("TODO E03");
    }

    @Override
    public int compareTo(Folio otra) {
        // TODO E03: Ordenar primero por prefijo y después por número.
        int comparacion = prefijo.compareTo(otra.prefijo());

        //si los prefijos son diferentes vamod a devolver esa comparación
        if (comparacion !=0){
            return comparacion;
        }
        //si los prefijos son diferentes comparamos los números
        return Integer.compare(numero, otra.numero());
        //throw new UnsupportedOperationException("TODO E03");
    }

    @Override
    public String toString() {
        return String.format("%s-%04d", prefijo, numero);
    }
}
