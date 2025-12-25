package com.inventario.service;

import model.Producto;
import java.util.ArrayList;
import java.util.List;

public class InventarioService {
    // Nuestra "base de datos" temporal
    private List<Producto> productos = new ArrayList<>();

    public void agregarProducto(Producto p) {
        productos.add(p);
        System.out.println("Producto agregado con éxito.");
    }

    public void listarProductos() {
        if (productos.isEmpty()) {
            System.out.println("El inventario está vacío.");
        } else {
            for (Producto p : productos) {
                System.out.println(p);
            }
        }
    }
}
