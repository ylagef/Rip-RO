import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
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

class Servidor {

    /*Esta clase servidor crea un servidor en el Router del que se le dice la ip
    y en este lee su fichero de configuración, crea el ArrayList de los vecinos
    e inicia el envío de la tabla.
     */

    static DatagramSocket receptionSocket;
    static int ns = 0;
    private static DatagramSocket sendSocket;
    private static ArrayList<Router> listaVecinos = new ArrayList<>();
    private InetAddress ipLocal;
    private TablaEncaminamiento tablaEncaminamiento = new TablaEncaminamiento();

    Servidor(InetAddress ipLocal, int puerto) throws IOException {
        this.ipLocal = ipLocal;

        procesarFicheroConfiguracion();

        while (true) {
            sendSocket = new DatagramSocket(puerto);
            Emisor e = new Emisor(tablaEncaminamiento, ipLocal);
            e.run();
            sendSocket.close();

            receptionSocket = new DatagramSocket(puerto);
            Receptor r = new Receptor(tablaEncaminamiento, ipLocal);
            r.run();
            receptionSocket.close();

            tablaEncaminamiento.compruebaTimeouts();
        }
    }

    static void envioUnicast(Paquete paquete, int size) {
        for (Router destino : listaVecinos) {
            try {
                System.out.println("Enviando desde el puerto " + sendSocket.getLocalPort() + " hacia " + destino.getIp().getHostAddress() + ":" + destino.getPuerto() + "...");

                ArrayList<Encaminamiento> encaminamientos = paquete.getEncaminamientosDelPacket();
                Paquete aux = new Paquete(Comando.RESPONSE, size);
                for (Encaminamiento encaminamiento : encaminamientos) {
                    if (encaminamiento.getSiguienteRout().getIp().getHostAddress().replaceAll("/", "").contains(destino.getIp().getHostAddress().replaceAll("/", ""))) {
                        encaminamiento.setDistancia(16);
                    }
                    aux.addEncaminamiento(encaminamiento);
                }

                Paquete p = new Paquete(Comando.RESPONSE, size);

                for (Encaminamiento e : aux.getEncaminamientosDelPacket()) {
                    if (e.getSiguienteRout().getIp().getHostAddress().equals(destino.getIp()))
                        e.setDistancia(16);
                    p.addEncaminamiento(e);
                }

                paquete.setSeqNumber(ns);
                ns++;

                DatagramPacket dp = new DatagramPacket(paquete.datos.array(), paquete.datos.limit(), destino.getIp(), destino.getPuerto());
                paquete.autenticarPaquete();

                sendSocket.send(dp);
            } catch (IOException e) {
                System.out.println("ERROR EN EL ENVÍO.");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    private void procesarFicheroConfiguracion() throws IOException {

        System.out.print("Leyendo fichero de configuración... ");

        String linea, info;
        String ficheroConf = "ripconf-" + ipLocal.getHostAddress() + ".topo";
        FileReader fr = null;

        try {
            fr = new FileReader(ficheroConf);
        } catch (FileNotFoundException e) {
            System.out.println("Error al leer el fichero de configuración. Compruebe el nombre.");
            System.exit(-1);
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
                        String puertoAux = "5512";
                        Router vecino = new Router(InetAddress.getByName(info), Integer.parseInt(puertoAux));
                        listaVecinos.add(vecino);

                    }
                }
            }
        }
        System.out.print("Completado con éxito.\n\n");
    }

}