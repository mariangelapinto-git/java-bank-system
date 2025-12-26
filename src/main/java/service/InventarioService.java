package service;

import com.inventario.util.ConexionBD;
import model.Producto;
import java.sql.*;

public class InventarioService {

    // 1. LISTAR
    public void listarProductos() {
        String sql = "SELECT * FROM productos";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- INVENTARIO ACTUAL ---");
            System.out.printf("%-5s | %-20s | %-10s | %-10s%n", "ID", "Nombre", "Precio", "Stock");
            System.out.println("----------------------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");

                // CAMBIA "stock" POR "cantidad"
                int stock = rs.getInt("cantidad");

                System.out.printf("%-5d | %-20s | %-10.2f | %-10d%n", id, nombre, precio, stock);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar productos: " + e.getMessage());
        }
    }

    // 2. AGREGAR
    public void agregarProducto(Producto p) {
        String sql = "INSERT INTO productos (id, nombre, precio, cantidad) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, p.getId());
            ps.setString(2, p.getNombre());
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getCantidad());

            ps.executeUpdate();
            System.out.println("Producto guardado en MariaDB.");
        } catch (SQLException e) {
            System.out.println("Error al agregar: " + e.getMessage());
        }
    }

    // 3. ELIMINAR
    public void eliminarProducto(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("Producto con ID " + id + " eliminado correctamente.");
            } else {
                System.out.println("No se encontró ningún producto con el ID " + id);
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar producto: " + e.getMessage());
        }
    }

    // 4. ACTUALIZAR
    public boolean actualizarProducto(int id, double nuevoPrecio, int nuevaCantidad) {

        //Uso de parametros genericos
        String sql = "UPDATE productos SET precio = ?, cantidad = ? WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

        //En lugar de numeros se pueden usar los nombres entre comillas (1,2,3)
            ps.setDouble(1, nuevoPrecio);
            ps.setInt(2, nuevaCantidad);
            ps.setInt(3, id);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.out.println("Error al actualizar: " + e.getMessage());
            return false;
        }
    }


}
