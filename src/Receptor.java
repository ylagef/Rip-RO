import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Yeray on 06/05/2016.
 */
public class Receptor implements Runnable {

    private TablaEncaminamiento tablaEncaminamiento;
    private int puerto;
    private InetAddress ip;

    public Receptor(TablaEncaminamiento tablaEncaminamiento, InetAddress ip, int puerto) {
        this.tablaEncaminamiento = tablaEncaminamiento;
        this.ip = ip;
        this.puerto = puerto;
    }

    @Override
    public void run() {
        Date inicio = new Date();
        long timeout = 10000;
        boolean continuar = true;

        try {
            Servidor.receptionSocket.setSoTimeout((int) timeout);
        } catch (SocketException e) {
            // TODO Auto-generated catch bloc
            System.out.println("TIEMPO DE ESPERA AGOTADO");
        }

        while (continuar) try {
            Date actual = new Date();
            timeout = 10000 - (actual.getTime() - inicio.getTime());
            Servidor.receptionSocket.setSoTimeout((int) timeout);

            DatagramPacket recibido = new DatagramPacket(new byte[504], 504); //TODO ESTO CREA UN BUFFER DEMASIADO GRANDE

            System.out.println("Escuchando en el puerto " + Servidor.receptionSocket.getLocalPort());

            Servidor.receptionSocket.receive(recibido);

            if (Servidor.receptionSocket.isBound()) {
                procesarPaquete(recibido);
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Se ha agotado el tiempo de espera.\n");
            continuar = false;
        } catch (IllegalArgumentException e) {
            System.out.println("EXCEPCION TIMEOUT");
            continuar = false;
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void procesarPaquete(DatagramPacket receivedPacket) throws UnknownHostException {
        System.out.println("    PROCESANDO EL PAQUETE RECIBIDO...");
        System.out.println("        Long Paquete: " + receivedPacket.getLength() + "\n");

        byte[] p = receivedPacket.getData();
        Paquete recibido = new Paquete(p);

        ArrayList<Encaminamiento> aux = recibido.getEncaminamientosDelPacket();


    }


}
