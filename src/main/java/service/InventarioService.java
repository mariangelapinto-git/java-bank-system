package service;

import model.Producto;
import java.util.ArrayList;
import com.inventario.util.ArchivoUtil;
import java.util.List;

public class InventarioService {
    private List<Producto> productos;

    public InventarioService() {
        // Al iniciar, cargamos los datos guardados [cite: 12]
        this.productos = ArchivoUtil.cargarArchivo();
    }

    // Método para ELIMINAR
    public boolean eliminarProducto(int id) {
        boolean eliminado = productos.removeIf(p -> p.getId() == id);
        if (eliminado) {
            // Guardamos la lista actualizada en el archivo para que el cambio sea permanente
            ArchivoUtil.guardarArchivo(productos);
        }
        return eliminado;
    }

    // Método para ACTUALIZAR
    public boolean actualizarProducto(int id, double nuevoPrecio, int nuevaCantidad) {
        for (Producto p : productos) {
            if (p.getId() == id) {
                p.setPrecio(nuevoPrecio);
                p.setCantidad(nuevaCantidad);
                // Guardamos los cambios en el archivo
                ArchivoUtil.guardarArchivo(productos);
                return true;
            }
        }
        return false;
    }

    public void agregarProducto(Producto p) {
        productos.add(p);
        ArchivoUtil.guardarArchivo(productos); // Guardar cambios
        System.out.println("Producto agregado y guardado.");
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
