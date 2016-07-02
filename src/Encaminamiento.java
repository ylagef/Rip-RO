import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by Yeray on 06/05/2016.
 */
public class Encaminamiento {

    long tiempoInsercion = System.nanoTime(); //Momento en que se inserta en la tabla de encaminamiento.

    boolean basura = false;

    //Para facilitar el método toString
    private InetAddress direccionInet;
    private Router siguienteRout;
    private int mascaraInt;
    private int distanciaInt = 1; //Las subredes conectadas están a distancia 0 (directamente conectadas) al router.

    //Para crear correctamente el paquete de bytes
    private byte[] direccion;
    private byte[] mascara;
    private byte[] siguiente;
    private byte[] distancia;

    //Este constructor es para las subredes directamente conectadas (distancia 1).
    public Encaminamiento(InetAddress direccion, int masc) {
        direccionInet = direccion;
        mascaraInt = masc;


        ByteBuffer dirB = ByteBuffer.allocate(4);
        dirB.put(direccion.getAddress());
        this.direccion = dirB.array();

        int mascara1 = 0xffffffff << (32 - masc);
        byte[] mascaraBytes = new byte[]{
                (byte) (mascara1 >>> 24), (byte) (mascara1 >> 16 & 0xff), (byte) (mascara1 >> 8 & 0xff), (byte) (mascara1 & 0xff)};
        this.mascara = mascaraBytes;

        ByteBuffer distB = ByteBuffer.allocate(4);
        distB.put((byte) 0);
        distB.put((byte) 0);
        distB.put((byte) 0);
        distB.put((byte) 1);
        this.distancia = distB.array();
    }

    public Encaminamiento(InetAddress direccion, int masc, Router siguiente, int distancia) {
        direccionInet = direccion;
        distanciaInt = distancia;
        mascaraInt = masc;
        siguienteRout = siguiente;

        ByteBuffer dirB = ByteBuffer.allocate(4);
        dirB.put(direccion.getAddress());
        this.direccion = dirB.array();

        int mascara1 = 0xffffffff << (32 - masc);
        byte[] mascaraBytes = new byte[]{
                (byte) (mascara1 >>> 24), (byte) (mascara1 >> 16 & 0xff), (byte) (mascara1 >> 8 & 0xff), (byte) (mascara1 & 0xff)};
        this.mascara = mascaraBytes;

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

    public void setDistancia(int distancia) {
        this.distanciaInt = distancia;
        ByteBuffer distB = ByteBuffer.allocate(4);
        distB.put((byte) 0);
        distB.put((byte) 0);
        distB.put((byte) 0);
        distB.put((byte) distancia);
        this.distancia = distB.array();
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

    public void resetTimer() {
        tiempoInsercion = System.nanoTime();
        return;
    }

    public long getTimer() {
        return tiempoInsercion;
    }

    @Override
    public String toString() {

        if (siguiente != null) {
            return "[\t" + direccionInet.getHostAddress() + "/" + mascaraInt + "\t\t|\t\t" + distanciaInt + "\t\t|\t\t" + siguienteRout.getIp().getHostAddress()
                    + "\t]";
        }

        return "[\t" + direccionInet.getHostAddress() + "/" + mascaraInt + "\t\t|\t\t" + distanciaInt + "\t\t]";

    }
}