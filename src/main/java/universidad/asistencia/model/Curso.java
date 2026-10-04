package universidad.asistencia.model;

public class Curso {
    private int idCurso;
    private String codigo;
    private String nombre;
    private String descripcion;
    private int creditos;
    private boolean activo;

    public Curso() { }

    public Curso(String codigo, String nombre, String descripcion, int creditos) {
        this(0, codigo, nombre, descripcion, creditos, true);
    }

    public Curso(int idCurso, String codigo, String nombre, String descripcion,
                 int creditos, boolean activo) {
        this.idCurso = idCurso;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.creditos = creditos;
        this.activo = activo;
    }

    public int getIdCurso() { return idCurso; }
    public void setIdCurso(int idCurso) { this.idCurso = idCurso; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getCreditos() { return creditos; }
    public void setCreditos(int creditos) { this.creditos = creditos; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "Curso{" + "id=" + idCurso + ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' + ", creditos=" + creditos +
                ", activo=" + activo + '}';
    }
}
