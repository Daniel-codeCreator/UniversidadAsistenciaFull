package universidad.asistencia.model;

import universidad.asistencia.enums.RolUsuario;

import java.time.LocalDateTime;

public class Usuario {
    private int idUsuario;
    private String usuario;
    private String passwordHash;
    private RolUsuario rol;
    private Docente docente;
    private boolean activo;
    private LocalDateTime fechaCreacion;

    public Usuario() { }

    public Usuario(String usuario, String passwordHash, RolUsuario rol, Docente docente) {
        this(0, usuario, passwordHash, rol, docente, true, null);
    }

    public Usuario(int idUsuario, String usuario, String passwordHash, RolUsuario rol,
                   Docente docente, boolean activo, LocalDateTime fechaCreacion) {
        this.idUsuario = idUsuario;
        this.usuario = usuario;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.docente = docente;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }
    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public String toString() {
        return "Usuario{" + "id=" + idUsuario + ", usuario='" + usuario + '\'' +
                ", rol=" + rol + ", docente=" + (docente == null ? null : docente.getCodigoEmpleado()) +
                ", activo=" + activo + ", fechaCreacion=" + fechaCreacion + '}';
    }
}
