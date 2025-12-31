package com.bank;
import com.bank.exception.BankException;
import service.BankService;
import java.util.Scanner;
import model.*;

public class Main {
    public static void main(String[] args) {

        BankService servicio = new BankService();
        Scanner leer = new Scanner(System.in);
        int usuarioLogueadoId = 3;
        int opcion = 0;

        System.out.println("--- BIENVENIDO AL SISTEMA BANCARIO ---");

        // ==================================SISTEMA DEL LOGIN=====================

        Usuario usuarioLogueado = null;

        // Verificar si la tabla está vacía para crear el primer administrador

        if (servicio.obtenerTotalUsuarios() == 0) {
            System.out.println("Configuración inicial: Creando usuario administrador...");
            servicio.registrarUsuario("admin", "1234", "ADMIN");
            System.out.println("Usuario 'admin' con clave '1234' creado exitosamente.");
        }

        while (usuarioLogueado == null) {
            System.out.println("\n--- ACCESO AL SISTEMA BANCARIO ---");
            System.out.print("Usuario: ");
            String user = leer.nextLine();
            System.out.print("Contraseña: ");
            String pass = leer.nextLine();

            usuarioLogueado = servicio.login(user, pass);

            if (usuarioLogueado == null) {
                System.out.println("Error: Usuario o contraseña incorrectos.");
            }
        }
        System.out.println("Acceso concedido como: " + usuarioLogueado.getRol());

        System.out.println("Bienvenid@, " + usuarioLogueado.getUsername() + " [" + usuarioLogueado.getRol() + "]");
        do {
            System.out.println("\n1. Abrir Cuenta Nueva");
            System.out.println("2. Ver Cuentas (Listar)");
            System.out.println("3. Cerrar Cuenta (Soft Delete)");
            System.out.println("4. Consulta de Saldo");
            System.out.println("5. Deposito Dinero");
            System.out.println("6. Retiro de Dinero");
            System.out.println("7. Transferencia");
            System.out.println("8. Ver extracto bancario");
            System.out.println("9. Panel de Administración (Auditoría/Reactivar)");
            System.out.println("10. Generar PDF");
            System.out.println("11. Ejecutar Cierre de Mes (Intereses)");
            System.out.println("12. Disponibilidad y limite de la cuenta");
            System.out.println("13. Evaluar prestamos");
            System.out.println("14. Salir");

            opcion = leerEntero(leer, "\nElige una opción: ");

            switch (opcion) {
                case 1:
                    // ABRIR CUENTA NUEVA
                    int id = leerEntero(leer, "Ingrese ID único: ");

                    // CORRECCIÓN AQUÍ: Usamos nextLine() para limpiar el "Enter" del buffer
                    leer.nextLine();

                    System.out.print("Número de Cuenta: ");
                    String numCuenta = leer.nextLine();

                    System.out.print("Nombre del Titular: ");
                    String titular = leer.nextLine();

                    System.out.print("Cédula/RIF: ");
                    String cedula = leer.nextLine();

                    System.out.print("Dirección: ");
                    String direccion = leer.nextLine();

                    System.out.print("Teléfono: ");
                    String telefono = leer.nextLine();

                    double saldoInicial = leerDouble(leer, "Depósito Inicial: ");

                    // Otra limpieza de buffer después de leer un número (leerDouble)
                    leer.nextLine();

                    System.out.println("\nSeleccione Tipo de Cuenta:");
                    System.out.println("1. Ahorro (Genera Intereses)");
                    System.out.println("2. Corriente (Permite Sobregiro)");
                    System.out.println("3. Nómina (Sin Comisiones)");
                    int tipoC = leerEntero(leer, "Opción: ");
                    leer.nextLine(); // Limpiar buffer otra vez

                    Cuenta nuevaCuenta = null;

                    if (tipoC == 1) {
                        nuevaCuenta = new CuentaAhorro(id, numCuenta, titular, cedula, direccion, telefono, saldoInicial);
                    } else if (tipoC == 2) {
                        nuevaCuenta = new CuentaCorriente(id, numCuenta, titular, cedula, direccion, telefono, saldoInicial);
                    } else {
                        nuevaCuenta = new CuentaNomina(id, numCuenta, titular, cedula, direccion, telefono, saldoInicial);
                    }

                    if (nuevaCuenta != null) {
                        servicio.crearCuenta(nuevaCuenta, usuarioLogueado.getId());                    }
                    break;

                case 2:
                    servicio.listarCuentas();
                    break;

                case 3:
                    int idEliminar = leerEntero(leer, "Ingrese el ID de la cuenta a cerrar: ");
                    servicio.cerrarCuenta(idEliminar);
                    break;

                case 4:
                    int idConsulta = leerEntero(leer, "Ingrese ID para consultar saldo: ");
                    servicio.consultarSaldo(idConsulta);
                    break;

                case 5:
                    int idDep = leerEntero(leer, "Ingrese ID para depositar: ");
                    double montoDep = leerDouble(leer, "Monto: ");
                    servicio.depositar(idDep, montoDep);
                    break;

                case 6: // RETIROS
                    int idRet = leerEntero(leer, "Ingrese ID de cuenta: ");
                    double montoRet = leerDouble(leer, "Monto a retirar: ");

                    try {
                        // 1. Validamos reglas: Si algo está mal, lanzará una BankException y saltará al catch
                        // IMPORTANTE: Asegúrate de pasar los 3 parámetros que definimos antes
                        servicio.validarReglasDeRetiro(idRet, montoRet, usuarioLogueadoId);

                        // 2. Si llegó aquí es porque las reglas pasaron, ahora procedemos al retiro
                        servicio.retirar(idRet, montoRet, usuarioLogueadoId);

                        System.out.println("¡Retiro procesado con éxito!");

                    } catch (BankException e) {
                        // 3. Aquí se mostrarán todos los errores (Saldo mínimo, límite diario, saldo insuficiente)
                        System.err.println("ALERTA: " + e.getMessage());
                    }
                    break;

                case 7: // TRANSFERENCIAS
                    int idOri = leerEntero(leer, "ID de su cuenta (Origen): ");
                    leer.nextLine(); // Limpiar buffer
                    System.out.print("Ingrese el Número de Cuenta Destino (20 dígitos): ");
                    String numCuentaDes = leer.nextLine();
                    double montoTrans = leerDouble(leer, "Monto a enviar: ");

                    try {
                        // 4. Llamamos al método con el ID del usuario logueado
                        servicio.transferir(idOri, numCuentaDes, montoTrans, usuarioLogueadoId);
                        System.out.println("Transferencia realizada con éxito.");
                    } catch (BankException e) {
                        // 5. Capturamos el error y el usuario verá el motivo exacto
                        System.err.println("ERROR EN TRANSFERENCIA: " + e.getMessage());
                    }
                    break;

                case 8:
                    int idEx = leerEntero(leer, "ID para historial: ");
                    servicio.verExtracto(idEx);
                    break;

                case 9:
                    if (!usuarioLogueado.getRol().equals("ADMIN")) {
                        System.out.println("Acceso denegado: Se requieren privilegios de ADMIN.");
                        break;
                    }

                    System.out.println("\n--- PANEL DE CONTROL ---");
                    System.out.println("1. Ver todas las cuentas (Auditoría)");
                    System.out.println("2. Reactivar una cuenta");
                    int subOpcion = leerEntero(leer, "Opción: ");
                    if (subOpcion == 1) servicio.listarTodasLasCuentas();
                    else if (subOpcion == 2) {
                        int idRe = leerEntero(leer, "ID a reactivar: ");
                        servicio.reactivarCuenta(idRe);
                    }
                    break;

                case 10:
                    idEx = leerEntero(leer, "Ingrese el ID de la cuenta para generar PDF: ");
                    servicio.generarReportePDF(idEx); // Cambiamos el método antiguo por el nuevo
                    break;

                case 11:
                    if (usuarioLogueado.getRol().equals("ADMIN")) {
                        servicio.ejecutarInteresesBatch();
                    } else {
                        System.out.println("Acceso denegado: Solo el Administrador puede cerrar el mes.");
                    }
                    break;

                case 12:
                    int idInfo = leerEntero(leer, "Ingrese el ID de su cuenta: ");
                    servicio.mostrarDetallesDeCuenta(idInfo);
                    break;

                case 13:
                    int idPre = leerEntero(leer, "Ingrese su ID para evaluar préstamo: ");
                    servicio.evaluarYOfrecerPrestamo(idPre);
                    break;
                case 14:
                    System.out.println("¡Gracias por usar JavaBank, " + usuarioLogueado.getUsername() + "!");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }

        } while (opcion != 14);

        leer.close();
    }

    private static int leerEntero(Scanner leer, String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return Integer.parseInt(leer.next());
            } catch (NumberFormatException e) {
                System.out.println("Error: Ingresa un número entero.");
            }
        }
    }

    private static double leerDouble(Scanner leer, String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return Double.parseDouble(leer.next());
            } catch (NumberFormatException e) {
                System.out.println("Error: Monto inválido.");
            }
        }
    }
}
