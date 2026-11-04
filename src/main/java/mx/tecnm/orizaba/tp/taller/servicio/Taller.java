package mx.tecnm.orizaba.tp.taller.servicio;

import mx.tecnm.orizaba.tp.taller.concurrencia.TareaPeriodica;
import mx.tecnm.orizaba.tp.taller.eventos.Evento;
import mx.tecnm.orizaba.tp.taller.eventos.OyenteEvento;
import mx.tecnm.orizaba.tp.taller.eventos.TipoEvento;
import mx.tecnm.orizaba.tp.taller.funcional.FabricaElemento;
import mx.tecnm.orizaba.tp.taller.funcional.Regla;
import mx.tecnm.orizaba.tp.taller.modelo.Cliente;
import mx.tecnm.orizaba.tp.taller.modelo.Concepto;
import mx.tecnm.orizaba.tp.taller.modelo.CatalogoConceptos;
import mx.tecnm.orizaba.tp.taller.modelo.Folio;
import mx.tecnm.orizaba.tp.taller.modelo.Estado;
import mx.tecnm.orizaba.tp.taller.modelo.Nivel;
import mx.tecnm.orizaba.tp.taller.modelo.ReglaNegocioException;
import mx.tecnm.orizaba.tp.taller.modelo.OrdenServicio;
import mx.tecnm.orizaba.tp.taller.persistencia.ConexionSQLite;
import mx.tecnm.orizaba.tp.taller.persistencia.Repositorio;
import mx.tecnm.orizaba.tp.taller.persistencia.RepositorioClienteSQLite;
import mx.tecnm.orizaba.tp.taller.persistencia.RepositorioConceptoSQLite;
import mx.tecnm.orizaba.tp.taller.persistencia.RepositorioOrdenServicioSQLite;
import mx.tecnm.orizaba.tp.taller.persistencia.Respaldo;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Fachada del dominio (capa de servicio). <strong>La UI sólo debe depender de esta clase</strong>
 * y de los tipos del paquete {@code modelo}/{@code eventos}; nunca de JDBC.
 */
public class Taller implements AutoCloseable {

    /** Días tras los cuales una orden en curso genera una alerta. */
    public static final int PLAZO_DIAS = 5;

    /** Catálogo local de conceptos disponibles en el taller. */
    private final CatalogoConceptos catalogo = new CatalogoConceptos();
    /** Mapa concurrente que almacena los clientes registrados en memoria. */
    private final Map<Integer, Cliente> clientes = new ConcurrentHashMap<>();
    /** Mapa concurrente que almacena las órdenes de servicio registradas en memoria. */
    private final Map<Integer, OrdenServicio> registros = new ConcurrentHashMap<>();
    /** Conjunto concurrente que lleva el seguimiento de los identificadores de órdenes que ya han generado una alerta. */
    private final Set<Integer> alertados = ConcurrentHashMap.newKeySet();
    /** Lista segura para hilos que contiene a los oyentes suscritos al sistema de eventos. */
    private final List<OyenteEvento> oyentes = new CopyOnWriteArrayList<>();
    /** Repositorio de conceptos persistidos en SQLite. */
    private final Repositorio<Concepto, Folio> repoElementos;
    /** Repositorio de clientes persistidos en SQLite. */
    private final Repositorio<Cliente, Integer> repoClientes;
    /** Repositorio de órdenes de servicio persistidas en SQLite. */
    private final Repositorio<OrdenServicio, Integer> repoRegistros;
    /** Proveedor de la fecha actual (inyectable para pruebas y simulación). *//** Proveedor de la fecha actual (inyectable para pruebas y simulación). */
    private final Supplier<LocalDate> reloj;
    private MonitorAvance monitor;

    public Taller(ConexionSQLite bd) {
        this(bd, LocalDate::now);
    }

