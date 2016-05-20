import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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

            DatagramPacket recibido = new DatagramPacket(new byte[504], 504);

            System.out.println("Escuchando en el puerto " + Servidor.receptionSocket.getLocalPort());

            Servidor.receptionSocket.receive(recibido);

            if (Servidor.receptionSocket.isBound()) {
                procesarPaquete(recibido);
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Se ha agotado el tiempo de espera.\n");
            continuar = false;
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void procesarPaquete(DatagramPacket receivedPacket) {
        System.out.println("    PROCESANDO EL PAQUETE RECIBIDO...");
        System.out.println("        Long Paquete: " + receivedPacket.getLength() + "\n");
    }
}
