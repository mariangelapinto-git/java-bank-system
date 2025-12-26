package com.bank;

import model.Cuenta;
import service.BankService;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        BankService servicio = new BankService();
        Scanner leer = new Scanner(System.in);
        int opcion = 0;

        System.out.println("--- BIENVENIDO AL SISTEMA BANCARIO ---");

        do {
            System.out.println("\n1. Abrir Cuenta Nueva");
            System.out.println("2. Ver Cuentas (Listar)");
            System.out.println("3. Cerrar Cuenta (Eliminar)");
            System.out.println("4. Consulta de Saldo");
            System.out.println("5. Deposito Dinero");
            System.out.println("6. Retiro de Dinero");
            System.out.println("7. Transferencia");
            System.out.println("8. Ver extracto bancario");
            System.out.println("9. Salir");


            // Usamos el lector seguro para la opción del menú
            opcion = leerEntero(leer, "\nElige una opción: ");

            switch (opcion) {
                case 1:
                    int id = leerEntero(leer, "Ingrese ID único: ");
                    leer.nextLine(); // Limpiar buffer después de un número

                    System.out.print("Número de Cuenta: ");
                    String numCuenta = leer.nextLine();

                    System.out.print("Nombre del Titular: ");
                    String titular = leer.nextLine();

                    double saldoInicial = leerDouble(leer, "Depósito Inicial: ");

                    Cuenta nuevaCuenta = new Cuenta(id, numCuenta, titular, saldoInicial);
                    servicio.crearCuenta(nuevaCuenta);
                    break;

                case 2:
                    System.out.println("\n--- Resumen de Cuentas ---");
                    servicio.listarCuentas();
                    break;

                case 3:
                    int idEliminar = leerEntero(leer, "Ingrese el ID de la cuenta a eliminar: ");
                    servicio.cerrarCuenta(idEliminar);
                    break;

                case 4:
                    int idConsulta = leerEntero(leer, "Ingrese el ID de la cuenta para consultar saldo: ");
                    servicio.consultarSaldo(idConsulta);
                    break;

                case 5:
                    int idDep = leerEntero(leer, "Ingrese ID de la cuenta para depositar: ");
                    double montoDep = leerDouble(leer, "Monto a depositar: ");
                    servicio.depositar(idDep, montoDep);
                    break;

                case 6:
                    int idRet = leerEntero(leer, "Ingrese ID de la cuenta para retirar: ");
                    double montoRet = leerDouble(leer, "Monto a retirar: ");
                    servicio.retirar(idRet, montoRet);
                    break;

                case 7:
                    System.out.println("\n--- MÓDULO DE TRANSFERENCIA ---");
                    int idOri = leerEntero(leer, "ID Cuenta Origen: ");
                    int idDes = leerEntero(leer, "ID Cuenta Destino: ");
                    double montoTrans = leerDouble(leer, "Monto a transferir: ");
                    servicio.transferir(idOri, idDes, montoTrans);
                    break;

                case 8:
                    int idEx = leerEntero(leer, "Ingrese el ID de la cuenta para ver su historial: ");
                    servicio.verExtracto(idEx);
                    break;

                case 9:
                    System.out.println("Cerrando sesión segura...");
                    break;

                default:
                    System.out.println("Opción no válida.");
            }

        } while (opcion != 3);

        leer.close();
    }

    // --- MÉTODOS DE VALIDACIÓN (ROBUSTEZ) ---

    private static int leerEntero(Scanner leer, String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                String entrada = leer.next();
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Error: Debes ingresar un número entero (sin letras o simbolos).");
            }
        }
    }

    private static double leerDouble(Scanner leer, String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                String entrada = leer.next();
                return Double.parseDouble(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Error: Monto inválido. Usa números (ej: 1000.50).");
            }
        }
    }
}