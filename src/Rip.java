import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Rip {

    static String iplocal = null;
    static int puertolocal = 5512;

    public static void main(String[] args) throws IOException {

        configIP(args); //Lee y asigna la ip inicial dependiendo de argumentos o local
        Servidor server = new Servidor(InetAddress.getByName(iplocal), puertolocal);

        System.out.println("La IP y puerto locales son: " + iplocal + ":" + puertolocal);

        /*
        ServerSocket sskt;

        try {
            sskt = new ServerSocket(puertolocal);
            System.out.println("Escuchando en el puerto: " + puertolocal);
            while (true) {
                Socket cskt = sskt.accept(); //Creamos el socket de conexion con el cliente
                System.out.print("Atendiendo al cliente: " + cskt.getInetAddress());
                OutputStream os = cskt.getOutputStream();
                DataOutputStream flujo = new DataOutputStream(os);
                flujo.writeUTF("Hola cliente, soy el servidor del host: " + iplocal);
                cskt.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
*/
    }


    public static void configIP(String[] args) throws UnknownHostException, SocketException {

        if (args.length != 0) {
            String[] entrada = args[0].split(":");
            iplocal = entrada[0];
            if (entrada.length != 1) {
                puertolocal = Integer.parseInt(entrada[1]);
            }
        } else {

            iplocal = Inet4Address.getLocalHost().getHostAddress();

            //FALTA COMPROBAR QUE SEA ETH0

        }
    }

}
