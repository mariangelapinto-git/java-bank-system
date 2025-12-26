package com.inventario;

import model.Producto;
import service.InventarioService;
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
            System.out.println("4. Eliminar Producto");
            System.out.println("5. Actualizar Producto");
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

                case 4:
                    System.out.print("Ingrese el ID del producto a eliminar: ");
                    int idEliminar = leer.nextInt();
                    servicio.eliminarProducto(idEliminar);
                    break;

                case 5:
                    System.out.print("ID a actualizar: ");
                    int idAct = leer.nextInt();
                    System.out.print("Nuevo precio: ");
                    double np = leer.nextDouble();
                    System.out.print("Nueva cantidad: ");
                    int nc = leer.nextInt();

                    if (servicio.actualizarProducto(idAct, np, nc)) {
                        System.out.println("¡Actualizado!");
                    } else {
                        System.out.println("ID no encontrado.");
                    }
                    break;

                default:
                    System.out.println("Opción no válida.");
            }
        } while (opcion != 3);

        leer.close();
    }
}