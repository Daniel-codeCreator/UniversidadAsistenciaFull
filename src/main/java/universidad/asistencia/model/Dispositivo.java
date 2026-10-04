package universidad.asistencia.model;

public class Dispositivo {
    private int idDispositivo;
    private String codigo;
    private String nombre;
    private String tipo;
    private String ubicacion;
    private boolean activo;

    public Dispositivo() { }

    public Dispositivo(String codigo, String nombre, String tipo, String ubicacion) {
        this(0, codigo, nombre, tipo, ubicacion, true);
    }

    public Dispositivo(int idDispositivo, String codigo, String nombre, String tipo,
                       String ubicacion, boolean activo) {
        this.idDispositivo = idDispositivo;
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.activo = activo;
    }

    public int getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(int idDispositivo) { this.idDispositivo = idDispositivo; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "Dispositivo{" + "id=" + idDispositivo + ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' + ", tipo='" + tipo + '\'' +
                ", ubicacion='" + ubicacion + '\'' + ", activo=" + activo + '}';
    }
}
