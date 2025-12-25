package com.inventario;

import model.Producto;
import com.inventario.service.InventarioService;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        // Instanciamos el servicio y el scanner
        InventarioService servicio = new InventarioService();
        Scanner leer = new Scanner(System.in);
        int opcion = 0;

        System.out.println("--- BIENVENIDO/A AL SISTEMA DE INVENTARIO ---");

        do {
            System.out.println("\n1. Agregar Producto");
            System.out.println("2. Ver Inventario");
            System.out.println("3. Salir");
            System.out.print("Elige una opción: ");
            opcion = leer.nextInt();

            leer.nextLine(); // Limpiar el buffer después de leer un número

            switch (opcion) {
                case 1:
                    System.out.print("Ingrese ID: ");
                    int id = leer.nextInt();
                    leer.nextLine(); // Limpiar buffer

                    System.out.print("Nombre del producto: ");
                    String nombre = leer.nextLine();

                    System.out.print("Precio: ");
                    double precio = leer.nextDouble();

                    System.out.print("Cantidad en stock: ");
                    int cantidad = leer.nextInt();

                    // Creamos el objeto y lo enviamos al servicio
                    Producto nuevo = new Producto(id, nombre, precio, cantidad);
                    servicio.agregarProducto(nuevo);
                    break;

                case 2:
                    System.out.println("\n--- Lista de Productos ---");
                    servicio.listarProductos();
                    break;

                case 3:
                    System.out.println("Saliendo del sistema...");
                    break;

                default:
                    System.out.println("Opción no válida.");
            }
        } while (opcion != 3);

        leer.close();
    }
}