    /** @param reloj proveedor de la fecha actual (inyectable para pruebas y simulación) */
    public Taller(ConexionSQLite bd, Supplier<LocalDate> reloj) {
        this.repoElementos = new RepositorioConceptoSQLite(bd);
        this.repoClientes = new RepositorioClienteSQLite(bd);
        this.repoRegistros = new RepositorioOrdenServicioSQLite(bd, repoClientes, repoElementos);
        this.reloj = reloj;
        cargar();
    }
    /** Carga inicial de datos desde los repositorios de persistencia hacia las estructuras en memoria. */
    private void cargar() {
        repoElementos.todos().forEach(catalogo::agregar);
        repoClientes.todos().forEach(cliente -> clientes.put(cliente.id(), cliente));
        repoRegistros.todos().forEach(orden -> registros.put(orden.id(), orden));
    }

    // ------------------------------------------------------------------ eventos
    /**
     * Suscribe un nuevo oyente para recibir notificaciones de eventos del sistema.
     *
     * @param oyente Instancia de {@link OyenteEvento}.
     */
    public void suscribir(OyenteEvento oyente) {
        oyentes.add(oyente);
    }

    public void desuscribir(OyenteEvento oyente) {
        oyentes.remove(oyente);
    }

    private void publicar(TipoEvento tipo, String mensaje) {
        Evento e = Evento.de(tipo, mensaje);
        oyentes.forEach(o -> o.notificar(e));
    }

    // ------------------------------------------------------------------ catálogo

    /**
     * Registra un nuevo concepto en el catálogo y lo persiste en la base de datos.
     *
     * @param fabrica  Fábrica funcional para instanciar el concepto.
     * @param nombre   Nombre descriptivo del concepto.
     * @param precio   Precio base.
     * @param atributo Atributo específico del subtipo.
     * @param <E>      Tipo de elemento (subclase de {@link Concepto}).
     * @param <A>      Tipo del atributo.
     * @return El concepto creado y registrado.
     */
    public <E extends Concepto, A> E registrar(FabricaElemento<E, A> fabrica, String nombre, double precio, A atributo) {
        E elemento = fabrica.crear(nombre, precio, atributo);
        repoElementos.guardar(elemento);
        catalogo.agregar(elemento);
        publicar(TipoEvento.ALTA, "Concepto registrado: " + elemento.clave());
        return elemento;
    }

    public void actualizar(Concepto elemento, String nombre, double precio) {
        elemento.setNombre(nombre);
        elemento.setPrecioBase(precio);
        repoElementos.guardar(elemento);
    }

    public boolean retirar(Folio clave) {
        boolean referenciado = registros.values().stream()
                .anyMatch(orden -> orden.lineas().stream()
                        .anyMatch(lineas -> lineas.elemento().clave().equals(clave)));

        if (referenciado) {
            throw new ReglaNegocioException("No se puede retirar un concepto asociado a una orden");
        }
        boolean eliminado = repoElementos.eliminar(clave);

        if (eliminado) {
            catalogo.retirar(clave);
        }
        return eliminado;
    }

    public CatalogoConceptos catalogo() {
        return catalogo;
    }

    public Concepto elemento(String clave) {
        return catalogo.buscar(Folio.parse(clave))
                .orElseThrow(() -> new ReglaNegocioException("No existe " + clave));
    }

    // ------------------------------------------------------------------ clientes

    public Cliente registrarCliente(String nombre, String correo, Nivel nivel) {
        boolean repetido = clientes.values().stream()
                .anyMatch(cliente -> cliente.correo().equalsIgnoreCase(correo));
        if (repetido) {
            throw new ReglaNegocioException("El correo ya está registrado");
        }
        int id = clientes.values().stream().mapToInt(Cliente::id).max().orElse(0) + 1;
        Cliente nuevo = new Cliente(id, nombre, correo, nivel);
        repoClientes.guardar(nuevo);
        clientes.put(nuevo.id(), nuevo);
        publicar(TipoEvento.ALTA, "Cliente registrado: " + nuevo.id());
        return nuevo;
    }

