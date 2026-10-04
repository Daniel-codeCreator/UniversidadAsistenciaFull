package universidad.asistencia.model;

public class Docente {
    private int idDocente;
    private String codigoEmpleado;
    private String nombres;
    private String apellidos;
    private String correo;
    private boolean activo;

    public Docente() { }

    public Docente(String codigoEmpleado, String nombres, String apellidos, String correo) {
        this(0, codigoEmpleado, nombres, apellidos, correo, true);
    }

    public Docente(int idDocente, String codigoEmpleado, String nombres, String apellidos,
                   String correo, boolean activo) {
        this.idDocente = idDocente;
        this.codigoEmpleado = codigoEmpleado;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.activo = activo;
    }

    public int getIdDocente() { return idDocente; }
    public void setIdDocente(int idDocente) { this.idDocente = idDocente; }
    public String getCodigoEmpleado() { return codigoEmpleado; }
    public void setCodigoEmpleado(String codigoEmpleado) { this.codigoEmpleado = codigoEmpleado; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "Docente{" + "id=" + idDocente + ", codigo='" + codigoEmpleado + '\'' +
                ", nombre='" + nombres + ' ' + apellidos + '\'' + ", activo=" + activo + '}';
    }
}
