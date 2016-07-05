import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Yeray on 21/05/2016.
 */
class ProcesadorPaquetes implements Runnable {
    private static DatagramSocket sendSocket;
    private final int puerto;
    private ArrayBlockingQueue<DatagramPacket> recibidos;
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
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (SocketException e) {

            }
        }
    }

    private void procesarPaquete(DatagramPacket receivedPacket) throws UnknownHostException, NoSuchAlgorithmException, SocketException { //Tiene que pasar el paquete (DatagramPacket) a ArrayList.

        if (receivedPacket.getPort() != puerto) {
            System.out.println("Puerto incorrecto. Paquete de:" + receivedPacket.getAddress().getHostAddress() + ":" + receivedPacket.getPort());
            return;
        }

        byte[] p = receivedPacket.getData();

        Paquete recibido = new Paquete(p);

        //Autenticar
        if (recibido.esAutentico()) {
            receptor.aut += "AUTENTICACIÓN CON " + receivedPacket.getAddress().getHostAddress() + " OK.\n";
        } else {
            receptor.aut += "ERROR DE AUTENTICACIÓN CON " + receivedPacket.getAddress().getHostAddress() + ".\n";
            return;
        }

        ArrayList<Encaminamiento> encaminamientos = recibido.getEncaminamientosDelPacket();
        actualizarTabla(encaminamientos, new Router(receivedPacket.getAddress(), receivedPacket.getPort()));
    }

    private void actualizarTabla(ArrayList<Encaminamiento> encaminamientos, Router routerEmisor) throws UnknownHostException, SocketException {

        HashMap<String, Encaminamiento> tabla = tablaEncaminamiento.getTabla();

        Encaminamiento vecino = new Encaminamiento(InetAddress.getByName(routerEmisor.getIp().getHostAddress()), 32,
                routerEmisor, 1);
        tabla.put(vecino.getDireccionInet().getHostAddress(), vecino);

        boolean triggered = false;
        for (Encaminamiento encaminamientoNuevo : encaminamientos) {

            ArrayList<Router> vecinos = Servidor.listaVecinos;
            boolean esVecino = false;
            for (Router neigh : vecinos) {
                if (encaminamientoNuevo.getDireccionInet().getHostAddress().replaceAll("/", "").contentEquals(neigh.getIp().getHostAddress().replaceAll("/", "")) && encaminamientoNuevo.getMascaraInt() == 32) {
                    esVecino = true;
                    break;
                }
            }
            if (esVecino) continue;

            if (encaminamientoNuevo.getDireccionInet().getHostAddress().contains(receptor.getIpLocal().getHostAddress())) {
                continue;
            }

            if (tabla.containsKey(encaminamientoNuevo.getDireccionInet().getHostAddress())) { //Ya tengo esta subred
                Encaminamiento encaminamientoActual =
                        tabla.get(encaminamientoNuevo.getDireccionInet().getHostAddress()); //El de la tabla actual

                if (encaminamientoActual.getSiguienteRout() != null) {
                    if (encaminamientoActual.getSiguienteRout().getIp().getHostAddress().replaceAll("/", "").contentEquals(vecino.getDireccionInet().getHostAddress().replaceAll("/", ""))) {
                        int distanciaNueva = encaminamientoNuevo.getDistanciaInt();
                        int distanciaActual = encaminamientoActual.getDistanciaInt();

                        if (distanciaActual >= 16) {
                            continue;
                        }

                        if (distanciaActual < 16 && encaminamientoActual.getSiguienteRout() != null) {
                            if (encaminamientoActual.getMascaraInt() == encaminamientoNuevo.getMascaraInt()) { //Misma mascara
                                if (distanciaNueva >= 16 && encaminamientoActual.getSiguienteRout().getIp().getHostAddress().replaceAll("/", "")
                                        .contentEquals(routerEmisor.getIp().getHostAddress().replaceAll("/", ""))) { //Cambia la distancia a infinito
                                    triggered = true;
                                    encaminamientoActual.setDistancia(16);
                                    continue;
                                } else if (distanciaNueva >= 16) {
                                    continue;
                                }
                            }
                        }


                        if (encaminamientoActual.getDireccionInet().getHostAddress().contains(receptor.getIpLocal().getHostAddress())) {
                            continue;
                        }

                        if (encaminamientoActual.getDistanciaInt() == 1 && encaminamientoActual.getSiguiente() == null) {
                            encaminamientoActual.resetTimer();
                            continue;
                        }

                        if ((encaminamientoActual.getDireccionInet().getHostAddress().replaceAll("/", "").
                                contains(encaminamientoNuevo.getDireccionInet().getHostAddress().replaceAll("/", ""))) &&
                                (encaminamientoActual.getMascaraInt() != encaminamientoNuevo.getMascaraInt())) {

                            Encaminamiento nuevo = new Encaminamiento(encaminamientoNuevo.getDireccionInet(),
                                    encaminamientoNuevo.getMascaraInt(), routerEmisor,
                                    (encaminamientoNuevo.getDistanciaInt() + 1));

                            nuevo.resetTimer();
                            String nme = encaminamientoNuevo.getDireccionInet().getHostAddress() + "/" + encaminamientoNuevo.getMascaraInt();
                            tabla.put(nme, nuevo);
                            encaminamientoActual.resetTimer();
                            continue;
                        }

                        if ((distanciaNueva + 1) < distanciaActual) {
                            //Se cambia el encaminamiento

                            tabla.remove(encaminamientoActual.getDireccionInet().getHostAddress());
                            Encaminamiento nuevo = new Encaminamiento(encaminamientoNuevo.getDireccionInet(),
                                    encaminamientoNuevo.getMascaraInt(), routerEmisor,
                                    (encaminamientoNuevo.getDistanciaInt() + 1));
                            nuevo.resetTimer(); //Pone el timer a la hora actual.
                            tabla.put(encaminamientoNuevo.getDireccionInet().getHostAddress(), nuevo);
                        }
                        encaminamientoActual.resetTimer(); //Se reinicia el tiempo.
                    }
                }
            } else {
                //No tengo la subred. Añade a la tabla el encaminamiento
                if (encaminamientoNuevo.getDistanciaInt() >= 16) {
                    continue;
                }

                Encaminamiento nuevo = new Encaminamiento(encaminamientoNuevo.getDireccionInet(),
                        encaminamientoNuevo.getMascaraInt(), routerEmisor,
                        (encaminamientoNuevo.getDistanciaInt() + 1));

                nuevo.resetTimer();
                tabla.put(nuevo.getDireccionInet().getHostAddress(), nuevo);
            }
        }

        if (triggered == true) {
            System.out.println("\n\n        -----------------------> TRIGGERED UPDATE <-----------------------");
            tablaEncaminamiento.imprimirTabla();
            System.out.println("        ---------------------> FIN TRIGGERED UPDATE <-----------------------\n\n");
            sendSocket = new DatagramSocket(puerto);
            Emisor e = new Emisor(tablaEncaminamiento, InetAddress.getByName(emisor.getHostAddress()));
            e.run();
            sendSocket.close();
        }
    }

    private boolean mismaSubred(Encaminamiento e1, Encaminamiento e2) throws UnknownHostException {
        InetAddress net1 = netFromEnc(e1.getDireccionInet(), e1.getMascaraInt()); //net del e1
        InetAddress net2 = netFromEnc(e2.getDireccionInet(), e2.getMascaraInt()); //net del e2
        InetAddress net3 = netFromEnc(net2, e1.getMascaraInt());

        return net1.getHostAddress().replaceAll("/", "").contentEquals(net3.getHostAddress().replaceAll("/", ""));
    }

    private InetAddress netFromEnc(InetAddress dir, int masc) throws UnknownHostException {
        int mascara = 0xffffffff << (32 - masc);
        byte[] mascaraBytes = new byte[]{
                (byte) (mascara >>> 24), (byte) (mascara >> 16 & 0xff), (byte) (mascara >> 8 & 0xff), (byte) (mascara & 0xff)};

        byte[] ip = dir.getAddress();

        int aux1 = ip[0] & mascaraBytes[0];
        int aux2 = ip[1] & mascaraBytes[1];
        int aux3 = ip[2] & mascaraBytes[2];
        int aux4 = ip[3] & mascaraBytes[3];

        return InetAddress.getByAddress(new byte[]{(byte) aux1, (byte) aux2, (byte) aux3, (byte) aux4});
    }
}