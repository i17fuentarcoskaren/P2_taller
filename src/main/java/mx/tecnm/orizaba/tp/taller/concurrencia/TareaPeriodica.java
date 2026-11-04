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
    /** Periodo de tiempo en milisegundos entre cada ejecución de ciclo. */
    private final long periodoMs;
    /** Bandera volátil que indica si la tarea se encuentra activa. */
    private volatile boolean activa;
    /** Hilo de ejecución asociado a la tarea periódica. */
    private Thread hilo;
    /** Contador atómico o volátil que registra el número total de ciclos ejecutados. */
    private volatile long ciclos;

    /**
     * Construye una nueva tarea periódica con el periodo especificado.
     *
     * @param periodoMs Tiempo en milisegundos entre ciclos. Debe ser mayor a cero.
     * @throws IllegalArgumentException Si el periodo especificado es menor o igual a cero.
     */

    protected TareaPeriodica(long periodoMs) {
        if (periodoMs <= 0) {
            throw new IllegalArgumentException("Periodo inválido: " + periodoMs);
        }
        this.periodoMs = periodoMs;
    }

    /**
     * Arranca el hilo (demonio) con el nombre indicado de manera sincronizada.
     *
     * @param nombreHilo Nombre que se le asignará al hilo de ejecución.
     * @throws IllegalStateException Si la tarea ya se encuentra activa.
     */

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
    /**
     * Comprueba si la tarea se encuentra actualmente activa.
     *
     * @return {@code true} si la tarea está activa; {@code false} en caso contrario.
     */

    public final boolean activa() {
        return activa;
    }

    /**
     * Obtiene el número total de ciclos ejecutados por la tarea.
     *
     * @return Cantidad de ciclos completados.
     */

    public final long ciclos() {
        return ciclos;
    }

    /**
     * Método principal de ejecución del hilo que sigue el patrón Template Method.
     * Ejecuta ciclos periódicos de trabajo mientras la bandera de actividad permanezca encendida.
     */

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