    public void cambiarNivel(int idCliente, Nivel nivel) {
        Cliente a = cliente(idCliente);
        a.setNivel(nivel);
        repoClientes.guardar(a);
    }

    public Cliente cliente(int id) {
        return Optional.ofNullable(clientes.get(id))
                .orElseThrow(() -> new ReglaNegocioException("No existe el registro " + id));
    }

    public List<Cliente> clientes() {
        return clientes.values().stream().sorted(Comparator.comparingInt(Cliente::id)).toList();
    }

    // ------------------------------------------------------------------ órdenes de servicio
    /**
     * Actualiza los datos de un cliente registrado.
     *
     * @param id identificador del cliente
     * @param nombre nuevo nombre
     * @param correo nuevo correo electrónico
     * @param nivel nuevo nivel del cliente
     */
    public void actualizarCliente(int id, String nombre,
                                  String correo, Nivel nivel) {

        Cliente actual = cliente(id);

        String limpio = correo == null ? null : correo.strip();

        boolean repetido = clientes.values().stream()
                .anyMatch(c -> c.id() != id
                        && c.correo().equalsIgnoreCase(limpio));

        if (repetido) {
            throw new ReglaNegocioException(
                    "El correo ya está registrado por otro cliente"
            );
        }

        Cliente editado = new Cliente(id, nombre, limpio, nivel);

        // Guardar primero en SQLite
        repoClientes.guardar(editado);

        // Actualizar el objeto existente en memoria
        actual.setNombre(editado.nombre());
        actual.setCorreo(editado.correo());
        actual.setNivel(editado.nivel());
    }

    /**
     * Elimina un cliente si no tiene órdenes asociadas.
     *
     * @param id identificador del cliente
     * @return true si fue eliminado correctamente
     */
    public boolean eliminarCliente(int id) {

        // Verificar que el cliente exista
        cliente(id);

        // Comprobar si tiene órdenes relacionadas
        boolean tieneOrdenes = registros.values().stream()
                .anyMatch(o -> o.cliente().id() == id);

        if (tieneOrdenes) {
            throw new ReglaNegocioException(
                    "No se puede eliminar un cliente que tiene órdenes de servicio"
            );
        }

        // Eliminar de SQLite
        boolean eliminado = repoClientes.eliminar(id);

        // Eliminar de memoria
        if (eliminado) {
            clientes.remove(id);
        }

        return eliminado;
    }
    public OrdenServicio abrir(int idCliente) {
        Cliente cliente = cliente(idCliente);
        int id = registros.values().stream().mapToInt(OrdenServicio::id).max().orElse(0) + 1;
        OrdenServicio nueva = new OrdenServicio(id, cliente, reloj.get());
        repoRegistros.guardar(nueva);
        registros.put(nueva.id(), nueva);
        publicar(TipoEvento.CAMBIO_ESTADO, "Orden #" + nueva.id() + " abierta");
        return nueva;
    }

    public OrdenServicio.Linea agregarLinea(int idOrdenServicio, String clave, int unidades) {
        OrdenServicio orden = registro(idOrdenServicio);
        Concepto concepto = elemento(clave);
        OrdenServicio.Linea linea = orden.agregar(concepto, unidades);
        repoRegistros.guardar(orden);
        return linea;
    }

    public boolean quitarLinea(int idOrdenServicio, String clave) {
        OrdenServicio t = registro(idOrdenServicio);
        boolean ok = t.quitar(Folio.parse(clave));
        repoRegistros.guardar(t);
        return ok;
    }
    /**
     * Pone en curso (inicia) una orden de servicio.
     *
     * @param idOrdenServicio Identificador de la orden.
     */
    public void iniciar(int idOrdenServicio) {
        transitar(idOrdenServicio, OrdenServicio::iniciar);
    }

    /**
     * Cierra una orden de servicio.
     *
     * @param idOrdenServicio Identificador de la orden.
     */

