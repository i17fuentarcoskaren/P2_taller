package mx.tecnm.orizaba.tp.taller.app;

import mx.tecnm.orizaba.tp.taller.eventos.Evento;
import mx.tecnm.orizaba.tp.taller.eventos.TipoEvento;
import mx.tecnm.orizaba.tp.taller.funcional.FabricaElemento;
import mx.tecnm.orizaba.tp.taller.funcional.Regla;
import mx.tecnm.orizaba.tp.taller.funcional.Reglas;
import mx.tecnm.orizaba.tp.taller.modelo.*;
import mx.tecnm.orizaba.tp.taller.persistencia.ConexionSQLite;
import mx.tecnm.orizaba.tp.taller.persistencia.Respaldo;
import mx.tecnm.orizaba.tp.taller.servicio.Taller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Pruebas de aceptación del modelo (sin dependencias externas). Deben pasar al 100 %
 * antes de comenzar la UI. Ejecutar: {@code java ... mx.tecnm.orizaba.tp.taller.app.PruebasModelo}
 */
public final class PruebasModelo {

    private static int aprobadas;
    private static final List<String> fallidas = new ArrayList<>();

    @FunctionalInterface
    private interface Caso {
        void ejecutar() throws Exception;
    }

    private static void prueba(String nombre, Caso caso) {
        try {
            caso.ejecutar();
            aprobadas++;
            System.out.println("  [OK]    " + nombre);
        } catch (Throwable t) {
            fallidas.add(nombre);
            System.out.println("  [FALLA] " + nombre + " -> " + t);
        }
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private static void cerca(double esperado, double real) {
        verificar(Math.abs(esperado - real) < 0.01, "se esperaba " + esperado + " y se obtuvo " + real);
    }

    private static void lanza(Class<? extends Throwable> tipo, Caso caso) {
        try {
            caso.ejecutar();
        } catch (Throwable t) {
            verificar(tipo.isInstance(t), "se esperaba " + tipo.getSimpleName() + " y se obtuvo " + t);
            return;
        }
        throw new AssertionError("se esperaba " + tipo.getSimpleName());
    }

    public static void main(String[] args) throws Exception {
        Path tmp = Files.createTempDirectory("pruebas-taller");
        System.out.println("Pruebas del modelo – Taller");

        prueba("T01 Folio: formato, parse y validación", () -> {
            Folio c = Folio.parse("ABC-0042");
            verificar(c.toString().equals("ABC-0042"), "toString");
            verificar(c.compareTo(new Folio("ABC", 43)) < 0, "orden");
            lanza(IllegalArgumentException.class, () -> new Folio("ab", 1));
            lanza(IllegalArgumentException.class, () -> Folio.parse("ABC_1"));
        });

        prueba("T02 Invariantes de Concepto (encapsulamiento)", () -> {
            lanza(ReglaNegocioException.class, () -> new Concepto.Refaccion("x", -1, "Bosch"));
            lanza(ReglaNegocioException.class, () -> new Concepto.Refaccion(" ", 10, "Bosch"));
            lanza(ReglaNegocioException.class, () -> new Concepto.ManoDeObra("x", 10, 4));
            lanza(ReglaNegocioException.class, () -> new Concepto.Refaccion("x", 10, "Bosch").tarifa(0));
        });

        prueba("T03 Polimorfismo: tarifas por subtipo (Template Method)", () -> {
            Cotizable[] v = {
                new Concepto.Refaccion("a", 450.0, "Bosch"),
                new Concepto.ManoDeObra("b", 300.0, 2),
                new Concepto.Diagnostico("c", 500.0, "Escáner OBD-II") };
            cerca(900.0, v[0].tarifa(2));
            cerca(1080.0, v[1].tarifa(3));
            cerca(1000.0, v[2].tarifa(3));
            cerca(Math.round(900.0 * 1.16 * 100.0) / 100.0, v[0].tarifaConImpuesto(2));
        });

        prueba("T04 Clasificación múltiple y colisión de métodos default", () -> {
            Object s1 = new Concepto.Refaccion("a", 1, "Bosch");
            Object s2 = new Concepto.ManoDeObra("b", 1, 2);
            Concepto.Diagnostico s3 = new Concepto.Diagnostico("c", 1, "Escáner OBD-II");
            verificar(s1 instanceof ConGarantia && !(s1 instanceof Certificable), "Refaccion sólo ConGarantia");
            verificar(s2 instanceof Certificable && !(s2 instanceof ConGarantia), "ManoDeObra sólo Certificable");
            verificar(s3 instanceof ConGarantia && s3 instanceof Certificable, "Diagnostico ambas");
            String d = s3.descripcionCorta();
            verificar(d.contains("Garantía (meses)") && d.contains("Norma"), "colisión resuelta: " + d);
        });

        prueba("T05 Retorno covariante en copiar()", () -> {
            Concepto.Refaccion a = new Concepto.Refaccion("a", 5, "Bosch");
            Concepto.Refaccion b = a.copiar();   // sin conversión: retorno covariante
            verificar(!a.equals(b) && a.nombre().equals(b.nombre()), "copia con clave nueva");
        });

        prueba("T06 Máquina de estados de OrdenServicio", () -> {
            Cliente x = new Cliente(900, "Prueba", "prueba@ito.mx");
            OrdenServicio t = new OrdenServicio(900, x, LocalDate.now());
            lanza(ReglaNegocioException.class, t::iniciar);
            t.agregar(new Concepto.Refaccion("a", 10, "Bosch"), 1);
            t.iniciar();
            lanza(ReglaNegocioException.class, () -> t.agregar(new Concepto.Refaccion("b", 1, "Bosch"), 1));
            t.cerrar();
            lanza(ReglaNegocioException.class, t::cancelar);
            verificar(t.estado() == Estado.CERRADA, "estado final");
        });

        prueba("T07 Composición: la línea usa el nivel de la envolvente", () -> {
            Nivel n = Nivel.values()[Nivel.values().length - 1];
            Cliente x = new Cliente(901, "Prueba", "p2@ito.mx", n);
            OrdenServicio t = new OrdenServicio(901, x, LocalDate.now());
            OrdenServicio.Linea l = t.agregar(new Concepto.Refaccion("a", 450.0, "Bosch"), 2);
            cerca(n.aplicar(900.0), l.importe());
            verificar(l.contenedor() == t, "OrdenServicio.this");
            lanza(ReglaNegocioException.class, () -> t.agregar(l.elemento(), 2));
        });

        prueba("T08 Agregación, iterador oculto, clase anónima y clase local", () -> {
            Concepto a = new Concepto.Refaccion("Alfa", 300, "Bosch");
            Concepto b = new Concepto.ManoDeObra("Beta", 100, 2);
            CatalogoConceptos cat = new CatalogoConceptos(a, b);
            lanza(ReglaNegocioException.class, () -> cat.agregar(a));
            Iterator<Concepto> it = cat.iterator();
            verificar(!it.getClass().getSimpleName().isEmpty() && it.hasNext(), "iterador");
            lanza(UnsupportedOperationException.class, it::remove);
            verificar(cat.ordenados(CatalogoConceptos.porTarifa(1)).get(0) == b, "comparador anónimo");
            verificar(cat.resumen().startsWith("2 "), "clase local: " + cat.resumen());
            verificar(cat.deTipo(Concepto.Refaccion.class).size() == 1, "genéricos acotados");
        });

        prueba("T09 Interfaz funcional propia y composición de reglas", () -> {
            Concepto a = new Concepto.Refaccion("Alfa", 300, "Bosch");
            Concepto b = new Concepto.Diagnostico("Gamma", 100, "Escáner OBD-II");
            CatalogoConceptos cat = new CatalogoConceptos(a, b);
            Regla<Concepto> r = Reglas.precioHasta(200).y(Reglas.conCapacidad(Certificable.class));
            verificar(cat.filtrar(r).equals(List.of(b)), "y");
            verificar(cat.filtrar(r.negar()).equals(List.of(a)), "negar");
            verificar(cat.filtrar(Reglas.nombreContiene("ALF").o(r)).size() == 2, "o");
            FabricaElemento<Concepto.Refaccion, String> f = Concepto.Refaccion::new;
            verificar(f.crear("Delta", 1, "Bosch").tipo().equals("Refaccion"), "referencia a constructor");
        });

        prueba("T10 Serialización: transient y Externalizable", () -> {
            Concepto a = new Concepto.Refaccion("Alfa", 10, "Bosch");
            a.tarifa(1);
            Cliente x = new Cliente(7, "Ada", "ada@ito.mx", Nivel.values()[1]);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(bytes)) {
                oos.writeObject(a);
                oos.writeObject(x);
            }
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
                Concepto a2 = (Concepto) ois.readObject();
                Cliente x2 = (Cliente) ois.readObject();
                verificar(a2.equals(a) && a2.cotizaciones() == 0 && a.cotizaciones() == 1, "transient");
                verificar(x2.correo().equals("ada@ito.mx") && x2.nivel() == Nivel.values()[1], "externalizable");
            }
        });

