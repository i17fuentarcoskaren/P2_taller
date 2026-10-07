package mx.tecnm.orizaba.tp.taller.app;

import mx.tecnm.orizaba.tp.taller.eventos.OyenteEvento;
import mx.tecnm.orizaba.tp.taller.eventos.Evento;
import mx.tecnm.orizaba.tp.taller.funcional.Reglas;
import mx.tecnm.orizaba.tp.taller.modelo.*;
import mx.tecnm.orizaba.tp.taller.persistencia.ConexionSQLite;
import mx.tecnm.orizaba.tp.taller.servicio.Taller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Recorrido de consola por el modelo: sirve como referencia de uso para construir la UI.
 * La UI del estudiante debe ofrecer, como mínimo, lo que aquí se muestra.
 */
public final class DemoModelo {

    public static void main(String[] args) throws Exception {
        Path bd = args.length > 0 ? Path.of(args[0]) : Files.createTempFile("taller", ".db");
        AtomicReference<LocalDate> hoy = new AtomicReference<>(LocalDate.now());

        try (ConexionSQLite cx = new ConexionSQLite(bd.toString());
             Taller g = new Taller(cx, hoy::get)) {

            // Suscripción con clase anónima (la UI usaría Platform.runLater / invokeLater aquí).
            g.suscribir(new OyenteEvento() {
                @Override
                public void notificar(Evento e) {
                    System.out.println("   evento " + e + " [" + Thread.currentThread().getName() + "]");
                }
            });

            System.out.println("== Alta de conceptos (referencias a constructor)");
            g.registrar(Concepto.Refaccion::new, "Balatas delanteras", 450.0, "Bosch");
            g.registrar(Concepto.Refaccion::new, "Filtro de aceite", 180.0, "Fram");
            g.registrar(Concepto.ManoDeObra::new, "Afinación mayor", 300.0, 2);
            g.registrar(Concepto.ManoDeObra::new, "Ajuste de frenos", 250.0, 1);
            g.registrar(Concepto.Diagnostico::new, "Escaneo electrónico", 500.0, "Escáner OBD-II");
            g.registrar(Concepto.Diagnostico::new, "Revisión de suspensión", 350.0, "Rampa hidráulica");

            System.out.println("== clientes");
            g.registrarCliente("Transportes del Golfo", "contacto@tgolfo.mx", Nivel.OCASIONAL);
            g.registrarCliente("Diana Pérez", "diana.perez@gmail.com", Nivel.FRECUENTE);
            g.registrarCliente("Ernesto Gil", "egil@outlook.com", Nivel.FLOTILLA);

            System.out.println("== Flujo de orden de servicio");
            OrdenServicio t = g.abrir(1);
            g.agregarLinea(t.id(), "REF-0001", 2);
            g.agregarLinea(t.id(), "DIA-0001", 3);
            g.iniciar(t.id());
            t.forEach(System.out::println);
            g.cerrar(t.id());
            OrdenServicio t2 = g.abrir(2);
            g.agregarLinea(t2.id(), "MOB-0001", 1);
            g.iniciar(t2.id());
            g.abrir(3);

            System.out.println("== Reportes");
            System.out.println(g.catalogo().resumen());
            System.out.println("Ingresos por tipo: " + g.ingresosPorTipo());
            System.out.println("Estadísticas: " + g.estadisticas());
            System.out.println("Conteo por estado: " + g.conteoPorEstado());
            g.mejores(3).forEach(System.out::println);
            g.buscar(Reglas.conCapacidad(ConGarantia.class)).stream().map(g::describir).forEach(System.out::println);

            System.out.println("== Monitor en segundo plano (reloj simulado +" + (Taller.PLAZO_DIAS + 1) + " días)");
            g.iniciarMonitor(100);
            hoy.set(hoy.get().plusDays(Taller.PLAZO_DIAS + 1));
            Thread.sleep(400);
            g.detenerMonitor();
            System.out.println("Base de datos: " + bd.toAbsolutePath());
        }
    }
}
