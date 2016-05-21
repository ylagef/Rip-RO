import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Yeray on 21/05/2016.
 */
public class ProcesadorPaquetes implements Runnable {
    private final int puerto;
    ArrayBlockingQueue<DatagramPacket> recibidos;
    private Receptor receptor;
    private TablaEncaminamiento tablaEncaminamiento;
    private InetAddress emisor;

    ProcesadorPaquetes(Receptor receptor, ArrayBlockingQueue<DatagramPacket> recibidos, TablaEncaminamiento tablaEncaminamiento, InetAddress emisor, int puerto) {
        this.receptor = receptor;
        this.recibidos = recibidos;
        this.tablaEncaminamiento = tablaEncaminamiento;
        this.emisor = emisor;
        this.puerto = puerto;
    }

    @Override
    public void run() {
        while (true) {
            DatagramPacket paquete = null;
            try {
                paquete = recibidos.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                procesarPaquete(paquete);
            } catch (UnknownHostException e) {
                System.out.println("ERROR AL PROCESAR EL PAQUETE.");
            }
        }
    }

    private void procesarPaquete(DatagramPacket receivedPacket) throws UnknownHostException { //Tiene que pasar el paquete (DatagramPacket) a ArrayList.
        //System.out.println("    PROCESANDO PAQUETE. Recibido desde " + receivedPacket.getAddress().getHostAddress() + ":" + receivedPacket.getPort());

        byte[] p = receivedPacket.getData();
        Paquete recibido = new Paquete(p);

        ArrayList<Encaminamiento> encaminamientos = recibido.getEncaminamientosDelPacket();
        actualizarTabla(encaminamientos);
    }

    public void actualizarTabla(ArrayList<Encaminamiento> encaminamientos) {
        HashMap<String, Encaminamiento> tabla = tablaEncaminamiento.getTabla();
        for (int i = 0; i < encaminamientos.size(); i++) {
            Encaminamiento encaminamiento = encaminamientos.get(i);
            if (tabla.containsKey(encaminamiento.getDireccionInet().getHostAddress())) { //Si la tabla de encaminamiento YA contiene ese encaminamiento


            } else {
                tabla.put(encaminamiento.getDireccionInet().getHostAddress(), new Encaminamiento(encaminamiento.getDireccionInet(), encaminamiento.getMascaraInt(), new Router(emisor, puerto), encaminamiento.getDistanciaInt()));
            }
        }
    }
}
