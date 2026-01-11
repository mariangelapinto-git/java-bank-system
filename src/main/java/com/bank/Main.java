package com.bank;
import com.bank.exception.BankException;
import com.bank.model.*;
import com.bank.service.BankService;
import com.bank.service.ReporteService;
import com.bank.view.VentanaLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import java.awt.EventQueue;
import java.util.Scanner;

@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    private BankService servicio;

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private VentanaLogin ventanaLogin;

    private Usuario usuarioLogueado;

    public static void main(String[] args) {
        new SpringApplicationBuilder(Main.class)
                .headless(false) // Desactivado para permitir Java Swing
                .run(args);
    }

    @Override
    public void run(String... args) {
        // Inicialización del usuario (Simulación para auditoría y login)
        this.usuarioLogueado = new Usuario();
        this.usuarioLogueado.setId(1L);
        this.usuarioLogueado.setUsername("Admin_User");
        this.usuarioLogueado.setRol("ADMIN");

        // 1. Lanzamos la interfaz gráfica (Pendiente de lista de GUI)
        EventQueue.invokeLater(() -> {
            ventanaLogin.setVisible(true);
            ventanaLogin.setLocationRelativeTo(null);
        });

        // 2. Ejecución del Menú de Consola
        Scanner leer = new Scanner(System.in);
        int opcion = 0;

        System.out.println("\n========================================");
        System.out.println("   BIENVENIDO AL SISTEMA BANCARIO");
        System.out.println("========================================");
        System.out.println("Sesión iniciada como: " + usuarioLogueado.getUsername());

        do {
            mostrarMenu();
            opcion = leerEntero(leer, "\nElige una opción: ");
            if (leer.hasNextLine()) leer.nextLine();

            try {
                switch (opcion) {
                    case 1 -> crearCuentaMenu(leer);
                    case 2 -> servicio.obtenerTodasLasCuentas().forEach(c ->
                            System.out.println("ID: " + c.getId() + " | Nº: " + c.getNumeroCuenta() + " | Saldo: $" + c.getSaldo() + " | Estado: " + c.getEstado()));
                    case 3 -> {
                        Long idCerrar = leerLong(leer, "ID de cuenta a cerrar: ");
                        servicio.cambiarEstadoCuenta(idCerrar, "CERRADO");
                    }
                    case 4 -> {
                        Long idCons = leerLong(leer, "ID de cuenta: ");
                        servicio.consultarCuenta(idCons).ifPresentOrElse(
                                c -> System.out.println("Saldo actual: $" + c.getSaldo()),
                                () -> System.out.println("Cuenta no encontrada.")
                        );
                    }
                    case 5 -> {
                        Long idDep = leerLong(leer, "ID de cuenta: ");
                        double montoDep = leerDouble(leer, "Monto a depositar: ");
                        servicio.depositar(idDep, montoDep);
                    }
                    case 6 -> {
                        Long idRet = leerLong(leer, "ID de cuenta: ");
                        double montoRet = leerDouble(leer, "Monto a retirar: ");
                        // Pasamos el ID del usuario para el registro de logs [cite: 2025-12-27]
                        servicio.retirar(idRet, montoRet, usuarioLogueado.getId());
                    }
                    case 7 -> {
                        Long idOri = leerLong(leer, "ID cuenta origen: ");
                        System.out.print("Número cuenta destino: ");
                        String cuentaDest = leer.next();
                        double montoTrans = leerDouble(leer, "Monto: ");
                        servicio.transferir(idOri, cuentaDest, montoTrans, usuarioLogueado.getId());
                    }
                    case 10 -> {
                        Long idPdf = leerLong(leer, "ID de cuenta para el PDF: ");
                        reporteService.generarEstadoCuenta(idPdf); // Funcionalidad de reportes reales
                    }
                    case 11 -> {
                        if ("ADMIN".equals(usuarioLogueado.getRol())) {
                            servicio.ejecutarInteresesBatch(); // Sistema de Intereses Batch
                            System.out.println("Proceso completado.");
                        } else {
                            System.out.println("No tienes permisos.");
                        }
                    }
                    case 13 -> {
                        Long idPre = leerLong(leer, "ID de cuenta: ");
                        System.out.println(servicio.evaluarPrestamo(idPre)); // Reglas de Préstamos
                    }
                    case 14 -> System.out.println("Saliendo...");
                    default -> System.out.println("Opción inválida.");
                }
            } catch (BankException e) {
                System.err.println("\nALERTA BANCARIA: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("\nERROR: " + e.getMessage());
            }

        } while (opcion != 14);
    }

    private void mostrarMenu() {
        System.out.println("\n--- MENÚ PRINCIPAL ---");
        System.out.println("1. Abrir Cuenta | 2. Listar Cuentas | 3. Cerrar Cuenta");
        System.out.println("4. Saldo        | 5. Depósito       | 6. Retiro");
        System.out.println("7. Transferir   | 10. Generar PDF   | 11. Cierre Mes");
        System.out.println("13. Préstamos   | 14. Salir");
    }

    private void crearCuentaMenu(Scanner leer) {
        System.out.print("Número de Cuenta: ");
        String num = leer.nextLine();
        System.out.print("Titular: ");
        String titular = leer.nextLine();
        System.out.print("Cédula: ");
        String cedula = leer.nextLine();
        double saldo = leerDouble(leer, "Saldo Inicial: ");

        System.out.println("Tipo: 1.Ahorro, 2.Corriente, 3.Nomina");
        int tipo = leerEntero(leer, "Opción: ");

        Cuenta c = switch (tipo) {
            case 1 -> new CuentaAhorro(num, titular, cedula, "S/D", "S/D", saldo);
            case 2 -> new CuentaCorriente(num, titular, cedula, "S/D", "S/D", saldo);
            default -> new CuentaNomina(num, titular, cedula, "S/D", "S/D", saldo);
        };

        servicio.crearCuenta(c, usuarioLogueado.getId());
        System.out.println("Cuenta creada exitosamente.");
    }

    private int leerEntero(Scanner leer, String msg) { System.out.print(msg); return leer.nextInt(); }
    private Long leerLong(Scanner leer, String msg) { System.out.print(msg); return leer.nextLong(); }
    private double leerDouble(Scanner leer, String msg) { System.out.print(msg); return leer.nextDouble(); }
}