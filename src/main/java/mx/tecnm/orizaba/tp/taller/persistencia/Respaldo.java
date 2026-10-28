package mx.tecnm.orizaba.tp.taller.persistencia;

import mx.tecnm.orizaba.tp.taller.modelo.Cliente;
import mx.tecnm.orizaba.tp.taller.modelo.CatalogoConceptos;
import mx.tecnm.orizaba.tp.taller.modelo.OrdenServicio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Respaldo binario mediante serialización de objetos (complementa a SQLite).
 * Escribe y lee en el mismo orden (véase {@code java_4_modelo/t7_persistencia/a_serializacion/Gusano_07}).
 */
public final class Respaldo {

    public static final String ENCABEZADO = "Respaldo Taller v1";

    private Respaldo() {
    }

    /** Contenido recuperado de un respaldo. */
    public record Contenido(String encabezado, LocalDateTime fecha, CatalogoConceptos catalogo,
                            List<Cliente> clientes, List<OrdenServicio> registros) {
    }

    public static void exportar(Path archivo, CatalogoConceptos catalogo, Collection<Cliente> clientes,
                                Collection<OrdenServicio> registros) throws IOException {
        // TODO E14: ObjectOutputStream(BufferedOutputStream(Files.newOutputStream)): encabezado, fecha, catálogo, listas.
        // Abrir el flujo de salida con búfer y serialización de objetos (try-with-resources para cerrar automáticamente)
        try (ObjectOutputStream salida = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(archivo)))){

            // Guardar el encabezado como cadena UTF
            salida.writeUTF(ENCABEZADO);
            //Guardar la fecha y hora actual en la que se genera el respaldo
            salida.writeObject(LocalDateTime.now());
            //Guardar el catálogo de conceptos
            salida.writeObject(catalogo);
            //Guardar la lista de clientes convertida a ArrayList
            salida.writeObject(new ArrayList<>(clientes));
            //Guardar la lista de órdenes de servicio (registros) convertida a ArrayList
            salida.writeObject(new ArrayList<>(registros));

        }
        //throw new UnsupportedOperationException("TODO E14");
    }

    @SuppressWarnings("unchecked")
    public static Contenido importar(Path archivo) throws IOException, ClassNotFoundException {
        // TODO E14: Leer en el mismo orden; validar el encabezado (IOException si no coincide).
        //Flujo de entrada para leer los objetos secuencialmente con búfer
        try (ObjectInputStream entrada = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))){
            //Leer el encabezado en formato UTF
            String encabezado = entrada.readUTF();
            //Validar que el encabezado coincida exactamente con la versión esperada
            if (!ENCABEZADO.equals(encabezado)) {
                throw new IOException("El archivo de respaldo no es válido o el encabezado no coincide: " + encabezado);
            }
            // Recuperar la fecha del respaldo
            LocalDateTime fecha = (LocalDateTime) entrada.readObject();
            //Recuperar el catálogo de conceptos
            CatalogoConceptos catalogo = (CatalogoConceptos) entrada.readObject();
            //Recuperar la lista de clientes
            List<Cliente> clientes = (List<Cliente>) entrada.readObject();
            //Recuperar la lista de órdenes de servicio (registros)
            List<OrdenServicio> registros = (List<OrdenServicio>) entrada.readObject();
            // Construir y retornar el objeto Contenido con la información restaurada
            return new Contenido(encabezado, fecha, catalogo, clientes, registros);
        }
        //throw new UnsupportedOperationException("TODO E14");
    }
}