    public void cerrar(int idOrdenServicio) {
        transitar(idOrdenServicio, OrdenServicio::cerrar);
    }

    /**
     * Cancela una orden de servicio.
     *
     * @param idOrdenServicio Identificador de la orden.
     */
    public void cancelar(int idOrdenServicio) {
        transitar(idOrdenServicio, OrdenServicio::cancelar);
    }

    /** Método auxiliar que aplica una transición de estado a una orden y persiste el cambio. */
    private void transitar(int id, Consumer<OrdenServicio> transicion) {
        OrdenServicio orden = registro(id);
        transicion.accept(orden);
        repoRegistros.guardar(orden);
        publicar(TipoEvento.CAMBIO_ESTADO, "Orden #" + orden.id() + " cambió a " + orden.estado());
    }

    public OrdenServicio registro(int id) {
        return Optional.ofNullable(registros.get(id))
                .orElseThrow(() -> new ReglaNegocioException("No existe el registro " + id));
    }

    public List<OrdenServicio> registros() {
        return registros.values().stream().sorted(Comparator.comparingInt(OrdenServicio::id)).toList();
    }

    public List<OrdenServicio> registrosEn(Estado estado) {
        return registros().stream().filter(t -> t.estado() == estado).toList();
    }

    // ------------------------------------------------------------------ reportes (streams)

    /** Flujo auxiliar que emite únicamente las órdenes de servicio cerradas. */
    private Stream<OrdenServicio> cerrados() {
        return registros.values().stream().filter(t -> t.estado() == Estado.CERRADA);
    }

    /**
     * Calcula los ingresos totales agrupados por el tipo de concepto, basándose en las órdenes cerradas.
     *
     * @return Mapa ordenado con los ingresos por cada tipo de concepto.
     */
    public Map<String, Double> ingresosPorTipo() {
        return cerrados().flatMap(v -> v.lineas().stream()).collect(Collectors.groupingBy(
                linea -> linea.elemento().tipo(),
                TreeMap::new,
                Collectors.summingDouble(OrdenServicio.Linea::importe)
        ));
    }

