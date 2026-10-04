package universidad.asistencia.model;

import java.time.LocalDate;

public class Inscripcion {
    private int idInscripcion;
    private Estudiante estudiante;
    private Seccion seccion;
    private LocalDate fechaInscripcion;
    private LocalDate fechaRetiro;
    private boolean activa;

    public Inscripcion() { }

    public Inscripcion(Estudiante estudiante, Seccion seccion, LocalDate fechaInscripcion) {
        this(0, estudiante, seccion, fechaInscripcion, null, true);
    }

    public Inscripcion(int idInscripcion, Estudiante estudiante, Seccion seccion,
                       LocalDate fechaInscripcion, LocalDate fechaRetiro, boolean activa) {
        this.idInscripcion = idInscripcion;
        this.estudiante = estudiante;
        this.seccion = seccion;
        this.fechaInscripcion = fechaInscripcion;
        this.fechaRetiro = fechaRetiro;
        this.activa = activa;
    }

    public int getIdInscripcion() { return idInscripcion; }
    public void setIdInscripcion(int idInscripcion) { this.idInscripcion = idInscripcion; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public Seccion getSeccion() { return seccion; }
    public void setSeccion(Seccion seccion) { this.seccion = seccion; }
    public LocalDate getFechaInscripcion() { return fechaInscripcion; }
    public void setFechaInscripcion(LocalDate fechaInscripcion) { this.fechaInscripcion = fechaInscripcion; }
    public LocalDate getFechaRetiro() { return fechaRetiro; }
    public void setFechaRetiro(LocalDate fechaRetiro) { this.fechaRetiro = fechaRetiro; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    @Override
    public String toString() {
        return "Inscripcion{" + "id=" + idInscripcion + ", estudiante=" +
                (estudiante == null ? null : estudiante.getCarnet()) + ", seccion=" +
                (seccion == null ? null : seccion.getIdSeccion()) + ", fecha=" + fechaInscripcion +
                ", activa=" + activa + '}';
    }
}
