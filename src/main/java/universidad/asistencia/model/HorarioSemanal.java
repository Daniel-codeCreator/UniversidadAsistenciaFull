package universidad.asistencia.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class HorarioSemanal {
    private int idHorario;
    private Seccion seccion;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    public HorarioSemanal() { }

    public HorarioSemanal(Seccion seccion, DayOfWeek diaSemana,
                          LocalTime horaInicio, LocalTime horaFin) {
        this(0, seccion, diaSemana, horaInicio, horaFin);
    }

    public HorarioSemanal(int idHorario, Seccion seccion, DayOfWeek diaSemana,
                          LocalTime horaInicio, LocalTime horaFin) {
        this.idHorario = idHorario;
        this.seccion = seccion;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public int getIdHorario() { return idHorario; }
    public void setIdHorario(int idHorario) { this.idHorario = idHorario; }
    public Seccion getSeccion() { return seccion; }
    public void setSeccion(Seccion seccion) { this.seccion = seccion; }
    public DayOfWeek getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DayOfWeek diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    @Override
    public String toString() {
        return "HorarioSemanal{" + "id=" + idHorario + ", seccion=" +
                (seccion == null ? null : seccion.getIdSeccion()) + ", dia=" + diaSemana +
                ", horaInicio=" + horaInicio + ", horaFin=" + horaFin + '}';
    }
}
