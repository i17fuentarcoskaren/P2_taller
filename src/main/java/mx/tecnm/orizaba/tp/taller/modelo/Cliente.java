package mx.tecnm.orizaba.tp.taller.modelo;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Cliente del sistema. Implementa {@link Externalizable}: el propio objeto decide qué y cómo se
 * escribe (véase {@code java_4_modelo/t7_persistencia/b_externalizacion/Cosas2}); por ello requiere un
 * constructor público sin argumentos.
 */
public class Cliente implements Externalizable {

    /** Patrón de expresión regular para la validación del formato de correo electrónico. */

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    /** Identificador único del cliente. */
    private int id;
    /** Nombre o razón social del cliente. */
    private String nombre;
    /** Correo electrónico de contacto del cliente. */
    private String correo;
    /** Nivel de membresía o categoría del cliente. */
    private Nivel nivel;

    /** Requerido por la externalización: se invoca antes de readExternal. */
    public Cliente() {
    }

    /**
     * Construye un nuevo cliente con todos sus atributos especificados.
     *
     * @param id     Identificador único del cliente. Debe ser mayor a 0.
     * @param nombre Nombre o razón social del cliente. No puede ser nulo ni en blanco.
     * @param correo Correo electrónico de contacto. Debe cumplir con el patrón válido.
     * @param nivel  Nivel de servicio asignado. No debe ser nulo.
     * @throws ReglaNegocioException si el ID es menor o igual a 0, o si los datos no cumplen con las reglas de validación.
     */

    public Cliente(int id, String nombre, String correo, Nivel nivel) {
        // TODO E06: Validar id > 0 y asignar nombre, correo y nivel mediante sus setters.
        if (id <=0){
            throw new ReglaNegocioException("El ID debe de ser mayor a 0");
        }
        this.id = id;
        setNombre(nombre);
        setCorreo(correo);
        setNivel(nivel);
        //throw new UnsupportedOperationException("TODO E06");
    }

    /** Encadenamiento de constructores: nivel por omisión. */
    public Cliente(int id, String nombre, String correo) {
        this(id, nombre, correo, Nivel.values()[0]);
    }

    /**
     * Obtiene el identificador único del cliente.
     *
     * @return El ID numérico del cliente.
     */

    public int id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public String correo() {
        return correo;
    }

    public Nivel nivel() {
        return nivel;
    }

    public final void setNombre(String nombre) {
        // TODO E06: Rechazar nombre nulo o en blanco.
        if (nombre == null || nombre.isBlank()){
            throw new ReglaNegocioException("El nombre no puede estar vacío");
        }
        this.nombre = nombre.strip();
        //throw new UnsupportedOperationException("TODO E06");
    }

    public final void setCorreo(String correo) {
        // TODO E06: Validar el correo con el patrón CORREO y guardarlo en minúsculas.
        if (correo == null || !CORREO.matcher(correo).matches()){
            throw new ReglaNegocioException("El correo electrónico no es valido");
        }
        this.correo = correo.toLowerCase();
        //throw new UnsupportedOperationException("TODO E06");
    }

    public final void setNivel(Nivel nivel) {
        this.nivel = Objects.requireNonNull(nivel, "nivel");
    }

    @Override
    public void writeExternal(ObjectOutput salida) throws IOException {
        // TODO E14: Escribir id, nombre, correo y el nombre del nivel (writeInt / writeUTF).
        //escribir id
        salida.writeInt(id);
        //escribir nombre
        salida.writeUTF(nombre);
        //escribir correo
        salida.writeUTF(correo);
        //escribir el nombre de la constante
        salida.writeUTF(nivel.name());
        //throw new UnsupportedOperationException("TODO E14");
    }

    @Override
    public void readExternal(ObjectInput entrada) throws IOException {
        // TODO E14: Leer en el mismo orden en que se escribió y reconstruir el nivel con Nivel.valueOf.
        //leer el identificador num
        this.id = entrada.readInt();

        //leer el nombre
        this.nombre = entrada.readUTF();

        //leer el correo electronico
        this.correo = entrada.readUTF();

        //leer el nombre del nivel y reconvertirlo a la constante del enum usando valueOf
        this.nivel = Nivel.valueOf(entrada.readUTF());
        //throw new UnsupportedOperationException("TODO E14");
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Cliente otro && id == otro.id);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("%s #%d %s <%s> (%s)", "Cliente", id, nombre, correo, nivel);
    }
}
