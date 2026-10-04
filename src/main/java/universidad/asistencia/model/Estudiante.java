package universidad.asistencia.model;

public class Estudiante {

    private int idEstudiante;
    private String carnet;
    private String nombres;
    private String apellidos;
    private String correo;
    private boolean activo;

    public Estudiante() {
    }

    public Estudiante(
            String carnet,
            String nombres,
            String apellidos,
            String correo
    ) {
        this(0, carnet, nombres, apellidos, correo, true);
    }

    public Estudiante(
            int idEstudiante,
            String carnet,
            String nombres,
            String apellidos,
            String correo,
            boolean activo
    ) {
        this.idEstudiante = idEstudiante;
        this.carnet = carnet;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.activo = activo;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public String getCarnet() {
        return carnet;
    }

    public void setCarnet(String carnet) {
        this.carnet = carnet;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Estudiante{" + "id=" + idEstudiante + ", carnet='" + carnet + '\'' +
                ", nombre='" + nombres + ' ' + apellidos + '\'' + ", correo='" + correo +
                '\'' + ", activo=" + activo + '}';
    }
}
