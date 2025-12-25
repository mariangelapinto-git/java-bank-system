package model;

public class Producto {
    private int id;
    private String nombre;
    private double precio;
    private int cantidad;

    // Constructor: Para crear el producto con datos iniciales
    public Producto(int id, String nombre, double precio, int cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
}

    // Getters y Setters: Para leer y modificar los datos de forma segura
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) {
        if (precio < 0) {
            System.out.println("Error: El precio no puede ser negativo.");
            this.precio = 0;
        } else {
            this.precio = precio;
        }
    }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        if (cantidad < 0) {
            this.cantidad = 0;
        } else {
            this.cantidad = cantidad;
        }
    }

    // toString: Para que al imprimir el objeto no salga algo raro como "Producto@123"
    @Override
    public String toString() {
        return "ID: " + id + " | Nombre: " + nombre + " | Precio: $" + precio + " | Stock: " + cantidad;
    }
}
