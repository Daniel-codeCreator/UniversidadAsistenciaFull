package universidad.asistencia.model;

import universidad.asistencia.enums.EstadoJustificacion;

import java.time.LocalDateTime;

public class Justificacion {
    private int idJustificacion;
    private Estudiante estudiante;
    private SesionClase sesionClase;
    private Docente docenteRegistra;
    private String motivo;
    private LocalDateTime fecha;
    private String observacion;
    private String evidenciaNombre;
    private String evidenciaTipo;
    private byte[] evidencia;
    private EstadoJustificacion estado;
    private Docente docenteResuelve;
    private LocalDateTime fechaResolucion;

    public Justificacion() { }

    public Justificacion(Estudiante estudiante, SesionClase sesionClase, Docente docenteRegistra,
                         String motivo, String observacion) {
        this(0, estudiante, sesionClase, docenteRegistra, motivo, null, observacion,
                null, null, null, EstadoJustificacion.PENDIENTE, null, null);
    }

    public Justificacion(int idJustificacion, Estudiante estudiante, SesionClase sesionClase,
                         Docente docenteRegistra, String motivo, LocalDateTime fecha,
                         String observacion, String evidenciaNombre, String evidenciaTipo,
                         byte[] evidencia, EstadoJustificacion estado, Docente docenteResuelve,
                         LocalDateTime fechaResolucion) {
        this.idJustificacion = idJustificacion;
        this.estudiante = estudiante;
        this.sesionClase = sesionClase;
        this.docenteRegistra = docenteRegistra;
        this.motivo = motivo;
        this.fecha = fecha;
        this.observacion = observacion;
        this.evidenciaNombre = evidenciaNombre;
        this.evidenciaTipo = evidenciaTipo;
        this.evidencia = evidencia;
        this.estado = estado;
        this.docenteResuelve = docenteResuelve;
        this.fechaResolucion = fechaResolucion;
    }

    public int getIdJustificacion() { return idJustificacion; }
    public void setIdJustificacion(int idJustificacion) { this.idJustificacion = idJustificacion; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public SesionClase getSesionClase() { return sesionClase; }
    public void setSesionClase(SesionClase sesionClase) { this.sesionClase = sesionClase; }
    public Docente getDocenteRegistra() { return docenteRegistra; }
    public void setDocenteRegistra(Docente docenteRegistra) { this.docenteRegistra = docenteRegistra; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public String getEvidenciaNombre() { return evidenciaNombre; }
    public void setEvidenciaNombre(String evidenciaNombre) { this.evidenciaNombre = evidenciaNombre; }
    public String getEvidenciaTipo() { return evidenciaTipo; }
    public void setEvidenciaTipo(String evidenciaTipo) { this.evidenciaTipo = evidenciaTipo; }
    public byte[] getEvidencia() { return evidencia; }
    public void setEvidencia(byte[] evidencia) { this.evidencia = evidencia; }
    public EstadoJustificacion getEstado() { return estado; }
    public void setEstado(EstadoJustificacion estado) { this.estado = estado; }
    public Docente getDocenteResuelve() { return docenteResuelve; }
    public void setDocenteResuelve(Docente docenteResuelve) { this.docenteResuelve = docenteResuelve; }
    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    @Override
    public String toString() {
        return "Justificacion{" + "id=" + idJustificacion + ", estudiante=" +
                (estudiante == null ? null : estudiante.getCarnet()) + ", sesion=" +
                (sesionClase == null ? null : sesionClase.getIdSesion()) + ", motivo='" + motivo + '\'' +
                ", estado=" + estado + '}';
    }
}
