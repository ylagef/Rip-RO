import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Created by Yeray on 06/05/2016.
 */
public class Encaminamiento {

    private byte[] direccion;
    private byte[] mascara;
    private byte[] siguiente;
    private byte[] distancia;

    //Este constructor es para las subredes directamente conectadas (distancia 1).
    public Encaminamiento(InetAddress direccion, int mascara) {
        ByteBuffer dirB = ByteBuffer.allocate(4);
        dirB.putInt(Integer.parseInt(direccion.getHostName()));
        this.direccion = dirB.array();

        ByteBuffer mascaraB = ByteBuffer.allocate(4);
        mascaraB.putInt(mascara);
        this.mascara = mascaraB.array();

        ByteBuffer distB = ByteBuffer.allocate(4);
        distB.putInt(0);
        distB.putInt(0);
        distB.putInt(0);
        distB.putInt(1);
        this.distancia = distB.array();
    }

    public Encaminamiento(InetAddress direccion, int mascara, Router siguiente, int distancia) {
        ByteBuffer dirB = ByteBuffer.allocate(4);
        dirB.putInt(Integer.parseInt(direccion.getHostName()));
        this.direccion = dirB.array();

        ByteBuffer mascaraB = ByteBuffer.allocate(4);
        mascaraB.putInt(mascara);
        this.mascara = mascaraB.array();

        ByteBuffer distB = ByteBuffer.allocate(4);
        distB.putInt(0);
        distB.putInt(0);
        distB.putInt(0);
        distB.putInt(distancia);
        this.distancia = distB.array();

        ByteBuffer sigB = ByteBuffer.allocate(4);
        sigB.putInt(Integer.parseInt(siguiente.getIp().getHostName()));
        this.siguiente = sigB.array();
    }

    public byte[] getDireccion() {
        return direccion;
    }

    public void setDireccion(byte[] direccion) {
        this.direccion = direccion;
    }

    public byte[] getMascara() {
        return mascara;
    }

    public void setMascara(byte[] mascara) {
        this.mascara = mascara;
    }

    public byte[] getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(byte[] siguiente) {
        this.siguiente = siguiente;
    }

    public byte[] getDistancia() {
        return distancia;
    }

    public void setDistancia(byte[] distancia) {
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
