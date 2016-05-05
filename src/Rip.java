import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class Rip {

    static InetAddress iplocal = null;
    static int puertolocal = 5512;

    public static void main(String[] args) throws IOException {

        configIP(args); //Lee y asigna la ip inicial dependiendo de argumentos o local

        Servidor server = new Servidor(iplocal, puertolocal);

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
            iplocal = InetAddress.getByName(entrada[0]);

            if (entrada.length != 1) {
                puertolocal = Integer.parseInt(entrada[1]);
            }

        } else {

            Enumeration en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface i = (NetworkInterface) en.nextElement();
                if (i.getName().contains("wlan2")) {
                    for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements(); ) {
                        InetAddress addr = (InetAddress) en2.nextElement();
                        if (!addr.isLoopbackAddress()) {
                            if (addr instanceof Inet4Address) {
                                iplocal = addr;
                                return;
                            }
                        }
                    }
                }
            }

            System.out.printf("NO EXISTE ETH0");
            System.exit(-1);
        }

    }
}