import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Yeray on 06/05/2016.
 */
class Receptor implements Runnable {

    private TablaEncaminamiento tablaEncaminamiento;
    private InetAddress ipLocal;
    private int puertoLocal;
    private ArrayBlockingQueue<DatagramPacket> recibidos = new ArrayBlockingQueue<>(100);

    Receptor(TablaEncaminamiento tablaEncaminamiento, InetAddress ipLocal, int puertoLocal) {
        this.tablaEncaminamiento = tablaEncaminamiento;
        this.ipLocal = ipLocal;
        this.puertoLocal = puertoLocal;


    }

    InetAddress getIpLocal() {
        return ipLocal;
    }

    @Override
    public void run() {
        Date inicio = new Date();

        double aleat = Math.random() * 5 + 0.1;
        long timeout = 10000 + (long) (aleat * 1000);
        long tOut = timeout;
        boolean continuar = true;

        try {
            Servidor.receptionSocket.setSoTimeout((int) timeout);
        } catch (SocketException e) {
            System.out.println("TIEMPO DE ESPERA AGOTADO");
        }

        while (continuar) try {

            Date actual = new Date();
            timeout = timeout - (actual.getTime() - inicio.getTime());
            Servidor.receptionSocket.setSoTimeout((int) timeout);

            DatagramPacket recibido = new DatagramPacket(new byte[504], 504);


            Servidor.receptionSocket.receive(recibido);

            recibidos.add(recibido);

            ProcesadorPaquetes procesadorPaquetes = new ProcesadorPaquetes(this, recibidos, tablaEncaminamiento, recibido.getAddress(), recibido.getPort());
            (new Thread(procesadorPaquetes)).start();

        } catch (SocketTimeoutException e) {
            System.out.println("    Tiempo de recepción finalizado. " + tOut + "ms.\n");
            continuar = false;
        } catch (IllegalArgumentException e) {
            System.out.println("    Tiempo de recepción finalizado. " + tOut + "ms.\n");
            continuar = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}