        Path bd = tmp.resolve("pruebas.db");
        AtomicReference<LocalDate> hoy = new AtomicReference<>(LocalDate.now());
        int[] ids = new int[2];

        prueba("T11 Servicio + SQLite: alta, flujo y reportes", () -> {
            try (ConexionSQLite cx = new ConexionSQLite(bd.toString())) {
                Taller g = new Taller(cx, hoy::get);
                List<Evento> eventos = new CopyOnWriteArrayList<>();
                g.suscribir(eventos::add);
                Concepto e1 = g.registrar(Concepto.Refaccion::new, "Uno", 450.0, "Bosch");
                Concepto e3 = g.registrar(Concepto.Diagnostico::new, "Tres", 500.0, "Escáner OBD-II");
                Cliente a = g.registrarCliente("Grace Hopper", "grace@ito.mx", Nivel.values()[0]);
                lanza(ReglaNegocioException.class, () -> g.registrarCliente("Otra", "GRACE@ito.mx", Nivel.values()[0]));
                OrdenServicio t = g.abrir(a.id());
                g.agregarLinea(t.id(), e1.clave().toString(), 2);
                g.agregarLinea(t.id(), e3.clave().toString(), 3);
                g.iniciar(t.id());
                g.cerrar(t.id());
                ids[0] = t.id();
                cerca(900.0 + 1000.0, g.estadisticas().getSum());
                verificar(g.ingresosPorTipo().size() == 2, "ingresos por tipo");
                verificar(g.mejores(1).equals(List.of(a)), "mejores");
                verificar(g.masDemandado().isPresent(), "más demandado");
                verificar(g.describir(e1).contains("Refaccion"), "describir");
                verificar(g.proyectar(Concepto::nombre).containsAll(List.of("Uno", "Tres")), "proyectar");
                verificar(eventos.stream().anyMatch(ev -> ev.tipo() == TipoEvento.ALTA), "eventos");
                lanza(ReglaNegocioException.class, () -> g.retirar(e1.clave()));
                ids[1] = g.abrir(a.id()).id();
            }
        });

