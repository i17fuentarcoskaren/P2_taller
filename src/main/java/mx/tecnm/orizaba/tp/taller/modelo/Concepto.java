package mx.tecnm.orizaba.tp.taller.modelo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase abstracta <strong>sellada</strong> que factoriza el estado y el comportamiento común de todo
 * concepto. Sus tres subtipos se declaran como clases anidadas estáticas finales
 * (véase {@code java_4_modelo/t1_abstraccion/Abstraccion04_anidadas}).
 * <ul>
 *   <li>Abstracción + Jerarquía: patrón <em>Template Method</em> ({@link #tarifa(int)} es final y delega en
 *       {@link #calcularTarifa(int)}).</li>
 *   <li>Proceso: bloque estático, bloque de instancia y encadenamiento {@code this(...)}/{@code super(...)}
 *       (véase {@code java_2_proceso/Proceso03}, {@code Proceso09}).</li>
 *   <li>Persistencia: {@code Serializable} con un campo {@code transient}
 *       (véase {@code java_4_modelo/t7_persistencia/a_serializacion/NoSerializar_09}).</li>
 * </ul>
 */
public abstract sealed class Concepto implements Cotizable, Serializable
        permits Concepto.Refaccion, Concepto.ManoDeObra, Concepto.Diagnostico {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final AtomicInteger INSTANCIAS_CREADAS;

    static {
        INSTANCIAS_CREADAS = new AtomicInteger();
    }

    private final Folio clave;
    private String nombre;
    private double precioBase;
    private final LocalDateTime creadoEn;
    /** No se serializa: tras recuperar un respaldo vuelve a 0. */
    private transient int cotizaciones;

    {
        // Bloque de inicialización de instancia: se ejecuta antes del cuerpo de cualquier constructor.
        creadoEn = LocalDateTime.now();
        INSTANCIAS_CREADAS.incrementAndGet();
    }

    /** Constructor para objetos nuevos: genera la clave y encadena con {@code this(...)}. */
    protected Concepto(String prefijo, String nombre, double precioBase) {
        this(Folio.siguiente(prefijo), nombre, precioBase);
    }

    /** Constructor para objetos reconstruidos (persistencia) con clave conocida. */
    protected Concepto(Folio clave, String nombre, double precioBase) {
        // TODO E04: Asignar la clave (no nula), registrarla en Folio.registrar y validar nombre y precio mediante los setters.
        this.clave=Objects.requireNonNull(clave);
        Folio.registrar(this.clave);
        //validar nombre
        setNombre(nombre);
        //Guardar precioBase dentro del constructor
        setPrecioBase(precioBase);
        //throw new UnsupportedOperationException("TODO E04");
    }

    public static int instanciasCreadas() {
        return INSTANCIAS_CREADAS.get();
    }

    @Override
    public final Folio clave() {
        return clave;
    }

    @Override
    public final String nombre() {
        return nombre;
    }

    @Override
    public final double precioBase() {
        return precioBase;
    }

    public final LocalDateTime creadoEn() {
        return creadoEn;
    }

    public final int cotizaciones() {
        return cotizaciones;
    }

    public final void setNombre(String nombre) {
        // TODO E04: Rechazar nombres nulos o en blanco (ReglaNegocioException); guardar el nombre sin espacios extremos.
        if (nombre == null || nombre.isBlank()){

            throw new ReglaNegocioException ("El nombre no puede estar vacío");
        }
        this.nombre = nombre.strip();
        //throw new UnsupportedOperationException("TODO E04");

    }

    public final void setPrecioBase(double precioBase) {
        // TODO E04: Rechazar precios negativos, NaN o infinitos (ReglaNegocioException).
        if (precioBase < 0 || !Double.isFinite(precioBase)){
            throw new ReglaNegocioException ("El precio base debe ser mayor o igual a cero");
        }
        this.precioBase= precioBase;
        //throw new UnsupportedOperationException("TODO E04");
    }

    /** Método plantilla: valida, contabiliza y redondea; la variación la aporta cada subtipo. */
    @Override
    public final double tarifa(int unidades){
        if (unidades <=0){
            throw new ReglaNegocioException ("Unidades no validas");
        }
        //Después se incrementa el contador
        cotizaciones ++;
        return Math.round(calcularTarifa(unidades) * 100.00)/ 100.00;
        // TODO E04: Validar unidades > 0 (ReglaNegocioException), incrementar cotizaciones y devolver calcularTarifa redondeada a 2 decimales.
        //throw new UnsupportedOperationException("TODO E04");
    }

    /** Punto de variación del Template Method. */
    protected abstract double calcularTarifa(int unidades);

    /** Copia con clave nueva; los subtipos la redefinen con <strong>retorno covariante</strong>. */
    public abstract Concepto copiar();

    /** Nombre corto del subtipo (útil para la UI y la persistencia). */
    public final String tipo() {
        return getClass().getSimpleName();
    }

    @Override
    public final boolean equals(Object o) {
        return this == o || (o instanceof Concepto otro && clave.equals(otro.clave));
    }

    @Override
    public final int hashCode() {
        return clave.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s[%s, %s, $%.2f]", tipo(), clave, nombre, precioBase);
    }

    /**
     * Refacción: precio unitario por pieza.
     * <p>Subtipo de Concepto declarado como clase anidada estática final; implementa ConGarantia.</p>
     */
    public static final class Refaccion extends Concepto implements ConGarantia {
        @Serial
        private static final long serialVersionUID = 1L;

        public static final String PREFIJO = "REF";

        private final String marca;

        public Refaccion(String nombre, double precioBase, String marca) {
            super(PREFIJO, nombre, precioBase);
            this.marca = validar(marca);
        }

        public Refaccion(Folio clave, String nombre, double precioBase, String marca) {
            super(clave, nombre, precioBase);
            this.marca = validar(marca);
        }

        private static String validar(String marca) {
            if(!(marca != null && !marca.isBlank())){
                throw new ReglaNegocioException ("La marca es obligatoria");
            }
            return marca;
            // TODO E05: Validar: marca != null && !marca.isBlank() (si no se cumple: ReglaNegocioException "La marca es obligatoria").
            //throw new UnsupportedOperationException("TODO E05");
        }

        public String marca() {
            return marca;
        }

        @Override
        protected double calcularTarifa(int unidades) {
            // TODO E05: Refacción: precio unitario por pieza. Fórmula: precioBase() * unidades
            //Regresa el metodo tarifa de la clase padre
            return precioBase() * unidades;
            //throw new UnsupportedOperationException("TODO E05");
        }

        /** Retorno covariante: devuelve Refaccion, no Concepto. */
        @Override
        public Refaccion copiar() {
            // TODO E05: Crear un nuevo Refaccion con el mismo nombre, precio y atributo (clave nueva).
            //Retorno covariente
            return new Refaccion(nombre(), precioBase(), marca());
            //throw new UnsupportedOperationException("TODO E05");
        }

        @Override
        public int mesesGarantia() {
            return 6;
        }

        @Override
        public String toString() {
            return super.toString() + " {marca=" + marca + "}";
        }
    }

    /**
     * Mano de obra: 10 % adicional por nivel técnico.
     * <p>Subtipo de Concepto declarado como clase anidada estática final; implementa Certificable.</p>
     */
    public static final class ManoDeObra extends Concepto implements Certificable {
        @Serial
        private static final long serialVersionUID = 1L;

        public static final String PREFIJO = "MOB";

        private final int nivelTecnico;

        public ManoDeObra(String nombre, double precioBase, int nivelTecnico) {
            super(PREFIJO, nombre, precioBase);
            this.nivelTecnico = validar(nivelTecnico);
        }

        public ManoDeObra(Folio clave, String nombre, double precioBase, int nivelTecnico) {
            super(clave, nombre, precioBase);
            this.nivelTecnico = validar(nivelTecnico);
        }

        private static int validar(int nivelTecnico) {
            // TODO E05: Validar: nivelTecnico >= 1 && nivelTecnico <= 3 (si no se cumple: ReglaNegocioException "El nivel técnico debe estar entre 1 y 3").
            // Validar que el nivel esté entre 1 y 3
            if (nivelTecnico < 1 || nivelTecnico > 3){
                throw new ReglaNegocioException("El nivel técnico debe estar entre 1 y 3");
            }
            // Si es válido, devolver el nivel
            return nivelTecnico;
            //throw new UnsupportedOperationException("TODO E05");
        }

        public int nivelTecnico() {
            return nivelTecnico;
        }

        @Override
        protected double calcularTarifa(int unidades) {
            // TODO E05: Mano de obra: 10 % adicional por nivel técnico. Fórmula: precioBase() * unidades * (1 + 0.10 * nivelTecnico)
            //Calcular el precio adicional por el nivel técnico
            return precioBase() * unidades * (1 + 0.10 * nivelTecnico);
            //throw new UnsupportedOperationException("TODO E05");
        }

        /** Retorno covariante: devuelve ManoDeObra, no Concepto. */
        @Override
        public ManoDeObra copiar() {
            // TODO E05: Crear un nuevo ManoDeObra con el mismo nombre, precio y atributo (clave nueva).
            return new ManoDeObra(nombre(), precioBase(), nivelTecnico);
            //throw new UnsupportedOperationException("TODO E05");
        }

        @Override
        public String norma() {
            return "NOM-TEC-" + nivelTecnico;
        }

        @Override
        public String toString() {
            return super.toString() + " {nivel técnico=" + nivelTecnico + "}";
        }
    }

    /**
     * Diagnóstico: revisiones subsecuentes al 50 %.
     * <p>Subtipo de Concepto declarado como clase anidada estática final; implementa ConGarantia, Certificable.</p>
     */
    public static final class Diagnostico extends Concepto implements ConGarantia, Certificable {
        @Serial
        private static final long serialVersionUID = 1L;

        public static final String PREFIJO = "DIA";

        private final String equipo;

        public Diagnostico(String nombre, double precioBase, String equipo) {
            super(PREFIJO, nombre, precioBase);
            this.equipo = validar(equipo);
        }

        public Diagnostico(Folio clave, String nombre, double precioBase, String equipo) {
            super(clave, nombre, precioBase);
            this.equipo = validar(equipo);
        }

        private static String validar(String equipo) {
            // TODO E05: Validar: equipo != null && !equipo.isBlank() (si no se cumple: ReglaNegocioException "El equipo de diagnóstico es obligatorio").
            // Validar que el equipo sea obligatorio
            if (equipo == null || equipo.isBlank()){
                throw new ReglaNegocioException("El equipo de diagnóstico es obligatorio");
            }
            //throw new UnsupportedOperationException("TODO E05");
            //devolver el equipo si es valido
            return equipo;
        }

        public String equipo() {
            return equipo;
        }

        @Override
        protected double calcularTarifa(int unidades) {
            // TODO E05: Diagnóstico: revisiones subsecuentes al 50 %. Fórmula: unidades == 1 ? precioBase() : precioBase() + (unidades - 1) * precioBase() * 0.5
            return unidades == 1 ? precioBase() : precioBase() + (unidades - 1) * precioBase() * 0.5;
            //throw new UnsupportedOperationException("TODO E05");
        }

        /** Retorno covariante: devuelve Diagnostico, no Concepto. */
        @Override
        public Diagnostico copiar() {
            // TODO E05: Crear un nuevo Diagnostico con el mismo nombre, precio y atributo (clave nueva).
            return new Diagnostico(nombre(), precioBase(), equipo());
            //throw new UnsupportedOperationException("TODO E05");
        }

        @Override
        public int mesesGarantia() {
            return 1;
        }

        @Override
        public String norma() {
            return "Protocolo " + equipo;
        }

        /** Resolución explícita de la colisión entre métodos por defecto. */
        @Override
        public String descripcionCorta() {
            // TODO E05: Combinar ConGarantia.super.descripcionCorta() y Certificable.super.descripcionCorta() separados por " | ".
            return ConGarantia.super.descripcionCorta() + " | "
            //Ejecuta la implementacion de default de certificable
            + Certificable.super.descripcionCorta();
            //throw new UnsupportedOperationException("TODO E05");
        }

        @Override
        public String toString() {
            return super.toString() + " {equipo=" + equipo + "}";
        }
    }
}
