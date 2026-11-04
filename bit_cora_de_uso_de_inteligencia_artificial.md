# Bitácora de Uso de Inteligencia Artificial (BITACORA_IA.md)

**Proyecto:** Sistema de Gestión de Taller Mecánico (`mx.tecnm.orizaba.tp.taller`)  
**Autor:** Karen Julie Fuentes Arcos  
**Herramienta IA Utilizada:** ChatGPT / Gemini  

---

## Registro de Consultas e Interacciones

| # | Fecha | Herramienta / Modelo | Propósito | Prompt Resumido / Consulta | Qué se aceptó / rechazó y por qué |
| :-: | :---: | :---: | :--- | :--- | :--- |
| **1** | 2026-10-07 | ChatGPT | Configuración del Entorno | ¿Cómo conectar GitHub con IntelliJ IDEA para el control de versiones? | **Aceptado:** Se ejecutó el procedimiento para vincular la cuenta de GitHub mediante token Personal Access Token (PAT) e integración nativa en IntelliJ. |
| **2** | 2026-10-10 | ChatGPT | Configuración de Herramientas | Comandos e instrucciones para la instalación y configuración de Apache Maven en el sistema. | **Aceptado:** Se agregaron las variables de entorno `M2_HOME` y `PATH`, verificando la instalación correcta con `mvn -version`. |
| **3** | 2026-10-12 | ChatGPT | Control de Versiones | Comandos de Git para inicializar, vincular al repositorio remoto y subir el proyecto a GitHub. | **Aceptado:** Se aplicaron los comandos `git init`, `git add`, `git commit` y `git push -u origin main` para publicar la base del repositorio. |
| **4** | 2026-10-15 | ChatGPT | Arquitectura / Persistencia | Guía de implementación del modelo de datos para el taller mecánico. | **Aceptado parcialmente:** Se adaptó la estructura propuesta a las entidades del dominio (`Cliente`, `Concepto`, `OrdenServicio`) respetando el uso de `records` y clases selladas. |
| **5** | 2026-10-18 | ChatGPT | Concurrencia | Investigación sobre el manejo de Hilos (Threads) e hilos en segundo plano en Java. | **Aceptado:** Se utilizó el concepto para estructurar la clase `TareaPeriodica` y el `MonitorAvance` del taller sin bloquear el hilo principal de la interfaz gráfica. |
| **6** | 2026-10-20 | ChatGPT | Patrones de Diseño | Investigación sobre la estructura y aplicación del patrón de diseño *Template Method*. | **Aceptado:** Se implementó como base para la abstracción de tareas periódicas concurrentes en la aplicación. |
| **7** | 2026-10-22 | ChatGPT | Interfaz Gráfica (UI) | Investigación sobre la instalación e integración de Swing con el Look & Feel FlatLaf. | **Aceptado:** Se agregaron las dependencias de FlatLaf en el `pom.xml` (`FlatLightLaf` y `FlatDarkLaf`) para dar soporte al tema industrial claro/oscuro. |
| **8** | 2026-10-24 | ChatGPT | Depuración / Compilación | Solución a errores de compilación y ejecución desde PowerShell con dependencias de FlatLaf. | **Aceptado:** Se corrigió la inclusión del *classpath* al compilar manualmente desde PowerShell y se configuró Maven para gestionar la empaquetación de dependencias. |
| **9** | 2026-10-26 | ChatGPT / Gemini | Persistencia (BD) | Consulta para el diseño de tablas SQLite, sintaxis *Upsert* (`ON CONFLICT`) y patrón JDBC Repositorio. | **Aceptado:** Se implementaron los repositorios JDBC (`RepositorioClienteSQLite`, `RepositorioConceptoSQLite`, `RepositorioOrdenServicioSQLite`) con transacciones automáticas `enTransaccion`. |
| **10** | 2026-10-31 | Gemini | Documentación | Generación y estandarización de comentarios Javadoc reglamentarios (`@param`, `@return`, `@throws`, `@author`). | **Aceptado:** Se incluyeron los comentarios de documentación en el estándar formal de Java en todos los paquetes del sistema y se compiló exitosamente con `mvn javadoc:javadoc`. |