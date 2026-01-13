# 📁 Sistema de Gestión Bancaria e Inventario (Java)

Este es un sistema robusto desarrollado en Java que integra operaciones bancarias avanzadas, gestión de inventarios y módulos de seguridad. El proyecto está diseñado bajo principios de programación orientada a objetos (**POO**) 

---

## 🚀 Características Principales

* **Gestión de Cuentas:** Soporte para cuentas de Ahorro (con intereses), Corrientes (con sobregiros) y Nómina.
* **Seguridad Avanzada:** Autenticación de usuarios con encriptación de contraseñas mediante **BCrypt**.
* **Módulo de Préstamos:** Reglas de negocio basadas en el promedio de saldo.
* **Auditoría y Logs:** Registro detallado de acciones (`system_logs`) para rastrear movimientos y prevenir fraudes.
* **Reportes:** Generación de estados de cuenta y reportes en formato **PDF**.

---

## 📋 Requisitos del Sistema

Para ejecutar este proyecto, asegúrate de tener instalado lo siguiente:

* **Java JDK 17 o superior:** [Descargar aquí](https://www.oracle.com/java/technologies/downloads/)
* **IDE de preferencia:** IntelliJ IDEA, Eclipse o VS Code (con Java Extension Pack).
* **Gestor de Base de Datos:** MySQL o PostgreSQL (según tu configuración).
* **Maven/Gradle:** (Opcional, si estás gestionando dependencias para BCrypt o PDF Generation).

---

## 🛠️ Instalación y Configuración

Sigue estos pasos para tener el sistema funcionando en tu máquina local:

### 1. Clonar el repositorio
```bash
git clone [https://github.com/tu-usuario/sistema-inventario-java.git](https://github.com/tu-usuario/sistema-inventario-java.git)
cd sistema-inventario-java
### 2. Configuración de la Base de Datos
1. Crea una base de datos llamada `banco_db`.
2. Ejecuta el script SQL incluido en `/src/main/resources/database_schema.sql` para crear las tablas (`usuarios`, `cuentas`, `system_logs`).

### 3. Configuración de Variables de Entorno
Asegúrate de configurar los datos de conexión en el archivo de propiedades o dentro de tu clase `DatabaseConnection`:

* **DB_URL:** `jdbc:mysql://localhost:3306/banco_db`
* **DB_USER:** `tu_usuario`
* **DB_PASS:** `tu_contraseña`

---

## 🖥️ Ejecución del Proyecto

### Desde la Terminal:
Compila y ejecuta la clase principal:

```bash
javac -d bin src/main/java/com/proyecto/Main.java
java -cp bin com.proyecto.Main
