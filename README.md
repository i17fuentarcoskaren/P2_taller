# Miniproyecto 2: Gestión de Órdenes de Servicio de un Taller Mecánico

Aplicación de escritorio para registrar clientes, conceptos de cobro (refacciones, mano de obra y diagnósticos) y órdenes de servicio de un taller mecánico, con persistencia en SQLite y reportes de ingresos.

- **Materia:** Tecnologías de Programación
- **Autora:** Karen Julie Fuentes Arcos — Maestría en Sistemas Computacionales
- **Profesor:** Dr. Ulises Juárez Martínez
- **Institución:** TecNM Campus Orizaba
- **Paquete raíz:** `mx.tecnm.orizaba.tp.taller`
- **Variante:** esqueleto (ver [TODO.md](TODO.md) para el avance)

## Tecnologías

| Componente | Versión |
|---|---|
| Java | 21 |
| Maven | 3.x |
| SQLite (`sqlite-jdbc`) | 3.46.1.3 |
| Interfaz gráfica | Swing + FlatLaf 3.4.1 |

## Funcionalidades

- **Catálogo de conceptos** con folios automáticos (`REF-0001`, `MOB-0002`, `DIA-0003`):
  - *Refacción*: precio por pieza, con garantía.
  - *Mano de obra*: 10 % adicional por cada nivel técnico (1 a 3), certificable.
  - *Diagnóstico*: la primera revisión a precio completo y las siguientes al 50 %, con garantía y norma.
- **Clientes** con correo validado y nivel de descuento: Ocasional (0 %), Frecuente (8 %) y Flotilla (15 %).
- **Órdenes de servicio** con varias líneas (concepto + unidades). El total aplica el descuento del cliente.
  - Estados: `ABIERTA → EN_CURSO → CERRADA`, o `ABIERTA/EN_CURSO → CANCELADA`.
  - Solo se editan líneas mientras la orden está abierta.
- **Reportes:** ingresos por tipo de concepto, concepto más demandado, mejores clientes, estadísticas de órdenes cerradas, conteo por estado y exportación a CSV.
- **Monitor en segundo plano:** revisa periódicamente las órdenes. Genera una alerta si pasan de 5 días (`PLAZO_DIAS`) y cancela automáticamente las que siguen sin avanzar.
- **Eventos:** la interfaz se suscribe a altas, cambios de estado, alertas y cancelaciones automáticas.
- **Respaldo:** exporta e inspecciona catálogo, clientes y órdenes en un archivo serializado.

## Estructura del proyecto

```
P2_taller/
├── src/main/java/mx/tecnm/orizaba/tp/taller/
│   ├── modelo/         Concepto (+3 subtipos), Folio, Cliente, OrdenServicio, CatalogoConceptos, Nivel, Estado...
│   ├── funcional/      Regla<T>, Reglas, FabricaElemento<E, A>
│   ├── concurrencia/   TareaPeriodica (plantilla de hilo periódico)
│   ├── eventos/        Evento, TipoEvento, OyenteEvento
│   ├── persistencia/   Repositorio<T, K>, repositorios SQLite, ConexionSQLite, Respaldo
│   ├── servicio/       Taller (fachada) y Taller.MonitorAvance
│   ├── ui/             VentanaPrincipal (Swing)
│   └── app/            App, DemoModelo, PruebasModelo
├── diagramas/          Fuentes (.puml, .dot) e imágenes de los diagramas
├── lib/                JAR de SQLite para compilar sin Maven
├── compilar.sh · probar.sh
├── pom.xml
├── Bitacora.docx
└── TODO.md
```

Dependencias entre capas: `ui` → `servicio` → (`modelo`, `funcional`, `persistencia`, `eventos`, `concurrencia`). La interfaz solo habla con `Taller`.

## Cómo ejecutar

### Con Maven

```bash
mvn -q compile && mvn -q exec:java        # PruebasModelo (pruebas del modelo)
mvn -q exec:java -Pdemo                   # DemoModelo (demostración por consola)
mvn -q exec:java -Dexec.mainClass=mx.tecnm.orizaba.tp.taller.app.App   # Interfaz gráfica
```

### Sin Maven (Linux/macOS)

1. Descarga `sqlite-jdbc-3.46.1.3.jar` (Maven Central: `org.xerial:sqlite-jdbc`) y colócalo en `lib/`.
2. Ejecuta:

```bash
./compilar.sh     # compila a ./out
./probar.sh       # corre PruebasModelo
```

> La interfaz gráfica también requiere `flatlaf-3.4.1.jar` en el classpath si no usas Maven.

La base de datos `taller.db` se crea en el directorio desde el que se ejecuta el programa.

## Diagramas

En la carpeta [`diagramas/`](diagramas/) están los diagramas actualizados:

| Diagrama | Fuente | Imagen |
|---|---|---|
| Clases del modelo (Figura 1) | `modelo.puml`, `modelo.dot` | `modelo.png` |
| Paquetes y dependencias (Figura 2) | `arquitectura.dot` | `arquitectura.png` |

Para regenerar las imágenes:

```bash
dot -Tpng diagramas/modelo.dot -o diagramas/modelo.png
dot -Tpng diagramas/arquitectura.dot -o diagramas/arquitectura.png
```

## Conceptos de programación aplicados

Clases abstractas y selladas, interfaces con métodos `default` y `static`, genéricos acotados, clases internas (miembro, local y anónima), `record` y `enum` con comportamiento, expresiones lambda y referencias a métodos, Streams y Collectors, `switch` con pattern matching, hilos y sincronización (`synchronized`, `volatile`), serialización (`Serializable` y `Externalizable`), JDBC con transacciones y el patrón Repositorio.

## Avance

Estado de los pendientes (`// TODO Exx` en el código): consulta [TODO.md](TODO.md).

## Bitácora

El registro de uso de herramientas de apoyo y consultas está en [Bitacora.docx](Bitacora.docx).
