package model;

public class Usuario {
    private int id;
    private String username;
    private String rol;

    public Usuario(int id, String username, String rol) {
        this.id=id;
        this.username = username;
        this.rol = rol;
    }

    public int getId(){return id;}
    public String getUsername() { return username; }
    public String getRol() { return rol; }
}
