package universidad.asistencia.controller;

import universidad.asistencia.enums.*;
import universidad.asistencia.model.*;
import universidad.asistencia.service.AsistenciaService;
import java.time.LocalDateTime;
import java.util.List;

public class AsistenciaController {
    private final AsistenciaService service;
    public AsistenciaController() { this(new AsistenciaService()); }
    public AsistenciaController(AsistenciaService service) { this.service = service; }
    public Marcaje registrarMarcaje(int estudiante, int sesion, int dispositivo, TipoMarcaje tipo, MedioMarcaje medio) { return service.registrarMarcaje(estudiante, sesion, dispositivo, tipo, medio); }
    public Marcaje registrarMarcaje(int estudiante, int sesion, int dispositivo, TipoMarcaje tipo, MedioMarcaje medio, LocalDateTime fechaHora) { return service.registrarMarcaje(estudiante, sesion, dispositivo, tipo, medio, fechaHora); }
    public boolean validarPuedeMarcar(int estudiante, int sesion, int dispositivo, TipoMarcaje tipo, MedioMarcaje medio) { return service.validarPuedeMarcar(estudiante, sesion, dispositivo, tipo, medio); }
    public ResultadoAsistencia calcularResultadoAsistencia(int estudiante, int sesion) { return service.calcularResultadoAsistencia(estudiante, sesion); }
    public double calcularPorcentajeAsistencia(int estudiante) { return service.calcularPorcentajeAsistencia(estudiante); }
    public List<Marcaje> obtenerHistorial(int estudiante) { return service.obtenerHistorial(estudiante); }
    public List<Estudiante> obtenerEstudiantesBajo80() { return service.obtenerEstudiantesBajo80(); }
}
