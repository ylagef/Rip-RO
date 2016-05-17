import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;

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

        ServerSocket ss = null;

        try {
            ss = new ServerSocket(puerto);
            System.out.println("Escuchando en: IP=" + ip + " puerto=" + puerto);
            ss.setSoTimeout(10000);
            Socket cliente = ss.accept();
            System.out.println("Conectando al cliente " + cliente.getInetAddress().getHostAddress() + ":" + cliente.getPort() + " ...");
            System.out.println("¡CLIENTE CONECTADO!");
            DataInputStream dis = new DataInputStream(cliente.getInputStream());
            String str = dis.readUTF();
            System.out.println("Mensaje = " + str);
        } catch (SocketTimeoutException e) {
            System.out.println("- Nadie se ha intentado conectar tras 10 segundos escuchando - \n");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