        prueba("T12 Persistencia: la información sobrevive al reinicio", () -> {
            try (ConexionSQLite cx = new ConexionSQLite(bd.toString())) {
                Taller g = new Taller(cx, hoy::get);
                verificar(g.catalogo().tamanio() == 2, "catálogo");
                verificar(g.clientes().size() == 1, "clientes");
                OrdenServicio t = g.registro(ids[0]);
                verificar(t.estado() == Estado.CERRADA && t.lineas().size() == 2, "registro reconstruido");
                cerca(900.0 + 1000.0, t.total());
            }
        });

        prueba("T13 Monitor: reglas, hilo y detención", () -> {
            try (ConexionSQLite cx = new ConexionSQLite(bd.toString())) {
                Taller g = new Taller(cx, hoy::get);
                List<Evento> eventos = new CopyOnWriteArrayList<>();
                g.suscribir(eventos::add);
                OrdenServicio t = g.abrir(g.clientes().get(0).id());
                g.agregarLinea(t.id(), g.catalogo().iterator().next().clave().toString(), 1);
                g.iniciar(t.id());
                hoy.set(hoy.get().plusDays(Taller.PLAZO_DIAS + 1));
                verificar(g.revisar() == 2, "cancelación + alerta");
                verificar(g.registro(ids[1]).estado() == Estado.CANCELADA, "abierta sin líneas cancelada");
                verificar(g.revisar() == 0, "la alerta no se repite");
                g.iniciarMonitor(20);
                Thread.sleep(120);
                verificar(g.monitorActivo(), "hilo activo");
                g.detenerMonitor();
                verificar(!g.monitorActivo(), "hilo detenido");
                long alertas = eventos.stream().filter(ev -> ev.tipo() == TipoEvento.ALERTA).count();
                verificar(alertas == 1, "alertas: " + alertas);
            }
        });

        prueba("T14 Respaldo binario (exportar/importar)", () -> {
            Path archivo = tmp.resolve("respaldo.bin");
            try (ConexionSQLite cx = new ConexionSQLite(bd.toString())) {
                Taller g = new Taller(cx, hoy::get);
                g.exportarRespaldo(archivo);
                Respaldo.Contenido c = Respaldo.importar(archivo);
                verificar(c.catalogo().tamanio() == g.catalogo().tamanio(), "catálogo");
                verificar(c.registros().size() == g.registros().size(), "registros");
                OrdenServicio.Linea l = c.registros().get(0).lineas().get(0);
                verificar(l.contenedor() == c.registros().get(0), "clase interna serializada con su envolvente");
            }
        });

        System.out.printf("%nResultado: %d aprobadas, %d fallidas%n", aprobadas, fallidas.size());
        if (!fallidas.isEmpty()) {
            System.exit(1);
        }
    }
}
