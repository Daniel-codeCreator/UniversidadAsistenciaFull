package universidad.asistencia.model;

import universidad.asistencia.enums.EstadoSesion;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public class SesionClase {
    private int idSesion;
    private Seccion seccion;
    private LocalDate fecha;
    private LocalTime horaInicioProgramada;
    private LocalTime horaFinProgramada;
    private String aula;
    private EstadoSesion estado;

    public SesionClase() { }

    public SesionClase(Seccion seccion, LocalDate fecha, LocalTime horaInicioProgramada,
                       LocalTime horaFinProgramada, String aula, EstadoSesion estado) {
        this(0, seccion, fecha, horaInicioProgramada, horaFinProgramada, aula, estado);
    }

    public SesionClase(int idSesion, Seccion seccion, LocalDate fecha,
                       LocalTime horaInicioProgramada, LocalTime horaFinProgramada,
                       String aula, EstadoSesion estado) {
        this.idSesion = idSesion;
        this.seccion = seccion;
        this.fecha = fecha;
        this.horaInicioProgramada = horaInicioProgramada;
        this.horaFinProgramada = horaFinProgramada;
        this.aula = aula;
        this.estado = estado;
    }

    public int getIdSesion() { return idSesion; }
    public void setIdSesion(int idSesion) { this.idSesion = idSesion; }
    public Seccion getSeccion() { return seccion; }
    public void setSeccion(Seccion seccion) { this.seccion = seccion; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicioProgramada() { return horaInicioProgramada; }
    public void setHoraInicioProgramada(LocalTime horaInicioProgramada) { this.horaInicioProgramada = horaInicioProgramada; }
    public LocalTime getHoraFinProgramada() { return horaFinProgramada; }
    public void setHoraFinProgramada(LocalTime horaFinProgramada) { this.horaFinProgramada = horaFinProgramada; }
    public String getAula() { return aula; }
    public void setAula(String aula) { this.aula = aula; }
    public EstadoSesion getEstado() { return estado; }
    public void setEstado(EstadoSesion estado) { this.estado = estado; }

    public long calcularDuracionMinutos() {
        return Duration.between(horaInicioProgramada, horaFinProgramada).toMinutes();
    }

    @Override
    public String toString() {
        return "SesionClase{" + "id=" + idSesion + ", seccion=" +
                (seccion == null ? null : seccion.getIdSeccion()) + ", fecha=" + fecha +
                ", horario=" + horaInicioProgramada + "-" + horaFinProgramada +
                ", estado=" + estado + '}';
    }
}
