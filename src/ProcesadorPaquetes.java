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

            Encaminamiento encaminamientoNuevo = encaminamientos.get(i); //El que ya está en la tabla

            if (tabla.containsKey(encaminamientoNuevo.getDireccionInet().getHostAddress())) { //Si la tabla de encaminamiento YA contiene ese encaminamiento se comparan las distancias.
                Encaminamiento encaminamientoActual = tabla.get(encaminamientoNuevo.getDireccionInet().getHostAddress()); //El que nos envió el vecino

                int distanciaActual;
                int distanciaNueva;

                distanciaNueva = encaminamientoNuevo.getDistanciaInt();
                distanciaActual = encaminamientoActual.getDistanciaInt();

                if ((distanciaNueva + 1) < distanciaActual) {
                    //Se cambia el encaminamiento
                    tabla.remove(encaminamientoActual.getDireccionInet().getHostAddress());
                    tabla.put(encaminamientoNuevo.getDireccionInet().getHostAddress(), encaminamientoNuevo);
                }
            } else { //Añade a la tabla el encaminamiento
                tabla.put(encaminamientoNuevo.getDireccionInet().getHostAddress(), new Encaminamiento(encaminamientoNuevo.getDireccionInet(), encaminamientoNuevo.getMascaraInt(),
                        new Router(emisor, puerto), (encaminamientoNuevo.getDistanciaInt() + 1)));
            }
        }
    }
}
