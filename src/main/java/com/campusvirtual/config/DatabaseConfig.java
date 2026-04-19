package com.campusvirtual.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestión centralizada de la conexión a la base de datos MySQL.
 * Lee las credenciales desde db.properties (fichero externo, no en el código fuente).
 * Implementa patrón Singleton para la conexión.
 */
public class DatabaseConfig {
    private static Connection connection;

    private DatabaseConfig() {}

    /**
     * Obtiene la conexión activa. Si no existe o está cerrada, la crea leyendo db.properties.
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Properties props = loadProperties();
                String url = props.getProperty("db.url");
                String user = props.getProperty("db.user");
                String password = props.getProperty("db.password");

                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Conexión a MySQL establecida correctamente.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL no encontrado: " + e.getMessage());
            throw new RuntimeException("Driver MySQL no disponible", e);
        } catch (SQLException e) {
            System.err.println("Error conectando a MySQL: " + e.getMessage());
            throw new RuntimeException("No se pudo conectar a la base de datos", e);
        }
        return connection;
    }

    /**
     * Cierra la conexión activa. Debe llamarse al cerrar la aplicación.
     */
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexión MySQL cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error cerrando conexión: " + e.getMessage());
        }
    }

    /**
     * Carga las propiedades desde db.properties.
     * Busca primero en el directorio de trabajo, luego en el classpath.
     */
    private static Properties loadProperties() {
        Properties props = new Properties();

        // Intentar cargar desde archivo externo (directorio de trabajo)
        try (InputStream is = new FileInputStream("db.properties")) {
            props.load(is);
            return props;
        } catch (IOException ignored) {
            // Si no existe el archivo externo, buscar en el classpath
        }

        // Intentar cargar desde el classpath (recursos de la aplicación)
        try (InputStream is = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
                return props;
            }
        } catch (IOException ignored) {}

        throw new RuntimeException(
            "No se encontró db.properties. Copia db.properties.example como db.properties " +
            "y configura las credenciales de la base de datos."
        );
    }
}
