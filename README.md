# Sistema de Gestión de Biblioteca Escolar

Evaluación Final Transversal – PRY2203 Desarrollo Orientado a Objetos II (DuocUC).
Autor: Rodrigo Gutiérrez Espinoza.

Aplicación de escritorio en **Java 21 + Swing + JDBC + MySQL 8** para administrar libros, categorías,
estudiantes y préstamos de una biblioteca escolar, con ingreso por rol (bibliotecario / estudiante).

## Requisitos
- JDK 21 (Temurin), IntelliJ IDEA
- MySQL 8.x y MySQL Workbench
- MySQL Connector/J 8.4.0 (`com.mysql:mysql-connector-j:8.4.0`)

## Puesta en marcha
1. En Workbench ejecutar **una sola vez**, en este orden:
   1. `sql/Script_crea_tablas_biblioteca.sql`
   2. `sql/Script_poblado_tablas_biblioteca.sql`

   (Para reiniciar los datos: `DROP DATABASE biblioteca;` y volver a ejecutar ambos.)
2. Abrir la carpeta del proyecto en IntelliJ. Project SDK 21 y Language level 21.
   Si la librería del conector no aparece sola: *File > Project Structure > Libraries > + > From Maven…* →
   `com.mysql:mysql-connector-j:8.4.0` → OK (agregarla al módulo).
3. *Run > Edit Configurations* → aplicación `app.Main`, y en **Environment variables**:
   `MYSQL_PASSWORD=<su clave de MySQL>` (opcionalmente `MYSQL_USER`, por defecto `root`).
   Las credenciales no están escritas en el código.
4. Ejecutar `app.Main`.

## Usuarios de prueba (contraseña `clave123`)
| Rol | Correo |
|---|---|
| Bibliotecario | antonia@correo.cl |
| Estudiante | carlos@correo.cl (también maria@, ignacio@, laura@, javier@, fer@, pedro@, valen@, tomas@ correo.cl) |

## Módulos por rol
- **Bibliotecario:** Gestión de libros (CRUD + búsqueda), Categorías (CRUD), Gestión de estudiantes (CRUD, crea también su acceso),
  Préstamos y devoluciones, Reportes (libros más prestados, historial por estudiante, libros en préstamo).
- **Estudiante:** Consulta de libros, solicitud de préstamos a su nombre y su propio historial.

## Arquitectura
```
src/
  app/          Main (punto de entrada)
  config/       DatabaseConnection  (Singleton, conexión JDBC)
  modelo/       Persona (abstracta) > Usuario (abstracta) > UsuarioBibliotecario / UsuarioEstudiante,
                Estudiante, Libro, Categoria, Prestamo, enums Rol y Modulo, resultados
  dao/          CrudDAO<T> (interfaz) + CategoriaDAO, LibroDAO, EstudianteDAO, UsuarioDAO, PrestamoDAO, ReporteDAO
  servicio/     PrestamoService (lógica de préstamos sincronizada)
  controlador/  Controladores MVC y manejo de errores SQL
  vista/        LoginFrame, MainFrame (navegación + CardLayout) y paneles Swing
sql/            Scripts oficiales de creación y poblado
```
Patrones: MVC, DAO, Singleton. POO: herencia, clases abstractas, interfaz genérica y polimorfismo
(`getModulosPermitidos()` define el menú de cada rol).

## Concurrencia
- Los préstamos y devoluciones se ejecutan en hilos de segundo plano (`ExecutorService`); la interfaz no se congela
  (botones deshabilitados y mensaje de estado mientras se procesa).
- `PrestamoService.prestarLibro/devolverLibro` son `synchronized` y el descuento de stock es atómico
  (`UPDATE ... SET stock = stock - 1 WHERE id = ? AND stock > 0`), por lo que el stock nunca queda negativo.
- El botón **Simular préstamos simultáneos** (bibliotecario) lanza un préstamo del mismo libro por cada estudiante a la vez.
  Con stock 2 y 10 estudiantes: 2 préstamos registrados y 8 rechazados. Registra préstamos reales en la BD.
- Se agregó un retardo simulado de 300 ms en el préstamo solo para evidenciar el trabajo en segundo plano.

## Notas
- El plazo de préstamo es de 7 días (`fecha_devolucion` = fecha de vencimiento). Como el esquema oficial no tiene columna de
  fecha real de devolución, el atraso se calcula al devolver y se muestra en el mensaje y en la columna Estado.
- Las contraseñas están en texto plano porque así las define el script oficial de poblado.
- Se maneja y muestra con mensajes claros: credenciales inválidas, RUT/ISBN duplicado, stock agotado, libro ya prestado al
  mismo estudiante, eliminación bloqueada por llaves foráneas y fallos de conexión.
