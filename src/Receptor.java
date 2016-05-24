import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Yeray on 06/05/2016.
 */
public class Receptor implements Runnable {

    private TablaEncaminamiento tablaEncaminamiento;
    private InetAddress ipLocal;
    private int puertoLocal;
    private ArrayBlockingQueue<DatagramPacket> recibidos = new ArrayBlockingQueue<DatagramPacket>(100);

    public Receptor(TablaEncaminamiento tablaEncaminamiento, InetAddress ipLocal, int puertoLocal) {
        this.tablaEncaminamiento = tablaEncaminamiento;
        this.ipLocal = ipLocal;
        this.puertoLocal = puertoLocal;


    }

    public InetAddress getIpLocal() {
        return ipLocal;
    }

    public int getPuertoLocal() {
        return puertoLocal;
    }

    @Override
    public void run() {
        Date inicio = new Date();
        Random r = new Random();
        long timeout = 10000 + ((r.nextInt(10) - 5) * 1000);
        boolean continuar = true;

        try {
            Servidor.receptionSocket.setSoTimeout((int) timeout);
        } catch (SocketException e) {
            System.out.println("TIEMPO DE ESPERA AGOTADO");
        }

        while (continuar) try {

            Date actual = new Date();
            timeout = 10000 - (actual.getTime() - inicio.getTime());
            Servidor.receptionSocket.setSoTimeout((int) timeout);

            DatagramPacket recibido = new DatagramPacket(new byte[504], 504); //TODO ESTO CREA UN BUFFER DEMASIADO GRANDE, (se soluciona capando al procesar lo recibido)

            //System.out.println("Escuchando en el puerto " + Servidor.receptionSocket.getLocalPort());

            Servidor.receptionSocket.receive(recibido);

            recibidos.add(recibido);

            ProcesadorPaquetes procesadorPaquetes = new ProcesadorPaquetes(this, recibidos, tablaEncaminamiento, recibido.getAddress(), recibido.getPort());
            (new Thread(procesadorPaquetes)).start();

        } catch (SocketTimeoutException e) {
            System.out.println("    Se ha agotado el tiempo de espera.\n");
            continuar = false;
        } catch (IllegalArgumentException e) {
            System.out.println("    Tiempo de escucha finalizado.\n");
            continuar = false;
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}