import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by Yeray on 06/05/2016.
 */
class Encaminamiento {

    public boolean garbage;
    private long tiempoInsercion = System.nanoTime(); //Momento en que se inserta en la tabla de encaminamiento.
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
    Encaminamiento(InetAddress direccion, int masc) {
        direccionInet = direccion;
        mascaraInt = masc;


        ByteBuffer dirB = ByteBuffer.allocate(4);
        dirB.put(direccion.getAddress());
        this.direccion = dirB.array();

        int mascara1 = 0xffffffff << (32 - masc);
        this.mascara = new byte[]{
                (byte) (mascara1 >>> 24), (byte) (mascara1 >> 16 & 0xff), (byte) (mascara1 >> 8 & 0xff), (byte) (mascara1 & 0xff)};

        ByteBuffer distB = ByteBuffer.allocate(4);
        distB.put((byte) 0);
        distB.put((byte) 0);
        distB.put((byte) 0);
        distB.put((byte) 1);
        this.distancia = distB.array();
    }

    Encaminamiento(InetAddress direccion, int masc, Router siguiente, int distancia) {
        direccionInet = direccion;
        distanciaInt = distancia;
        mascaraInt = masc;
        siguienteRout = siguiente;

        ByteBuffer dirB = ByteBuffer.allocate(4);
        dirB.put(direccion.getAddress());
        this.direccion = dirB.array();

        int mascara1 = 0xffffffff << (32 - masc);
        this.mascara = new byte[]{
                (byte) (mascara1 >>> 24), (byte) (mascara1 >> 16 & 0xff), (byte) (mascara1 >> 8 & 0xff), (byte) (mascara1 & 0xff)};

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

    byte[] getDireccion() {
        return direccion;
    }

    byte[] getSiguiente() {
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

    byte[] getDistancia() {
        return distancia;
    }

    void setDistancia(int distancia) {
        this.distanciaInt = distancia;

        ByteBuffer distB = ByteBuffer.allocate(4);
        distB.put((byte) 0);
        distB.put((byte) 0);
        distB.put((byte) 0);
        distB.put((byte) distancia);
        this.distancia = distB.array();
    }

    InetAddress getDireccionInet() {
        return direccionInet;
    }

    Router getSiguienteRout() {
        return siguienteRout;
    }

    int getMascaraInt() {
        return mascaraInt;
    }

    int getDistanciaInt() {
        return distanciaInt;
    }

    void resetTimer() {
        tiempoInsercion = System.nanoTime();
    }

    long getTimer() {
        return tiempoInsercion;
    }

    @Override
    public String toString() {
        if (direccionInet.getHostAddress().length() >= 9 && direccionInet.getHostAddress().length() < 13) {
            if (siguiente != null) {
                return "[\t" + direccionInet.getHostAddress() + "/" + mascaraInt + "\t\t|\t\t" + distanciaInt + "\t\t|\t\t" + siguienteRout.getIp().getHostAddress()
                        + "\t]";
            }

            return "[\t" + direccionInet.getHostAddress() + "/" + mascaraInt + "\t\t|\t\t" + distanciaInt + "\t\t]";
        } else if (direccionInet.getHostAddress().length() < 9) {
            if (siguiente != null) {
                return "[\t" + direccionInet.getHostAddress() + "/" + mascaraInt + "\t\t\t|\t\t" + distanciaInt + "\t\t|\t\t" + siguienteRout.getIp().getHostAddress()
                        + "\t]";
            }

            return "[\t" + direccionInet.getHostAddress() + "/" + mascaraInt + "\t\t\t|\t\t" + distanciaInt + "\t\t]";
        } else {
            if (siguiente != null) {
                return "[\t" + direccionInet.getHostAddress() + "/" + mascaraInt + "\t|\t\t" + distanciaInt + "\t\t|\t\t" + siguienteRout.getIp().getHostAddress()
                        + "\t]";
            }

            return "[\t" + direccionInet.getHostAddress() + "/" + mascaraInt + "\t|\t\t" + distanciaInt + "\t\t]";
        }
    }
}