    public Optional<Concepto> masDemandado() {
        Map<Concepto, Integer> unidadesPorConcepto = cerrados().flatMap(v -> v.lineas().stream()).collect(Collectors.groupingBy(
                OrdenServicio.Linea::elemento,
                Collectors.summingInt(OrdenServicio.Linea::unidades)
        ));
        return unidadesPorConcepto.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    /**
     * Obtiene los mejores {@code n} clientes ordenados de mayor a menor según el importe total de sus órdenes cerradas.
     *
     * @param n Número máximo de clientes a retornar.
     * @return Lista de los mejores clientes.
     */

    public List<Cliente> mejores(int n) {
        Map<Cliente, Double> totales = cerrados().collect(Collectors.groupingBy(
                OrdenServicio::cliente,
                Collectors.summingDouble(OrdenServicio::total)
        ));
        return totales.entrySet().stream()
                .sorted(Map.Entry.<Cliente, Double>comparingByValue().reversed())
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();
    }

    public DoubleSummaryStatistics estadisticas() {
        return cerrados().mapToDouble(OrdenServicio::total).summaryStatistics();
    }

    public Map<Boolean, List<Concepto>> particionarPorPrecio(double umbral) {
        return catalogo.ordenados(Comparator.comparing(Concepto::clave)).stream()
                .collect(Collectors.partitioningBy(e -> e.precioBase() >= umbral));
    }

    public Map<Estado, Long> conteoPorEstado() {
        return registros.values().stream()
                .collect(Collectors.groupingBy(OrdenServicio::estado, () -> new TreeMap<>(), Collectors.counting()));
    }

    public List<Concepto> buscar(Regla<? super Concepto> regla) {
        return catalogo.filtrar(regla);
    }

    /**
     * Proyecta los conceptos del catálogo aplicando una función de transformación.
     *
     * @param funcion Función de mapeo o proyección.
     * @param <R>     Tipo de dato resultante de la proyección.
     * @return Lista con los elementos proyectados.
     */

    public <R> List<R> proyectar(Function<? super Concepto, ? extends R> funcion) {
        return catalogo.ordenados(Comparator.comparing(Concepto::clave)).stream()
                .<R>map(funcion)
                .toList();
    }

    /**
     * Genera una descripción detallada y legible de un concepto según su subtipo y características.
     *
     * @param e Instancia de {@link Concepto}.
     * @return Cadena descriptiva.
     */

    public String describir(Concepto e) {
        return switch (e) {
            case Concepto.Refaccion r -> String.format("Refaccion %s de marca %s", r.nombre(), r.marca());
            case Concepto.ManoDeObra m when m.nivelTecnico() == 3 -> String.format("Mano de obra experta (Nivel 3): %s", m.nombre());
            case Concepto.ManoDeObra m -> String.format("Mano de obra (Nivel %d): %s", m.nivelTecnico(), m.nombre());
            case Concepto.Diagnostico d -> String.format("Diagnostico para %s: %s", d.equipo(), d.nombre());
        };
    }

    // ------------------------------------------------------------------ monitor

    public synchronized int revisar() {
        int acciones = 0;
        LocalDate hoy = reloj.get();
        for (OrdenServicio orden : registros.values()) {
            long dias = orden.diasDesde(hoy);
            switch (orden.estado()) {
                case ABIERTA -> {
                    if (orden.lineas().isEmpty() && dias >= 1) {
                        orden.cancelar();
                        repoRegistros.guardar(orden);
                        publicar(
                                TipoEvento.CANCELACION_AUTOMATICA,
                                "Orden #" + orden.id() + " cancelada"
                        );
                        acciones++;
                    }
                }
                case EN_CURSO -> {
                    if (dias > PLAZO_DIAS && alertados.add(orden.id())) {
                        publicar(TipoEvento.ALERTA, "Orden #" + orden.id() + " vencida");
                        acciones++;
                    }
                }
                case CERRADA, CANCELADA -> {}
            }
        }
        return acciones;
        //throw new UnsupportedOperationException("TODO E12")
    }
    /**
     * Inicia el hilo monitor periódico para la revisión automática de avance de las órdenes.
     *
     * @param periodoMs Intervalo de tiempo en milisegundos entre cada ciclo de revisión.
     * @throws IllegalStateException si el monitor ya se encuentra activo.
     */
    public synchronized void iniciarMonitor(long periodoMs) {
        if (monitor != null && monitor.activa()) {
            throw new IllegalStateException("El monitor ya está activo");
        }

        monitor = new MonitorAvance(periodoMs);
        monitor.iniciar("Hilo-Monitor-Taller");
    }

    public void detenerMonitor() throws InterruptedException {
        MonitorAvance m;
        synchronized (this) {
            m = monitor;
        }
        if (m != null) {
            m.detener();
        }
    }

    public synchronized boolean monitorActivo() {
        return monitor != null && monitor.activa();
    }

    public final class MonitorAvance extends TareaPeriodica {

        private MonitorAvance(long periodoMs) {
            super(periodoMs);
        }

        @Override
        protected void ejecutarCiclo() {
            Taller.this.revisar();
        }
    }

    // ------------------------------------------------------------------ respaldo

    /**
     * Exporta un respaldo binario completo del taller (catálogo, clientes y registros de órdenes) hacia un archivo.
     *
     * @param archivo Ruta del archivo de destino del respaldo.
     * @throws IOException si ocurre un error de E/S durante la exportación.
     */
    public void exportarRespaldo(Path archivo) throws IOException {
        Respaldo.exportar(archivo, catalogo, clientes(), registros());
    }

    /** Cierra los recursos del taller, deteniendo de manera segura el hilo monitor activo. */
    @Override
    public void close() {
        try {
            detenerMonitor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}