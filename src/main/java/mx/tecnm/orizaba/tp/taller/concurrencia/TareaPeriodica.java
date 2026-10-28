package mx.tecnm.orizaba.tp.taller.concurrencia;

/**
 * Tarea que se ejecuta periódicamente en su propio hilo.
 * <ul>
 *   <li>Implementa {@link Runnable} en lugar de extender {@link Thread}
 *       (véase {@code java_4_modelo/t6_concurrencia/EjemploHilos}).</li>
 *   <li>Detención cooperativa: bandera {@code volatile} + {@code interrupt()} + {@code join()}.</li>
 *   <li>Template Method: {@link #run()} es final; la variación está en {@link #ejecutarCiclo()}.</li>
 * </ul>
 */
public abstract class TareaPeriodica implements Runnable {

    private final long periodoMs;
    private volatile boolean activa;
    private Thread hilo;
    private volatile long ciclos;

    protected TareaPeriodica(long periodoMs) {
        if (periodoMs <= 0) {
            throw new IllegalArgumentException("Periodo inválido: " + periodoMs);
        }
        this.periodoMs = periodoMs;
    }

    /** Arranca el hilo (demonio) con el nombre indicado. */
    public final synchronized void iniciar(String nombreHilo) {
        // TODO E12: Si ya está activa lanzar IllegalStateException; crear new Thread(this, nombreHilo), marcarlo demonio, activar y start().
        //validar que la tarea no este en ejecucion
        if (activa){
            throw new IllegalStateException("La tarea ya se encuentra activa.");
        }
        //se crea el hilo que ejecutará la tarea
        hilo = new Thread(this, nombreHilo);
        hilo.setDaemon(true);

        //marcar la tarea en activa
        activa = true;

        //comenzar el nuevo hilo
        hilo.start();
        //throw new UnsupportedOperationException("TODO E12");
    }

    /** Solicita la detención y espera a que el hilo termine. */
    public final void detener() throws InterruptedException {
        // TODO E12: Bajar la bandera, interrumpir el hilo y esperar con join() (si existe).
        //hilo no dene de repetir el trabajo
        activa = false;
        //verificar que el hilo exista
        if(hilo != null){
            //interrumpir una posible espera
            hilo.interrupt();
            //esperar que el hilo termine
            hilo.join();
        }
        //throw new UnsupportedOperationException("TODO E12");
    }

    public final boolean activa() {
        return activa;
    }

    public final long ciclos() {
        return ciclos;
    }

    @Override
    public final void run() {
        // TODO E12: Mientras activa: ejecutarCiclo(), incrementar ciclos y dormir periodoMs; ante InterruptedException restaurar la interrupción y salir.
        while (activa){
            try{
                //ejecutar el trabajo
                ejecutarCiclo();
                //contar el ciclo
                ciclos++;
                //esperar antes del siguiente ciclo
                Thread.sleep(periodoMs);
            }
            catch (InterruptedException e){
                //conservar el estado de interrupcion
                Thread.currentThread().interrupt();
                break;
            }
        }
        activa = false;
        //throw new UnsupportedOperationException("TODO E12");
    }

    /** Trabajo de un ciclo. */
    protected abstract void ejecutarCiclo();
}
