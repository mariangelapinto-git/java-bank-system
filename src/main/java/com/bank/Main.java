package com.bank;

import service.BankService;
import java.util.Scanner;
import model.*;

public class Main {
    public static void main(String[] args) {

        BankService servicio = new BankService();
        Scanner leer = new Scanner(System.in);
        int opcion = 0;

        System.out.println("--- BIENVENIDO AL SISTEMA BANCARIO ---");

        // --- SISTEMA DE LOGIN ---
        Usuario usuarioLogueado = null;
        while (usuarioLogueado == null) {
            System.out.println("\n--- INICIO DE SESIÓN ---");
            System.out.print("Usuario: ");
            String user = leer.next();
            System.out.print("Contraseña: ");
            String pass = leer.next();
            usuarioLogueado = servicio.login(user, pass);
        }

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
            System.out.println("10. Salir");

            opcion = leerEntero(leer, "\nElige una opción: ");

            switch (opcion) {
                case 1:
                    int id = leerEntero(leer, "Ingrese ID único: ");
                    leer.nextLine();
                    System.out.print("Número de Cuenta: ");
                    String numCuenta = leer.nextLine();
                    System.out.print("Nombre del Titular: ");
                    String titular = leer.nextLine();
                    double saldoInicial = leerDouble(leer, "Depósito Inicial: ");
                    Cuenta nuevaCuenta = new Cuenta(id, numCuenta, titular, saldoInicial);
                    servicio.crearCuenta(nuevaCuenta);
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

                case 6:
                    int idRet = leerEntero(leer, "Ingrese ID para retirar: ");
                    double montoRet = leerDouble(leer, "Monto: ");
                    servicio.retirar(idRet, montoRet);
                    break;

                case 7:
                    int idOri = leerEntero(leer, "ID Origen: ");
                    int idDes = leerEntero(leer, "ID Destino: ");
                    double montoTrans = leerDouble(leer, "Monto: ");
                    servicio.transferir(idOri, idDes, montoTrans);
                    break;

                case 8:
                    int idEx = leerEntero(leer, "ID para historial: ");
                    servicio.verExtracto(idEx);
                    break;

                case 9:
                    // --- PROTECCIÓN DE ADMIN ---
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
                    System.out.println("¡Gracias por usar JavaBank, " + usuarioLogueado.getUsername() + "!");
                    break;

                default:
                    System.out.println("Opción no válida.");
            }

        } while (opcion != 10);

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
