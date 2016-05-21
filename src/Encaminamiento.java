import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by Yeray on 06/05/2016.
 */
public class Encaminamiento {

    //Para facilitar el método toString
    private InetAddress direccionInet;
    private Router siguienteRout;
    private int mascaraInt;
    private int distanciaInt;

    //Para crear correctamente el paquete de bytes
    private byte[] direccion;
    private byte[] mascara;
    private byte[] siguiente;
    private byte[] distancia;

    //Este constructor es para las subredes directamente conectadas (distancia 1).
    public Encaminamiento(InetAddress direccion, int mascara) {
        direccionInet = direccion;
        mascaraInt = mascara;


        ByteBuffer dirB = ByteBuffer.allocate(4);
        dirB.put(direccion.getAddress());
        this.direccion = dirB.array();

        ByteBuffer mascaraB = ByteBuffer.allocate(4);
        mascaraB.put((byte) mascara);
        this.mascara = mascaraB.array();

        ByteBuffer distB = ByteBuffer.allocate(4);
        distB.put((byte) 0);
        distB.put((byte) 0);
        distB.put((byte) 0);
        distB.put((byte) 1);
        this.distancia = distB.array();
    }

    public Encaminamiento(InetAddress direccion, int mascara, Router siguiente, int distancia) {
        direccionInet = direccion;
        distanciaInt = distancia;
        mascaraInt = mascara;
        siguienteRout = siguiente;

        ByteBuffer dirB = ByteBuffer.allocate(4);
        dirB.put(direccion.getAddress());
        this.direccion = dirB.array();

        ByteBuffer mascaraB = ByteBuffer.allocate(4);
        mascaraB.put((byte) mascara);
        this.mascara = mascaraB.array();

        ByteBuffer distB = ByteBuffer.allocate(4);
        distB.put((byte) 0);
        distB.put((byte) 0);
        distB.put((byte) 0);
        distB.put((byte) distancia);
        this.distancia = distB.array();

        ByteBuffer sigB = ByteBuffer.allocate(4);
        sigB.put(siguiente.getIp().getAddress());
        this.siguiente = sigB.array();
    }

    public byte[] getDireccion() {
        return direccion;
    }

    public byte[] getMascara() {
        return mascara;
    }

    public byte[] getSiguiente() {
        if (siguiente != null) {
            return siguiente;
        } else {
            ByteBuffer sigB = ByteBuffer.allocate(4);
            try {
                sigB.put(InetAddress.getByName("0.0.0.0").getAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            return sigB.array();
        }

    }

    public byte[] getDistancia() {
        return distancia;
    }

    public InetAddress getDireccionInet() {
        return direccionInet;
    }

    public Router getSiguienteRout() {
        return siguienteRout;
    }

    public int getMascaraInt() {
        return mascaraInt;
    }

    public int getDistanciaInt() {
        return distanciaInt;
    }

    @Override
    public String toString() {

        if (siguiente != null) {
            return "[ " + direccionInet.getHostName() + "/" + mascaraInt + " | " + distanciaInt + " | " + siguienteRout.getIp().getCanonicalHostName() + " ]";
        }

        return "[ " + direccionInet.getHostName() + "/" + mascaraInt + " | " + distanciaInt + " ]";

    }
}
