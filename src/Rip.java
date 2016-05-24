import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;

public class Rip {

    static InetAddress iplocal = null;
    static int puertolocal = 5512;

    public static void main(String[] args) throws IOException {

        configIP(args); //Lee y asigna la ip inicial dependiendo de argumentos o local

        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        System.out.println("Escriba la contraseña: ");
        String password = br.readLine();

        Paquete.setPassword(password);
        System.out.println("");

        Servidor server = new Servidor(iplocal, puertolocal);
        //server.probarTablas(); //Imprime las tablas para probar qué tienen.
    }

    public static void configIP(String[] args) throws UnknownHostException, SocketException {

        if (args.length != 0) {

            String[] entrada = args[0].split(":");
            iplocal = InetAddress.getByName(entrada[0]);

            if (entrada.length != 1) {
                puertolocal = Integer.parseInt(entrada[1]);
            }

        } else {

            //TODO Hay que probar esta funcion en los ordenadores del laboratorio, no es seguro que funcione.

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