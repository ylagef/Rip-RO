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

            Encaminamiento encaminamientoNuevo = encaminamientos.get(i); //El que ya está en la tabla

            if (encaminamientoNuevo.getSiguienteRout().getIp().getHostAddress().contains(this.receptor.getIpLocal().getHostAddress())) {
                continue;
            }

            if (tabla.containsKey(encaminamientoNuevo.getDireccionInet().getHostAddress())) {

                Encaminamiento encaminamientoActual = tabla.get(encaminamientoNuevo.getDireccionInet().getHostAddress()); //El que nos envió el vecino

                if (encaminamientoActual.getDireccionInet().getHostAddress().contains(receptor.getIpLocal().getHostAddress())) {
                    continue;
                }

                if (encaminamientoActual.getDistanciaInt() == 1 && encaminamientoActual.getSiguiente() == null) {
                    encaminamientoActual.resetTimer();
                    continue;
                }

                int distanciaActual;
                int distanciaNueva;

                distanciaNueva = encaminamientoNuevo.getDistanciaInt();
                distanciaActual = encaminamientoActual.getDistanciaInt();

                if ((distanciaNueva + 1) < distanciaActual) {
                    //Se cambia el encaminamiento
                    tabla.remove(encaminamientoActual.getDireccionInet().getHostAddress());
                    Encaminamiento nuevo = new Encaminamiento(encaminamientoNuevo.getDireccionInet(), encaminamientoNuevo.getMascaraInt(),
                            new Router(emisor, puerto), (encaminamientoNuevo.getDistanciaInt() + 1));
                    nuevo.resetTimer(); //Pone el timer a la hora actual.
                    tabla.put(encaminamientoNuevo.getDireccionInet().getHostAddress(), nuevo);
                }
                encaminamientoActual.resetTimer(); //Se reinicia el tiempo.
            } else { //Añade a la tabla el encaminamiento
                Encaminamiento nuevo = new Encaminamiento(encaminamientoNuevo.getDireccionInet(), encaminamientoNuevo.getMascaraInt(),
                        new Router(emisor, puerto), (encaminamientoNuevo.getDistanciaInt() + 1));
                nuevo.resetTimer();
                tabla.put(nuevo.getDireccionInet().getHostAddress(), nuevo);
            }
        }
    }
}