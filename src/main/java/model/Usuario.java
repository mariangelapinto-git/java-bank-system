package model;

public class Usuario {
    private String username;
    private String rol;

    public Usuario(String username, String rol) {
        this.username = username;
        this.rol = rol;
    }

    public String getUsername() { return username; }
    public String getRol() { return rol; }
}
