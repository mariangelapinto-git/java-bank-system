package com.inventario.util;

import model.Producto;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ArchivoUtil {
    private static final String NOMBRE_ARCHIVO = "inventario.txt";

    // Guarda la lista de productos en el archivo
    public static void guardarArchivo(List<Producto> productos) {
        try (PrintWriter salida = new PrintWriter(new FileWriter(NOMBRE_ARCHIVO))) {
            for (Producto p : productos) {
                salida.println(p.getId() + "," + p.getNombre() + "," + p.getPrecio() + "," + p.getCantidad());
            }
        } catch (IOException e) {
            System.out.println("Error al guardar el archivo: " + e.getMessage());
        }
    }

    // Lee el archivo y carga los productos en una lista
    public static List<Producto> cargarArchivo() {
        List<Producto> productos = new ArrayList<>();
        File archivo = new File(NOMBRE_ARCHIVO);

        if (!archivo.exists()) return productos;

        try (BufferedReader entrada = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                String[] datos = linea.split(",");
                int id = Integer.parseInt(datos[0]);
                String nombre = datos[1];
                double precio = Double.parseDouble(datos[2]);
                int cantidad = Integer.parseInt(datos[3]);
                productos.add(new Producto(id, nombre, precio, cantidad));
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error al cargar el archivo: " + e.getMessage());
        }
        return productos;
    }
}
