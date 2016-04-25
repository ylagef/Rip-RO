import java.net.*;
import java.util.Collections;
import java.util.Enumeration;

public class Rip {

    public static void main(String[] args) throws UnknownHostException, SocketException {
        String ipInicial = ipInicial(args); //Devuelve la ip inicial dependiendo de argumentos o local
        System.out.print(ipInicial);

    }

    public static String ipInicial(String[] args) throws UnknownHostException, SocketException {
        String iplocal = null;
        String puerto = "5512";

        if (args.length != 0) {
            String[] entrada = args[0].split(":");
            iplocal = entrada[0];
            if (entrada.length != 1) {
                puerto = entrada[1];
            }
        } else {

            //FALTA

        }
        return (iplocal + ":" + puerto);
    }

}
