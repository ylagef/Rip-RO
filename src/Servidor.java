import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
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

    private InetAddress ipLocal;
    private int puerto;
    private ArrayList<Router> listaVecinos = new ArrayList<>();
    private TablaEncaminamiento tablaEncaminamiento = new TablaEncaminamiento();

    public Servidor(InetAddress ipLocal, int puerto) throws IOException {
        this.ipLocal = ipLocal;
        this.puerto = puerto;

        procesarFicheroConfiguracion();

        Emisor e = new Emisor(tablaEncaminamiento);
        e.run();

    }

    public static void envioUnicast(Paquete paquete) {

    }

    private void procesarFicheroConfiguracion() throws IOException {

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
    }

    public void probarTablas() {

        for (int i = 0; i < listaVecinos.size(); i++) {
            System.out.println("Vecino " + i + " " + listaVecinos.get(i).getIp() + " puerto: " + listaVecinos.get(i).getPuerto());
        }

        System.out.println();

        tablaEncaminamiento.imprimirTabla();

    }
}

