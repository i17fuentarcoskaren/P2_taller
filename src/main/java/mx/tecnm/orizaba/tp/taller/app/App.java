package mx.tecnm.orizaba.tp.taller.app;

import com.formdev.flatlaf.FlatLightLaf;
import mx.tecnm.orizaba.tp.taller.persistencia.ConexionSQLite;
import mx.tecnm.orizaba.tp.taller.servicio.Taller;
import mx.tecnm.orizaba.tp.taller.ui.VentanaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
/**
 * Clase principal que actúa como el punto de entrada independiente
 * para la ejecución de la interfaz gráfica del sistema de gestión del taller mecánico,
 * utilizando Java Swing en conjunto con el tema moderno FlatLaf.
 *
 * <p>Esta clase se encarga de:</p>
 * <ul>
 *   <li>Configurar el Look and Feel global mediante FlatLaf.</li>
 *   <li>Inicializar la conexión a la base de datos SQLite y el servicio del taller.</li>
 *   <li>Lanzar la ventana principal en el hilo de despacho de eventos (EDT).</li>
 *   <li>Gestionar el cierre seguro de recursos cuando la interfaz es cerrada.</li>
 * </ul>
 *
 * @author Karen Julie Fuentes Arcos
 * @version 1.0
 * @since 2026-10
 */

/** Punto de entrada independiente para la interfaz gráfica Swing + FlatLaf. */
/**
 * Constructor privado para prevenir la instanciación de esta clase utilitaria
 * de punto de entrada (patrón de clase de utilidad).
 */
public final class App {
    private App() { }
    /**
     * Método principal (punto de entrada) de la aplicación de escritorio.
     * Configura el aspecto visual de FlatLaf e inicializa los componentes de la interfaz
     * gráfica de manera segura dentro del hilo de eventos de Swing.
     *
     * @param args Argumentos de la línea de comandos pasados al iniciar la aplicación.
     */

    public static void main(String[] args) {
        // Configurar el aspecto ANTES de crear componentes Swing.
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            ConexionSQLite bd = null;
            try {
                // Se crea taller.db en el directorio desde el que se ejecuta Maven.
                bd = new ConexionSQLite("taller.db");
                Taller taller = new Taller(bd);
                ConexionSQLite conexion = bd;

                //Instanciación de la interfaz gráfica principal con inyección del servicio
                VentanaPrincipal ventana = new VentanaPrincipal(taller);
                //Cierre seguro de recursos al cerrar la ventana principal de la aplicación
                ventana.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        try {
                            taller.close();
                        } finally {
                            conexion.close();
                        }
                    }
                });
                ventana.setVisible(true);
            } catch (Exception e) {
                if (bd != null) {
                    bd.close();
                }
                JOptionPane.showMessageDialog(null,
                        "No se pudo abrir la aplicación:\n" + e.getMessage(),
                        "Error al iniciar P2_taller", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
