package mx.tecnm.orizaba.tp.taller.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import mx.tecnm.orizaba.tp.taller.eventos.OyenteEvento;
import mx.tecnm.orizaba.tp.taller.modelo.Cliente;
import mx.tecnm.orizaba.tp.taller.modelo.Concepto;
import mx.tecnm.orizaba.tp.taller.modelo.Estado;
import mx.tecnm.orizaba.tp.taller.modelo.Nivel;
import mx.tecnm.orizaba.tp.taller.modelo.OrdenServicio;
import mx.tecnm.orizaba.tp.taller.persistencia.Respaldo;
import mx.tecnm.orizaba.tp.taller.servicio.Taller;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;

/** Pantalla de gestión P2: todas las operaciones pasan por la fachada Taller. */
public final class VentanaPrincipal extends JFrame {
    // Identidad visual: estilo industrial con acentos en naranja.
    private static final Color NARANJA = new Color(244, 111, 43);
    private static final Color MARINO = new Color(18, 28, 45);
    private static final Color MARINO_CLARO = new Color(32, 45, 65);
    private static final Color TEXTO = new Color(26, 39, 59);
    private static final Color MUTED = new Color(109, 121, 139);
    private static final Color VERDE = new Color(26, 159, 114);
    private static final Color AZUL = new Color(45, 106, 207);
    private static final Color ROJO = new Color(213, 80, 85);
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX"));
    private final Taller taller;
    private final JPanel contenido = new JPanel(new BorderLayout());
    private final JLabel titulo = new JLabel("Inicio");
    private final JLabel subtituloVista = new JLabel();
    private final JLabel ruta = new JLabel();
    private final Map<String, JButton> botonesMenu = new LinkedHashMap<>();
    private final JTextArea eventos = new JTextArea(12, 48);
    private final OyenteEvento oyente;
    private String seccion = "Inicio";
    private boolean oscuro;

