package com.bank.util;

import model.Cuenta;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ArchivoUtil {
    // Cambiamos el nombre del archivo de texto
    private static final String NOMBRE_ARCHIVO = "respaldo_cuentas.txt";

    // Guarda la lista de cuentas en el archivo (Backup)
    public static void guardarArchivo(List<Cuenta> cuentas) {
        try (PrintWriter salida = new PrintWriter(new FileWriter(NOMBRE_ARCHIVO))) {
            for (Cuenta c : cuentas) {
                // Usamos los nuevos atributos: id, numeroCuenta, titular, saldo
                salida.println(c.getId() + "," +
                        c.getNumeroCuenta() + "," +
                        c.getTitular() + "," +
                        c.getSaldo());
            }
            System.out.println("Respaldo en texto generado correctamente.");
        } catch (IOException e) {
            System.out.println("Error al guardar el archivo: " + e.getMessage());
        }
    }

    // Lee el archivo y carga las cuentas en una lista
    public static List<Cuenta> cargarArchivo() {
        List<Cuenta> cuentas = new ArrayList<>();
        File archivo = new File(NOMBRE_ARCHIVO);

        if (!archivo.exists()) return cuentas;

        try (BufferedReader entrada = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                String[] datos = linea.split(",");

                // Mapeo según el nuevo orden
                int id = Integer.parseInt(datos[0]);
                String numeroCuenta = datos[1];
                String titular = datos[2];
                double saldo = Double.parseDouble(datos[3]);

                cuentas.add(new Cuenta(id, numeroCuenta, titular, saldo));
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error al cargar el archivo: " + e.getMessage());
        }
        return cuentas;
    }
}
