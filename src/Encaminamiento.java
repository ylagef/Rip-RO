import java.net.InetAddress;

/**
 * Created by Yeray on 06/05/2016.
 */
public class Encaminamiento {

    private InetAddress direccion;
    private int mascara;
    private Router siguiente;
    private int distancia;

    //Este constructor es para las subredes directamente conectadas (distancia 1).
    public Encaminamiento(InetAddress direccion, int mascara) {
        this.direccion = direccion;
        this.mascara = mascara;
        this.distancia = 1;
    }

    public Encaminamiento(InetAddress direccion, int mascara, Router siguiente, int distancia) {
        this.direccion = direccion;
        this.mascara = mascara;
        this.distancia = distancia;
        this.siguiente = siguiente;
    }

    public InetAddress getDireccion() {
        return direccion;
    }

    public void setDireccion(InetAddress direccion) {
        this.direccion = direccion;
    }

    public int getMascara() {
        return mascara;
    }

    public void setMascara(int mascara) {
        this.mascara = mascara;
    }

    public Router getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(Router siguiente) {
        this.siguiente = siguiente;
    }

    public int getDistancia() {
        return distancia;
    }

    public void setDistancia(int distancia) {
        this.distancia = distancia;
    }

    @Override
    public String toString() {

        if (siguiente != null) {
            return "[ " + direccion + "/" + mascara + " | " + distancia + " | " + siguiente + " ]";
        }

        return "[ " + direccion + "/" + mascara + " | " + distancia + " ]";

    }
}
