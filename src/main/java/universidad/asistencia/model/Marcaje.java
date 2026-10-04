package universidad.asistencia.model;

import universidad.asistencia.enums.MedioMarcaje;
import universidad.asistencia.enums.TipoMarcaje;

import java.time.LocalDateTime;

public class Marcaje {
    private long idMarcaje;
    private Estudiante estudiante;
    private SesionClase sesionClase;
    private Dispositivo dispositivo;
    private LocalDateTime fechaHora;
    private TipoMarcaje tipo;
    private MedioMarcaje medio;

    public Marcaje() { }

    public Marcaje(Estudiante estudiante, SesionClase sesionClase, Dispositivo dispositivo,
                   TipoMarcaje tipo, MedioMarcaje medio) {
        this(0, estudiante, sesionClase, dispositivo, null, tipo, medio);
    }

    public Marcaje(Estudiante estudiante, SesionClase sesionClase, Dispositivo dispositivo,
                   LocalDateTime fechaHora, TipoMarcaje tipo, MedioMarcaje medio) {
        this(0, estudiante, sesionClase, dispositivo, fechaHora, tipo, medio);
    }

    public Marcaje(long idMarcaje, Estudiante estudiante, SesionClase sesionClase,
                   Dispositivo dispositivo, LocalDateTime fechaHora,
                   TipoMarcaje tipo, MedioMarcaje medio) {
        this.idMarcaje = idMarcaje;
        this.estudiante = estudiante;
        this.sesionClase = sesionClase;
        this.dispositivo = dispositivo;
        this.fechaHora = fechaHora;
        this.tipo = tipo;
        this.medio = medio;
    }

    public long getIdMarcaje() { return idMarcaje; }
    public void setIdMarcaje(long idMarcaje) { this.idMarcaje = idMarcaje; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public SesionClase getSesionClase() { return sesionClase; }
    public void setSesionClase(SesionClase sesionClase) { this.sesionClase = sesionClase; }
    public Dispositivo getDispositivo() { return dispositivo; }
    public void setDispositivo(Dispositivo dispositivo) { this.dispositivo = dispositivo; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public TipoMarcaje getTipo() { return tipo; }
    public void setTipo(TipoMarcaje tipo) { this.tipo = tipo; }
    public MedioMarcaje getMedio() { return medio; }
    public void setMedio(MedioMarcaje medio) { this.medio = medio; }

    @Override
    public String toString() {
        return "Marcaje{" + "id=" + idMarcaje + ", estudiante=" +
                (estudiante == null ? null : estudiante.getCarnet()) + ", sesion=" +
                (sesionClase == null ? null : sesionClase.getIdSesion()) + ", dispositivo=" +
                (dispositivo == null ? null : dispositivo.getCodigo()) + ", fechaHora=" + fechaHora +
                ", tipo=" + tipo + ", medio=" + medio + '}';
    }
}
