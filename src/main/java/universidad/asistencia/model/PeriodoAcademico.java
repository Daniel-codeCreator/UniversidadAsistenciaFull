package universidad.asistencia.model;

import java.time.LocalDate;

public class PeriodoAcademico {
    private int idPeriodo;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activo;

    public PeriodoAcademico() { }

    public PeriodoAcademico(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        this(0, nombre, fechaInicio, fechaFin, true);
    }

    public PeriodoAcademico(int idPeriodo, String nombre, LocalDate fechaInicio,
                            LocalDate fechaFin, boolean activo) {
        this.idPeriodo = idPeriodo;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activo = activo;
    }

    public int getIdPeriodo() { return idPeriodo; }
    public void setIdPeriodo(int idPeriodo) { this.idPeriodo = idPeriodo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "PeriodoAcademico{" + "id=" + idPeriodo + ", nombre='" + nombre + '\'' +
                ", fechaInicio=" + fechaInicio + ", fechaFin=" + fechaFin +
                ", activo=" + activo + '}';
    }
}
