package universidad.asistencia.model;

public class Seccion {
    private int idSeccion;
    private String codigo;
    private Curso curso;
    private PeriodoAcademico periodoAcademico;
    private Docente docente;
    private String aulaAsignada;
    private boolean activo;

    public Seccion() { }

    public Seccion(String codigo, Curso curso, PeriodoAcademico periodoAcademico,
                   Docente docente, String aulaAsignada) {
        this(0, codigo, curso, periodoAcademico, docente, aulaAsignada, true);
    }

    public Seccion(int idSeccion, String codigo, Curso curso, PeriodoAcademico periodoAcademico,
                   Docente docente, String aulaAsignada, boolean activo) {
        this.idSeccion = idSeccion;
        this.codigo = codigo;
        this.curso = curso;
        this.periodoAcademico = periodoAcademico;
        this.docente = docente;
        this.aulaAsignada = aulaAsignada;
        this.activo = activo;
    }

    public int getIdSeccion() { return idSeccion; }
    public void setIdSeccion(int idSeccion) { this.idSeccion = idSeccion; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
    public PeriodoAcademico getPeriodoAcademico() { return periodoAcademico; }
    public void setPeriodoAcademico(PeriodoAcademico periodoAcademico) { this.periodoAcademico = periodoAcademico; }
    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }
    public String getAulaAsignada() { return aulaAsignada; }
    public void setAulaAsignada(String aulaAsignada) { this.aulaAsignada = aulaAsignada; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "Seccion{" + "id=" + idSeccion + ", codigo='" + codigo + '\'' +
                ", curso=" + (curso == null ? null : curso.getCodigo()) +
                ", periodo=" + (periodoAcademico == null ? null : periodoAcademico.getNombre()) +
                ", docente=" + (docente == null ? null : docente.getCodigoEmpleado()) +
                ", aula='" + aulaAsignada + '\'' + ", activo=" + activo + '}';
    }
}
