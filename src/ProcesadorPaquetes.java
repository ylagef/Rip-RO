import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

    ProcesadorPaquetes(Receptor receptor, ArrayBlockingQueue<DatagramPacket> recibidos,
                       TablaEncaminamiento tablaEncaminamiento, InetAddress emisor, int puerto) {
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


        if (receivedPacket.getPort() != puerto) {
            System.out.println("Puerto incorrecto.");
            return;
        }

        byte[] p = receivedPacket.getData();

        if (!new Paquete(p).isPassValid()) {
            System.out.println("Contraseña incorrecta.");
            return;
        }

        Paquete recibido = new Paquete(p);

        ArrayList<Encaminamiento> encaminamientos = recibido.getEncaminamientosDelPacket();
        actualizarTabla(encaminamientos);
    }

    public void actualizarTabla(ArrayList<Encaminamiento> encaminamientos) throws UnknownHostException {

        HashMap<String, Encaminamiento> tabla = tablaEncaminamiento.getTabla();

        Encaminamiento vecino = new Encaminamiento(InetAddress.getByName(emisor.getHostAddress()), 32,
                new Router(emisor, puerto), 1);
        tabla.put(vecino.getDireccionInet().getHostAddress(), vecino);

        for (int i = 0; i < encaminamientos.size(); i++) {

            Encaminamiento encaminamientoNuevo = encaminamientos.get(i); //El que nos mandó el vecino

            if (encaminamientoNuevo.getDireccionInet().getHostAddress().contains(receptor.getIpLocal().getHostAddress())) {
                continue;
            }

            if (tabla.containsKey(encaminamientoNuevo.getDireccionInet().getHostAddress())) { //Ya tengo esta subred

                Iterator it = tabla.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry e = (Map.Entry) it.next();
                    if (mismaSubred(encaminamientoNuevo, (Encaminamiento) e.getValue())) {

                    }
                }

                Encaminamiento encaminamientoActual =
                        tabla.get(encaminamientoNuevo.getDireccionInet().getHostAddress()); //El de la tabla actual

                if (encaminamientoNuevo.getDireccionInet().getHostAddress().contains(receptor.getIpLocal().getHostAddress())) {
                    System.out.println("SOY YO");
                    continue;
                }

                if (encaminamientoActual.getDireccionInet().getHostAddress().contains(receptor.getIpLocal().getHostAddress())) {
                    continue;
                }

                if (encaminamientoActual.getDistanciaInt() == 1 && encaminamientoActual.getSiguiente() == null) {
                    encaminamientoActual.resetTimer();
                    continue;
                }

                int distanciaNueva = encaminamientoNuevo.getDistanciaInt();
                int distanciaActual = encaminamientoActual.getDistanciaInt();

                if ((distanciaNueva + 1) < distanciaActual) {
                    //Se cambia el encaminamiento

                    tabla.remove(encaminamientoActual.getDireccionInet().getHostAddress());
                    Encaminamiento nuevo = new Encaminamiento(encaminamientoNuevo.getDireccionInet(),
                            encaminamientoNuevo.getMascaraInt(), new Router(emisor, puerto),
                            (encaminamientoNuevo.getDistanciaInt() + 1));
                    nuevo.resetTimer(); //Pone el timer a la hora actual.
                    tabla.put(encaminamientoNuevo.getDireccionInet().getHostAddress(), nuevo);
                }

                encaminamientoActual.resetTimer(); //Se reinicia el tiempo.

            } else { //No tengo la subred. Añade a la tabla el encaminamiento
                Encaminamiento nuevo = new Encaminamiento(encaminamientoNuevo.getDireccionInet(),
                        encaminamientoNuevo.getMascaraInt(), new Router(emisor, puerto),
                        (encaminamientoNuevo.getDistanciaInt() + 1));

                nuevo.resetTimer();
                tabla.put(nuevo.getDireccionInet().getHostAddress(), nuevo);
            }
        }
    }

    InetAddress mismaSubred(Encaminamiento e1, Encaminamiento e2) throws UnknownHostException {
        int mascara = 0xffffffff << (32 - Integer.valueOf(e.getMascaraInt()));
        byte[] mascaraBytes = new byte[]{
                (byte) (mascara >>> 24), (byte) (mascara >> 16 & 0xff), (byte) (mascara >> 8 & 0xff), (byte) (mascara & 0xff)};

        byte[] ip = e.getDireccionInet().getAddress();

        int aux1 = ip[0] & mascaraBytes[0];
        int aux2 = ip[1] & mascaraBytes[1];
        int aux3 = ip[2] & mascaraBytes[2];
        int aux4 = ip[3] & mascaraBytes[3];

        InetAddress network = InetAddress.getByAddress(new byte[]{(byte) aux1, (byte) aux2, (byte) aux3, (byte) aux4});

    }

}