    public VentanaPrincipal(Taller taller) {
        super("TALLER / P2 · Administración de taller mecánico");
        this.taller = Objects.requireNonNull(taller, "taller");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 660));
        setSize(1320, 840);
        setLocationRelativeTo(null);

        eventos.setEditable(false);
        eventos.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        oyente = evento -> SwingUtilities.invokeLater(() -> {
            eventos.append(evento + System.lineSeparator());
            eventos.setCaretPosition(eventos.getDocument().getLength());
            if ("Inicio".equals(seccion) || "Monitor".equals(seccion)) mostrar(seccion);
        });
        taller.suscribir(oyente);
        construirMarco();
        mostrar("Inicio");
    }

    @Override public void dispose() {
        taller.desuscribir(oyente);
        super.dispose();
    }

    private Color fondo() { return oscuro ? new Color(16, 25, 39) : new Color(244, 247, 251); }
    private Color superficie() { return oscuro ? new Color(30, 42, 58) : Color.WHITE; }
    private Color tinta() { return oscuro ? new Color(237, 243, 249) : TEXTO; }
    private Color tenue() { return oscuro ? new Color(168, 183, 199) : MUTED; }
    private Color borde() { return oscuro ? new Color(56, 70, 89) : new Color(228, 234, 242); }

    private void construirMarco() {
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(fondo());
        add(crearMenu(), BorderLayout.WEST);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 23));
        cuerpo.setBackground(fondo());
        cuerpo.setBorder(new EmptyBorder(26, 32, 25, 32));
        JPanel barra = new JPanel(new BorderLayout(0, 8));
        barra.setOpaque(false);
        ruta.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        ruta.setForeground(NARANJA);
        barra.add(ruta, BorderLayout.NORTH);

        JPanel fila = new JPanel(new BorderLayout(12, 0));
        fila.setOpaque(false);
        JPanel rotulos = new JPanel();
        rotulos.setLayout(new BoxLayout(rotulos, BoxLayout.Y_AXIS));
        rotulos.setOpaque(false);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 29));
        titulo.setForeground(tinta());
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtituloVista.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        subtituloVista.setForeground(tenue());
        subtituloVista.setBorder(new EmptyBorder(6, 0, 0, 0));
        subtituloVista.setAlignmentX(Component.LEFT_ALIGNMENT);
        rotulos.add(titulo);
        rotulos.add(subtituloVista);
        fila.add(rotulos, BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 3));
        acciones.setOpaque(false);
        JButton tema = boton(oscuro ? "☀  Tema claro" : "◐  Tema oscuro", this::alternarTema);
        acciones.add(tema);
        JButton actualizar = boton("↻  Actualizar", () -> mostrar(seccion));
        acciones.add(actualizar);
        fila.add(acciones, BorderLayout.EAST);
        barra.add(fila, BorderLayout.CENTER);
        cuerpo.add(barra, BorderLayout.NORTH);
        contenido.setOpaque(true);
        contenido.setBackground(fondo());
        cuerpo.add(contenido, BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void alternarTema() {
        oscuro = !oscuro;
        if (oscuro) FlatDarkLaf.setup(); else FlatLightLaf.setup();
        SwingUtilities.updateComponentTreeUI(this);
        construirMarco();
        mostrar(seccion);
    }

    private JPanel crearMenu() {
        botonesMenu.clear();
        JPanel menu = new JPanel(new BorderLayout());
        menu.setBackground(MARINO);
        menu.setPreferredSize(new Dimension(253, 0));
        menu.setBorder(new EmptyBorder(27, 16, 23, 16));

        JPanel superior = new JPanel();
        superior.setLayout(new BoxLayout(superior, BoxLayout.Y_AXIS));
        superior.setOpaque(false);
        JPanel marcaFila = new JPanel(new FlowLayout(FlowLayout.LEFT, 11, 0));
        marcaFila.setOpaque(false);
        JLabel emblema = new JLabel("M", SwingConstants.CENTER);
        emblema.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        emblema.setForeground(Color.WHITE);
        emblema.setOpaque(true);
        emblema.setBackground(NARANJA);
        emblema.setPreferredSize(new Dimension(42, 42));
        marcaFila.add(emblema);
        JPanel marcaTextos = new JPanel(new GridLayout(2, 1, 0, 1));
        marcaTextos.setOpaque(false);
        JLabel nombre = new JLabel("TALLER / P2");
        nombre.setForeground(Color.WHITE);
        nombre.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        JLabel rubro = new JLabel("GESTIÓN DE TALLER");
        rubro.setForeground(new Color(159, 172, 192));
        rubro.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
        marcaTextos.add(nombre);
        marcaTextos.add(rubro);
        marcaFila.add(marcaTextos);
        marcaFila.setAlignmentX(Component.LEFT_ALIGNMENT);
        superior.add(marcaFila);
        superior.add(Box.createVerticalStrut(43));
        JLabel seccionTitulo = new JLabel("ESPACIO DE TRABAJO");
        seccionTitulo.setForeground(new Color(117, 135, 158));
        seccionTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        seccionTitulo.setBorder(new EmptyBorder(0, 11, 13, 0));
        seccionTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        superior.add(seccionTitulo);
        String[] nombres = {"Inicio", "Clientes", "Conceptos", "Órdenes", "Reportes", "Monitor", "Respaldo"};
        String[] simbolos = {"▦", "◉", "⚒", "▤", "◫", "◌", "▣"};
        for (int i = 0; i < nombres.length; i++) {
            String destino = nombres[i];
            JButton b = new JButton(simbolos[i] + "     " + destino);
            b.setName("nav" + destino);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            b.setForeground(new Color(185, 196, 212));
            b.setBackground(MARINO);
            b.setBorder(new EmptyBorder(12, 18, 12, 12));
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setOpaque(true);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.addActionListener(e -> mostrar(destino));
            botonesMenu.put(destino, b);
            superior.add(b);
            superior.add(Box.createVerticalStrut(5));
        }
        menu.add(superior, BorderLayout.NORTH);

        JPanel pie = new JPanel();
        pie.setLayout(new BoxLayout(pie, BoxLayout.Y_AXIS));
        pie.setOpaque(false);
        pie.setBorder(new EmptyBorder(14, 11, 1, 4));
        JLabel online = new JLabel("●  DATOS LOCALES");
        online.setForeground(new Color(83, 215, 164));
        online.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        online.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel plataforma = new JLabel("Java 21  /  SQLite  /  FlatLaf");
        plataforma.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        plataforma.setForeground(new Color(139, 158, 181));
        plataforma.setBorder(new EmptyBorder(9, 0, 0, 0));
        plataforma.setAlignmentX(Component.LEFT_ALIGNMENT);
        pie.add(online);
        pie.add(plataforma);
        menu.add(pie, BorderLayout.SOUTH);
        return menu;
    }

    private void mostrar(String destino) {
        seccion = destino;
        titulo.setText(destino.equals("Inicio") ? "Panel de control" : destino);
        ruta.setText("TALLER/ P2    /    " + destino.toUpperCase(Locale.forLanguageTag("es-MX")));
        subtituloVista.setText(switch (destino) {
            case "Clientes" -> "Administra los datos y niveles de tus clientes.";
            case "Conceptos" -> "Refacciones, diagnósticos y mano de obra del catálogo.";
            case "Órdenes" -> "Da seguimiento a cada trabajo desde su recepción hasta la entrega.";
            case "Reportes" -> "Revisa el rendimiento y exporta tus resultados.";
            case "Monitor" -> "Consulta alertas y supervisa trabajos que requieren atención.";
            case "Respaldo" -> "Genera e inspecciona copias de seguridad del taller.";
            default -> "Resumen de actividad y operaciones del taller.";
        });
        botonesMenu.forEach((nombre, b) -> {
            boolean activo = nombre.equals(destino);
            b.setBackground(activo ? MARINO_CLARO : MARINO);
            b.setForeground(activo ? Color.WHITE : new Color(185, 196, 212));
            b.setFont(new Font(Font.SANS_SERIF, activo ? Font.BOLD : Font.PLAIN, 14));
        });
        contenido.removeAll();
        JPanel panel = switch (destino) {
            case "Clientes" -> vistaClientes();
            case "Conceptos" -> vistaConceptos();
            case "Órdenes" -> vistaOrdenes();
            case "Reportes" -> vistaReportes();
            case "Monitor" -> vistaMonitor();
            case "Respaldo" -> vistaRespaldo();
            default -> vistaInicio();
        };
        panel.setBackground(fondo());
        contenido.setBackground(fondo());
        contenido.add(panel, BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();
    }

    private JPanel vistaInicio() {
        JPanel lienzo = new JPanel(new BorderLayout());
        lienzo.setBackground(fondo());
        JPanel principal = new JPanel();
        principal.setLayout(new BoxLayout(principal, BoxLayout.Y_AXIS));
        principal.setOpaque(false);
        principal.setBorder(new EmptyBorder(0, 0, 10, 7));

        JPanel portada = new BannerPanel();
        portada.setLayout(new BorderLayout(25, 0));
        portada.setBorder(new EmptyBorder(25, 28, 24, 26));
        portada.setMaximumSize(new Dimension(Integer.MAX_VALUE, 167));
        portada.setPreferredSize(new Dimension(600, 167));
        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        JLabel saludo = new JLabel("TU TALLER, BAJO CONTROL");
        saludo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        saludo.setForeground(new Color(255, 182, 136));
        JLabel mensaje = new JLabel("Todo listo para trabajar.");
        mensaje.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 25));
        mensaje.setForeground(Color.WHITE);
        mensaje.setBorder(new EmptyBorder(9, 0, 9, 0));
        JLabel consejo = new JLabel("Clientes, trabajos e indicadores en un mismo lugar.");
        consejo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        consejo.setForeground(new Color(204, 216, 232));
        textos.add(saludo);
        textos.add(mensaje);
        textos.add(consejo);
        portada.add(textos, BorderLayout.CENTER);
        JPanel atajos = new JPanel(new GridLayout(2, 1, 0, 11));
        atajos.setOpaque(false);
        atajos.add(botonPrimario("+  Nueva orden", this::nuevaOrden));
        atajos.add(botonClaro("+  Agregar cliente", this::nuevoCliente));
        portada.add(atajos, BorderLayout.EAST);
        principal.add(portada);
        principal.add(Box.createVerticalStrut(22));

        List<OrdenServicio> ordenes = taller.registros();
        JPanel estadisticas = new JPanel(new GridLayout(1, 4, 15, 0));
        estadisticas.setOpaque(false);
        estadisticas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 137));
        estadisticas.setPreferredSize(new Dimension(750, 137));
        estadisticas.add(tarjeta("CLIENTES", taller.clientes().size(), "Clientes registrados", AZUL));
        estadisticas.add(tarjeta("CATÁLOGO", taller.catalogo().tamanio(), "Conceptos disponibles", new Color(118, 85, 194)));
        estadisticas.add(tarjeta("EN REPARACIÓN", taller.registrosEn(Estado.EN_CURSO).size(), "Trabajos activos", NARANJA));
        estadisticas.add(tarjeta("ENTREGADAS", taller.registrosEn(Estado.CERRADA).size(), "Órdenes concluidas", VERDE));
        principal.add(estadisticas);
        principal.add(Box.createVerticalStrut(22));

        JPanel dosColumnas = new JPanel(new GridLayout(1, 2, 17, 0));
        dosColumnas.setOpaque(false);
        dosColumnas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 315));
        dosColumnas.setPreferredSize(new Dimension(750, 315));

        JPanel lista = caja(new BorderLayout(0, 15), 20);
        JPanel titularLista = new JPanel(new BorderLayout());
        titularLista.setOpaque(false);
        titularLista.add(etiquetaGrande("Órdenes recientes"), BorderLayout.WEST);
        JButton verTodas = botonTexto("Ver todas  →", () -> mostrar("Órdenes"));
        titularLista.add(verTodas, BorderLayout.EAST);
        lista.add(titularLista, BorderLayout.NORTH);
        DefaultTableModel modelo = tablaModelo("N° ORDEN", "CLIENTE", "ESTADO", "TOTAL");
        ordenes.stream().sorted(Comparator.comparingInt(OrdenServicio::id).reversed()).limit(5)
                .forEach(o -> modelo.addRow(new Object[]{"#" + o.id(), o.cliente().nombre(),
                        o.estado().etiqueta(), MONEDA.format(o.total())}));
        JTable recientes = tabla(modelo);
        recientes.setRowHeight(41);
        JScrollPane listado = new JScrollPane(recientes);
        listado.setBorder(BorderFactory.createEmptyBorder());
        listado.getViewport().setBackground(superficie());
        lista.add(listado, BorderLayout.CENTER);
        if (modelo.getRowCount() == 0) {
            JLabel vacio = new JLabel("Todavía no hay órdenes. Crea tu primera orden.", SwingConstants.CENTER);
            vacio.setForeground(tenue());
            lista.add(vacio, BorderLayout.SOUTH);
        }
        dosColumnas.add(lista);

        JPanel resumen = caja(new BorderLayout(0, 17), 21);
        resumen.add(etiquetaGrande("Estado de las órdenes"), BorderLayout.NORTH);
        JPanel filas = new JPanel(new GridLayout(4, 1, 0, 11));
        filas.setOpaque(false);
        Map<Estado, Long> conteos = taller.conteoPorEstado();
        int total = Math.max(1, ordenes.size());
        for (Estado estado : Estado.values()) {
            int valor = conteos.getOrDefault(estado, 0L).intValue();
            Color color = colorEstado(estado);
            JPanel fila = new JPanel(new BorderLayout(0, 6));
            fila.setOpaque(false);
            JPanel filaTitulos = new JPanel(new BorderLayout());
            filaTitulos.setOpaque(false);
            JLabel nombreEstado = new JLabel(estado.etiqueta());
            nombreEstado.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            nombreEstado.setForeground(tinta());
            JLabel cifra = new JLabel(valor + " orden(es)");
            cifra.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            cifra.setForeground(tenue());
            filaTitulos.add(nombreEstado, BorderLayout.WEST);
            filaTitulos.add(cifra, BorderLayout.EAST);
            fila.add(filaTitulos, BorderLayout.NORTH);
            JProgressBar barra = new JProgressBar(0, total);
            barra.setValue(valor);
            barra.setForeground(color);
            barra.setBackground(oscuro ? MARINO_CLARO : new Color(234, 239, 246));
            barra.setBorderPainted(false);
            barra.setPreferredSize(new Dimension(100, 9));
            fila.add(barra, BorderLayout.CENTER);
            filas.add(fila);
        }
        resumen.add(filas, BorderLayout.CENTER);
        JPanel nota = new JPanel(new BorderLayout());
        nota.setOpaque(false);
        JLabel leyenda = new JLabel("Los datos se actualizan desde SQLite.");
        leyenda.setForeground(tenue());
        leyenda.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        nota.add(leyenda, BorderLayout.WEST);
        resumen.add(nota, BorderLayout.SOUTH);
        dosColumnas.add(resumen);
        principal.add(dosColumnas);

        JScrollPane scroll = new JScrollPane(principal);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setViewportBorder(null);
        scroll.getViewport().setBackground(fondo());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        lienzo.add(scroll, BorderLayout.CENTER);
        return lienzo;
    }

    private Color colorEstado(Estado e) {
        return switch (e) {
            case ABIERTA -> AZUL;
            case EN_CURSO -> NARANJA;
            case CERRADA -> VERDE;
            case CANCELADA -> ROJO;
        };
    }

    private JPanel tarjeta(String clave, int valor, String descripcion, Color acento) {
        JPanel p = caja(new BorderLayout(0, 9), 17);
        JLabel cab = new JLabel(clave);
        cab.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        cab.setForeground(tenue());
        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        superior.add(cab, BorderLayout.WEST);
        JLabel punto = new JLabel("●");
        punto.setForeground(acento);
        superior.add(punto, BorderLayout.EAST);
        p.add(superior, BorderLayout.NORTH);
        JLabel numero = new JLabel(Integer.toString(valor));
        numero.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        numero.setForeground(tinta());
        p.add(numero, BorderLayout.CENTER);
        JLabel desc = new JLabel(descripcion);
        desc.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        desc.setForeground(tenue());
        p.add(desc, BorderLayout.SOUTH);
        return p;
    }

    private JPanel caja(LayoutManager layout, int margen) {
        JPanel panel = new PanelRedondeado(superficie(), borde());
        panel.setLayout(layout);
        panel.setBorder(new EmptyBorder(margen, margen, margen, margen));
        return panel;
    }

    private static final class PanelRedondeado extends JPanel {
        private final Color fondo, linea;
        private PanelRedondeado(Color fondo, Color linea) {
            this.fondo = fondo;
            this.linea = linea;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D d = (Graphics2D) g.create();
            d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            d.setColor(fondo);
            d.fill(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 18, 18));
            d.setColor(linea);
            d.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 18, 18));
            d.dispose();
            super.paintComponent(g);
        }
    }

    private static final class BannerPanel extends JPanel {
        private BannerPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D d = (Graphics2D) g.create();
            d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            d.setPaint(new GradientPaint(0, 0, new Color(25, 40, 62), getWidth(), getHeight(), new Color(40, 62, 83)));
            d.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            d.setColor(new Color(255, 255, 255, 14));
            d.setStroke(new BasicStroke(2));
            for (int i=0; i<6; i++) d.drawOval(getWidth()-220+i*24, -110+i*18, 220, 220);
            d.dispose();
            super.paintComponent(g);
        }
    }

    private JPanel vistaClientes() {
        DefaultTableModel m = tablaModelo("ID", "Nombre", "Correo", "Nivel");
        for (Cliente c : taller.clientes()) m.addRow(new Object[]{c.id(), c.nombre(), c.correo(), c.nivel().etiqueta()});
        JTable t = tabla(m);
        JPanel p = panelLista(t);
        JPanel barra = herramientas(t, "Buscar cliente por nombre, correo o ID...");
        barra.add(botonPrimario("+  Nuevo cliente", this::nuevoCliente));
        barra.add(boton("Editar", () -> {
            Integer id = idSeleccionado(t);
            if (id != null) editarCliente(id);
        }));
        barra.add(boton("Eliminar", () -> {
            Integer id = idSeleccionado(t);
            if (id != null && confirmar("¿Eliminar al cliente #" + id + "?")) {
                ejecutar(() -> taller.eliminarCliente(id), "Cliente eliminado");
            }
        }));
        p.add(barra, BorderLayout.NORTH);
        return p;
    }

    private void nuevoCliente() {
        JTextField nombre = new JTextField(23);
        JTextField correo = new JTextField(23);
        JComboBox<Nivel> nivel = new JComboBox<>(Nivel.values());
        if (!dialogoFormulario("Registrar cliente", "Nombre completo", nombre, "Correo electrónico", correo, "Tipo de cliente", nivel)) return;
        ejecutar(() -> taller.registrarCliente(nombre.getText().strip(), correo.getText().strip(), (Nivel) nivel.getSelectedItem()),
                "Cliente registrado y guardado en SQLite");
    }

    private void editarCliente(int id) {
        Cliente c = taller.cliente(id);
        JTextField nombre = new JTextField(c.nombre(), 23);
        JTextField correo = new JTextField(c.correo(), 23);
        JComboBox<Nivel> nivel = new JComboBox<>(Nivel.values());
        nivel.setSelectedItem(c.nivel());
        if (!dialogoFormulario("Editar cliente #" + id, "Nombre completo", nombre, "Correo electrónico", correo, "Tipo de cliente", nivel)) return;
        ejecutar(() -> taller.actualizarCliente(id, nombre.getText().strip(), correo.getText().strip(), (Nivel) nivel.getSelectedItem()),
                "Cliente actualizado");
    }

    private JPanel vistaConceptos() {
        DefaultTableModel m = tablaModelo("Clave", "Tipo", "Nombre", "Precio base", "Atributo");
        for (Concepto c : conceptos()) m.addRow(new Object[]{c.clave().toString(), c.tipo(), c.nombre(), MONEDA.format(c.precioBase()), atributo(c)});
        JTable t = tabla(m);
        JPanel p = panelLista(t);
        JPanel barra = herramientas(t, "Buscar concepto...");
        barra.add(botonPrimario("+  Nuevo concepto", this::nuevoConcepto));
        barra.add(boton("Editar", () -> {
            String clave = claveSeleccionada(t);
            if (clave != null) editarConcepto(clave);
        }));
        barra.add(boton("Retirar", () -> {
            String clave = claveSeleccionada(t);
            if (clave != null && confirmar("¿Retirar el concepto " + clave + "? No debe estar en ninguna orden.")) {
                ejecutar(() -> taller.retirar(taller.elemento(clave).clave()), "Concepto retirado");
            }
        }));
        p.add(barra, BorderLayout.NORTH);
        return p;
    }

    private List<Concepto> conceptos() {
        return taller.catalogo().ordenados(Comparator.comparing(c -> c.clave().toString()));
    }

    private String atributo(Concepto c) {
        return switch (c) {
            case Concepto.Refaccion r -> r.marca();
            case Concepto.ManoDeObra o -> "Nivel " + o.nivelTecnico();
            case Concepto.Diagnostico d -> d.equipo();
        };
    }

    private void nuevoConcepto() {
        JComboBox<String> tipo = new JComboBox<>(new String[]{"Refacción", "Mano de obra", "Diagnóstico"});
        JTextField nombre = new JTextField(22);
        JTextField precio = new JTextField("0.00", 22);
        JTextField atributo = new JTextField(22);
        JLabel etiquetaAtributo = new JLabel("Marca (o nivel técnico 1–3 / equipo)");
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 5, 6, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        panel.add(new JLabel("Tipo"), g); g.gridx = 1; g.weightx = 1; panel.add(tipo, g);
        g.gridy++; g.gridx = 0; g.weightx = 0; panel.add(new JLabel("Nombre"), g);
        g.gridx = 1; g.weightx = 1; panel.add(nombre, g);
        g.gridy++; g.gridx = 0; g.weightx = 0; panel.add(new JLabel("Precio base MXN"), g);
        g.gridx = 1; g.weightx = 1; panel.add(precio, g);
        g.gridy++; g.gridx = 0; g.weightx = 0; panel.add(etiquetaAtributo, g);
        g.gridx = 1; g.weightx = 1; panel.add(atributo, g);
        tipo.addActionListener(e -> etiquetaAtributo.setText(switch (tipo.getSelectedIndex()) {
            case 0 -> "Marca";
            case 1 -> "Nivel técnico (1–3)";
            default -> "Equipo";
        }));
        tipo.setSelectedIndex(1);
        tipo.setSelectedIndex(0);
        if (JOptionPane.showConfirmDialog(this, panel, "Registrar concepto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) return;
        ejecutar(() -> {
            String n = nombre.getText().strip();
            String a = atributo.getText().strip();
            double p = decimal(precio.getText());
            switch (tipo.getSelectedIndex()) {
                case 0 -> taller.registrar(Concepto.Refaccion::new, n, p, a);
                case 1 -> taller.registrar(Concepto.ManoDeObra::new, n, p, entero(a));
                default -> taller.registrar(Concepto.Diagnostico::new, n, p, a);
            }
        }, "Concepto guardado correctamente");
    }

    private void editarConcepto(String clave) {
        Concepto c = taller.elemento(clave);
        JTextField nombre = new JTextField(c.nombre(), 23);
        JTextField precio = new JTextField(Double.toString(c.precioBase()), 23);
        if (!dialogoFormulario("Editar " + clave + " (" + c.tipo() + ")", "Nombre", nombre, "Precio base MXN", precio)) return;
        ejecutar(() -> taller.actualizar(c, nombre.getText().strip(), decimal(precio.getText())), "Concepto actualizado");
    }

    private JPanel vistaOrdenes() {
        DefaultTableModel m = tablaModelo("ID", "Cliente", "Fecha", "Estado", "Líneas", "Total MXN");
        for (OrdenServicio o : taller.registros()) m.addRow(new Object[]{o.id(), o.cliente().nombre(), o.fecha(), o.estado().etiqueta(), o.lineas().size(), MONEDA.format(o.total())});
        JTable t = tabla(m);
        JPanel p = panelLista(t);
        JPanel barra = herramientas(t, "Buscar orden, cliente o estado...");
        barra.add(botonPrimario("+  Nueva orden", this::nuevaOrden));
        barra.add(boton("Ver / administrar", () -> {
            Integer id = idSeleccionado(t);
            if (id != null) administrarOrden(id);
        }));
        p.add(barra, BorderLayout.NORTH);
        t.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Integer id = idSeleccionado(t);
                    if (id != null) administrarOrden(id);
                }
            }
        });
        return p;
    }

    private void nuevaOrden() {
        List<Cliente> clientes = taller.clientes();
        if (clientes.isEmpty()) { aviso("Primero registra al menos un cliente."); mostrar("Clientes"); return; }
        JComboBox<ClienteOpcion> seleccion = new JComboBox<>();
        for (Cliente c : clientes) seleccion.addItem(new ClienteOpcion(c.id(), c.nombre()));
        if (!dialogoFormulario("Abrir orden de servicio", "Cliente", seleccion)) return;
        ClienteOpcion opcion = (ClienteOpcion) seleccion.getSelectedItem();
        if (opcion != null) ejecutar(() -> taller.abrir(opcion.id()), "Orden creada. Ahora agrega al menos un concepto.");
    }

    private record ClienteOpcion(int id, String nombre) {
        @Override public String toString() { return "#" + id + " · " + nombre; }
    }

    private record ConceptoOpcion(String clave, String nombre) {
        @Override public String toString() { return clave + " · " + nombre; }
    }

    private void administrarOrden(int id) {
        OrdenServicio o = taller.registro(id);
        JDialog dialogo = new JDialog(this, "Orden #" + id + " · " + o.cliente().nombre(), true);
        dialogo.setSize(780, 520);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(12, 12));
        JPanel cuerpo = new JPanel(new BorderLayout(0, 13));
        cuerpo.setBorder(new EmptyBorder(20, 20, 8, 20));
        JLabel encabezado = etiquetaGrande("Orden #" + o.id() + "  ·  " + o.estado().etiqueta());
        cuerpo.add(encabezado, BorderLayout.NORTH);
        DefaultTableModel m = tablaModelo("Clave", "Concepto", "Cantidad", "Subtotal", "Importe con descuento");
        for (OrdenServicio.Linea linea : o.lineas()) {
            m.addRow(new Object[]{linea.elemento().clave().toString(), linea.elemento().nombre(), linea.unidades(),
                    MONEDA.format(linea.subtotal()), MONEDA.format(linea.importe())});
        }
        JTable tabla = tabla(m);
        cuerpo.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JLabel total = etiquetaGrande("Subtotal: " + MONEDA.format(o.subtotal()) + "  ·  Total: " + MONEDA.format(o.total()));
        cuerpo.add(total, BorderLayout.SOUTH);
        dialogo.add(cuerpo, BorderLayout.CENTER);
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        if (o.estado() == Estado.ABIERTA) {
            pie.add(boton("+ Agregar línea", () -> { dialogo.dispose(); agregarLinea(id); }));
            pie.add(boton("Quitar línea", () -> {
                int fila = tabla.getSelectedRow();
                if (fila < 0) { aviso("Selecciona una línea de la tabla."); return; }
                String clave = m.getValueAt(tabla.convertRowIndexToModel(fila), 0).toString();
                if (confirmar("¿Quitar " + clave + " de la orden?")) {
                    ejecutar(() -> taller.quitarLinea(id, clave), "Línea eliminada");
                    dialogo.dispose();
                    administrarOrden(id);
                }
            }));
            pie.add(boton("Iniciar reparación", () -> cambiarEstado(id, "iniciar", dialogo)));
        }
        if (o.estado() == Estado.EN_CURSO) pie.add(boton("Entregar / Cerrar", () -> cambiarEstado(id, "cerrar", dialogo)));
        if (o.estado() == Estado.ABIERTA || o.estado() == Estado.EN_CURSO) {
            pie.add(boton("Cancelar orden", () -> {
                if (confirmar("¿Cancelar la orden #" + id + "?")) cambiarEstado(id, "cancelar", dialogo);
            }));
        }
        pie.add(boton("Cerrar ventana", dialogo::dispose));
        dialogo.add(pie, BorderLayout.SOUTH);
        dialogo.setVisible(true);
    }

    private void agregarLinea(int id) {
        if (conceptos().isEmpty()) { aviso("Primero registra un concepto."); mostrar("Conceptos"); return; }
        JComboBox<ConceptoOpcion> combo = new JComboBox<>();
        for (Concepto c : conceptos()) combo.addItem(new ConceptoOpcion(c.clave().toString(), c.nombre()));
        JSpinner unidades = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        if (!dialogoFormulario("Agregar línea a orden #" + id, "Concepto", combo, "Unidades", unidades)) {
            administrarOrden(id);
            return;
        }
        ConceptoOpcion opcion = (ConceptoOpcion) combo.getSelectedItem();
        if (opcion != null) ejecutar(() -> taller.agregarLinea(id, opcion.clave(), (Integer) unidades.getValue()), "Línea agregada");
        administrarOrden(id);
    }

    private void cambiarEstado(int id, String operacion, JDialog d) {
        ejecutar(() -> {
            switch (operacion) {
                case "iniciar" -> taller.iniciar(id);
                case "cerrar" -> taller.cerrar(id);
                case "cancelar" -> taller.cancelar(id);
                default -> throw new IllegalArgumentException("Transición desconocida");
            }
        }, "Estado de la orden actualizado");
        d.dispose();
    }

    private JPanel vistaReportes() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        arriba.add(boton("Actualizar reportes", () -> mostrar("Reportes")));
        arriba.add(boton("Exportar CSV de órdenes", this::exportarCsv));
        p.add(arriba, BorderLayout.NORTH);
        DoubleSummaryStatistics st = taller.estadisticas();
        StringBuilder sb = new StringBuilder();
        sb.append("RESUMEN DE ÓRDENES ENTREGADAS\n\n");
        sb.append("Órdenes cerradas: ").append(st.getCount()).append('\n');
        sb.append("Total de ingresos: ").append(MONEDA.format(st.getSum())).append('\n');
        sb.append("Ticket promedio: ").append(MONEDA.format(st.getCount() == 0 ? 0 : st.getAverage())).append("\n\n");
        sb.append("INGRESOS POR TIPO DE CONCEPTO\n");
        if (taller.ingresosPorTipo().isEmpty()) sb.append("Aún no hay órdenes entregadas.\n");
        else taller.ingresosPorTipo().forEach((k, v) -> sb.append("  ").append(k).append(": ").append(MONEDA.format(v)).append('\n'));
        sb.append("\nCONCEPTO MÁS DEMANDADO\n");
        sb.append(taller.masDemandado().map(c -> c.clave() + " · " + c.nombre()).orElse("Sin datos")).append("\n\n");
        sb.append("MEJORES CLIENTES (SEGÚN ÓRDENES ENTREGADAS)\n");
        List<Cliente> mejores = taller.mejores(5);
        if (mejores.isEmpty()) sb.append("Sin datos.\n");
        else mejores.forEach(c -> sb.append("  #").append(c.id()).append(" ").append(c.nombre()).append('\n'));
        JTextArea area = textoInformativo(sb.toString());
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        JPanel tarjeta = caja(new BorderLayout(), 22);
        JScrollPane desplazamiento = new JScrollPane(area);
        desplazamiento.setBorder(BorderFactory.createEmptyBorder());
        tarjeta.add(desplazamiento, BorderLayout.CENTER);
        p.add(tarjeta, BorderLayout.CENTER);
        return p;
    }

    private void exportarCsv() {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new java.io.File("ordenes_taller.csv"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path ruta = selector.getSelectedFile().toPath();
        if (Files.exists(ruta) && !confirmar("El archivo existe. ¿Sobrescribirlo?")) return;
        ejecutar(() -> {
            StringBuilder sb = new StringBuilder("ID,Cliente,Fecha,Estado,Total\n");
            for (OrdenServicio o : taller.registros()) sb.append(o.id()).append(',')
                    .append(csv(o.cliente().nombre())).append(',').append(csv(o.fecha().toString())).append(',')
                    .append(csv(o.estado().name())).append(',').append(o.total()).append('\n');
            Files.writeString(ruta, "\uFEFF" + sb, StandardCharsets.UTF_8);
        }, "Archivo CSV creado: " + ruta);
    }

    private String csv(String texto) { return "\"" + texto.replace("\"", "\"\"") + "\""; }

    private JPanel vistaMonitor() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        JLabel estado = new JLabel(taller.monitorActivo() ? "Monitor activo" : "Monitor detenido");
        estado.setFont(estado.getFont().deriveFont(Font.BOLD));
        acciones.add(estado);
        acciones.add(boton("Revisar ahora", () -> ejecutar(() -> {
            int n = taller.revisar();
            SwingUtilities.invokeLater(() -> aviso("Revisión terminada: " + n + " acción(es)."));
        }, "Revisión finalizada")));
        acciones.add(boton("Iniciar (cada 30 s)", () -> ejecutar(() -> taller.iniciarMonitor(30_000), "Monitor iniciado")));
        acciones.add(boton("Detener", () -> ejecutar(() -> {
            try { taller.detenerMonitor(); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new IllegalStateException("Interrumpido", ex); }
        }, "Monitor detenido")));
        p.add(acciones, BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.add(etiquetaGrande("Eventos y alertas"), BorderLayout.NORTH);
        centro.add(new JScrollPane(eventos), BorderLayout.CENTER);
        p.add(centro, BorderLayout.CENTER);
        p.add(textoInformativo("El monitor cancela órdenes abiertas sin líneas con 1 día o más, "
                + "y avisa sobre órdenes en reparación con más de 5 días. Los eventos se muestran aquí."), BorderLayout.SOUTH);
        return p;
    }

    private JPanel vistaRespaldo() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        botones.add(boton("Exportar respaldo binario", this::exportarRespaldo));
        botones.add(boton("Inspeccionar un respaldo", this::inspeccionarRespaldo));
        p.add(botones, BorderLayout.NORTH);
        p.add(textoInformativo("EXPORTACIÓN\n\nGuarda una copia binaria del catálogo, clientes y órdenes de servicio.\n\n"
                + "INSPECCIÓN\n\nPuedes abrir un respaldo para revisar su encabezado, fecha y cantidad de registros. "
                + "Por seguridad, esta pantalla NO reemplaza la base de datos al inspeccionarlo.\n\n"
                + "Los datos de trabajo se almacenan automáticamente en el archivo SQLite taller.db."), BorderLayout.CENTER);
        return p;
    }

    private void exportarRespaldo() {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new java.io.File("respaldo_taller.bin"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path ruta = selector.getSelectedFile().toPath();
        if (Files.exists(ruta) && !confirmar("El respaldo ya existe. ¿Sobrescribirlo?")) return;
        ejecutar(() -> taller.exportarRespaldo(ruta), "Respaldo exportado: " + ruta);
    }

    private void inspeccionarRespaldo() {
        JFileChooser selector = new JFileChooser();
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path ruta = selector.getSelectedFile().toPath();
        // Solo se leen archivos elegidos expresamente por el usuario.
        ejecutar(() -> {
            Respaldo.Contenido c = Respaldo.importar(ruta);
            aviso("Respaldo válido\nFecha: " + c.fecha() + "\nClientes: " + c.clientes().size()
                    + "\nConceptos: " + c.catalogo().tamanio() + "\nÓrdenes: " + c.registros().size()
                    + "\n\nLa base de datos NO fue modificada.");
        }, "Respaldo inspeccionado");
    }

    private static DefaultTableModel tablaModelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
    }

    private JTable tabla(DefaultTableModel m) {
        JTable t = new JTable(m) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? superficie() : (oscuro ? new Color(36, 49, 65) : new Color(249, 251, 254)));
                    c.setForeground(tinta());
                }
                if (c instanceof JComponent comp) comp.setBorder(new EmptyBorder(0, 10, 0, 6));
                return c;
            }
        };
        t.setRowHeight(42);
        t.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        t.setBackground(superficie());
        t.setForeground(tinta());
        t.setSelectionBackground(oscuro ? new Color(55, 79, 108) : new Color(223, 237, 251));
        t.setSelectionForeground(tinta());
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        t.getTableHeader().setBackground(superficie());
        t.getTableHeader().setForeground(tenue());
        t.getTableHeader().setPreferredSize(new Dimension(10, 42));
        return t;
    }

    private JPanel panelLista(JTable tabla) {
        JPanel p = caja(new BorderLayout(0, 13), 18);
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(superficie());
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JPanel herramientas(JTable tabla, String placeholder) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 6));
        p.setOpaque(false);
        JTextField buscar = new JTextField(23);
        buscar.putClientProperty("JTextField.placeholderText", placeholder);
        buscar.putClientProperty("JTextField.showClearButton", true);
        buscar.setPreferredSize(new Dimension(240, 36));
        TableRowSorter<?> sorter = (TableRowSorter<?>) tabla.getRowSorter();
        buscar.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String texto = buscar.getText().strip();
                sorter.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?iu)" + java.util.regex.Pattern.quote(texto)));
            }
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        p.add(buscar);
        return p;
    }

    private Integer idSeleccionado(JTable t) {
        int fila = t.getSelectedRow();
        if (fila < 0) { aviso("Selecciona una fila de la tabla."); return null; }
        return (Integer) t.getModel().getValueAt(t.convertRowIndexToModel(fila), 0);
    }

    private String claveSeleccionada(JTable t) {
        int fila = t.getSelectedRow();
        if (fila < 0) { aviso("Selecciona una fila de la tabla."); return null; }
        return String.valueOf(t.getModel().getValueAt(t.convertRowIndexToModel(fila), 0));
    }

    private JButton boton(String nombre, Runnable accion) {
        JButton b = new JButton(nombre);
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        b.setBorder(new EmptyBorder(10, 16, 10, 16));
        b.setFocusPainted(false);
        b.addActionListener(e -> accion.run());
        return b;
    }

    private JButton botonPrimario(String nombre, Runnable accion) {
        JButton b = boton(nombre, accion);
        b.setBackground(NARANJA);
        b.setForeground(Color.WHITE);
        return b;
    }

    private JButton botonClaro(String nombre, Runnable accion) {
        JButton b = boton(nombre, accion);
        b.setBackground(Color.WHITE);
        b.setForeground(MARINO);
        return b;
    }

    private JButton botonTexto(String nombre, Runnable accion) {
        JButton b = boton(nombre, accion);
        b.setBorder(new EmptyBorder(5, 8, 5, 8));
        b.setContentAreaFilled(false);
        b.setForeground(NARANJA);
        return b;
    }

    private JLabel etiquetaGrande(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        l.setForeground(tinta());
        return l;
    }

    private JTextArea textoInformativo(String texto) {
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setBorder(new EmptyBorder(12, 10, 10, 10));
        return area;
    }

    private boolean dialogoFormulario(String titulo, Object... etiquetasYComponentes) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 8, 7, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < etiquetasYComponentes.length; i += 2) {
            g.gridy = i / 2;
            g.gridx = 0; g.weightx = 0;
            p.add(new JLabel(String.valueOf(etiquetasYComponentes[i])), g);
            g.gridx = 1; g.weightx = 1;
            p.add((Component) etiquetasYComponentes[i + 1], g);
        }
        return JOptionPane.showConfirmDialog(this, p, titulo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                == JOptionPane.OK_OPTION;
    }

    private int entero(String texto) {
        try { return Integer.parseInt(texto.strip()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Ingresa un número entero válido."); }
    }

    private double decimal(String texto) {
        try { return Double.parseDouble(texto.strip().replace(',', '.')); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Ingresa un precio válido, por ejemplo 350.50."); }
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Taller Mecánico", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean confirmar(String pregunta) {
        return JOptionPane.showConfirmDialog(this, pregunta, "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    @FunctionalInterface private interface AccionUI { void ejecutar() throws Exception; }

    private void ejecutar(AccionUI operacion, String exito) {
        try {
            operacion.ejecutar();
            mostrar(seccion);
            // Evitar ventanas emergentes innecesarias después de operaciones pequeñas.
            if (exito.startsWith("Archivo") || exito.startsWith("Respaldo") || exito.startsWith("Cliente registrado")) aviso(exito);
        } catch (Exception ex) {
            Throwable causa = ex;
            while (causa.getCause() != null && causa.getCause() != causa) causa = causa.getCause();
            String detalle = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            if (causa != ex && causa.getMessage() != null) detalle += "\nCausa: " + causa.getMessage();
            JOptionPane.showMessageDialog(this, detalle, "No se pudo completar la operación", JOptionPane.ERROR_MESSAGE);
        }
    }
}
