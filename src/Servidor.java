import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.StringTokenizer;

enum Comando {

    //Los tipos de comandos sirven para especificar el propósito del mensaje.

    REQUEST(1), RESPONSE(2);

    public final int valor;

    Comando(int valor) {
        this.valor = valor;
    }
}

public class Servidor {

    /*Esta clase servidor crea un servidor en el Router del que se le dice la ip
    y en este lee su fichero de configuración, crea el ArrayList de los vecinos
    e inicia el envío de la tabla.
     */

    static DatagramSocket sendSocket;
    static DatagramSocket receptionSocket;

    private static ArrayList<Router> listaVecinos = new ArrayList<>();
    private InetAddress ipLocal;
    private int puerto;
    private TablaEncaminamiento tablaEncaminamiento = new TablaEncaminamiento();

    public Servidor(InetAddress ipLocal, int puerto) throws IOException {
        this.ipLocal = ipLocal;
        this.puerto = puerto;

        procesarFicheroConfiguracion();
        confPuertos(puerto);                                      //Configura los puertos de escucha y envio de los socket
        //probarTablas();                                         //Imprime las tablas para probar qué tienen.

        while (true) {
            Emisor e = new Emisor(tablaEncaminamiento);
            e.run();

            Receptor r = new Receptor(tablaEncaminamiento, ipLocal, puerto);
            r.run();
        }
    }

    public static void envioUnicast(Paquete paquete) {
        for (Router destino : listaVecinos) {
            try {
                System.out.println("Enviando desde el puerto " + sendSocket.getLocalPort());
                System.out.println("Destino - IP:" + destino.getIp().getHostAddress() + " puerto:" + destino.getPuerto());

                sendSocket.send(paquete.getDatagramPacket(destino.getIp(), destino.getPuerto()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void confPuertos(int puerto) {
        try {
            receptionSocket = new DatagramSocket(puerto); //TODO AQUI SE ABRE EL SOCKET DE RECEPCION
            receptionSocket.setSoTimeout(10000);
            sendSocket = new DatagramSocket(puerto + 1); //TODO AQUI SE ABRE EL SOCKET DE ENVÍO
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void procesarFicheroConfiguracion() throws IOException {

        System.out.print("Leyendo fichero de configuración... ");

        String linea, info;
        String ficheroConf = "ripconf-" + ipLocal.getHostAddress() + ".txt";
        FileReader fr = null;

        try {
            fr = new FileReader(ficheroConf);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BufferedReader br = new BufferedReader(fr);

        while ((linea = br.readLine()) != null) {

            if (!linea.startsWith("//")) { //Para saltarse los comentarios en el fichero de configuracion

                StringTokenizer st = new StringTokenizer(linea);

                while (st.hasMoreTokens()) {

                    String[] lineaInfo;

                    info = st.nextToken();

                    if (info.contains(":") || info.contains("/")) {

                        if (info.contains(":")) {
                            lineaInfo = info.split(":");
                            String ipAux = lineaInfo[0];
                            String puertoAux = lineaInfo[1];
                            Router vecino = new Router(InetAddress.getByName(ipAux), Integer.parseInt(puertoAux));
                            listaVecinos.add(vecino);

                        } else if (info.contains("/")) {
                            lineaInfo = info.split("/");
                            String ipAux = lineaInfo[0];
                            String mascara = lineaInfo[1];
                            Encaminamiento encaminamiento = new Encaminamiento(InetAddress.getByName(ipAux), Integer.parseInt(mascara));
                            tablaEncaminamiento.put(InetAddress.getByName(ipAux), encaminamiento);
                        }

                    } else {
                        String ipAux = info;
                        String puertoAux = "5512";
                        Router vecino = new Router(InetAddress.getByName(ipAux), Integer.parseInt(puertoAux));
                        listaVecinos.add(vecino);

                    }
                }
            }
        }
        System.out.print("Completado con éxito.\n\n");
    }

    public void probarTablas() {

        for (int i = 0; i < listaVecinos.size(); i++) {
            System.out.println("Vecino " + i + " " + listaVecinos.get(i).getIp() + " puerto: " + listaVecinos.get(i).getPuerto());
        }

        System.out.println();

        tablaEncaminamiento.imprimirTabla();

    }

}

