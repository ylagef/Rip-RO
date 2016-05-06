import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Servidor {

    /*Esta clase servidor crea un servidor en el Router del que se le dice la ip
    y en este lee su fichero de configuración, crea el ArrayList de los vecinos
    e inicia el envío de la tabla.
     */

    InetAddress iplocal;
    int puerto;
    ArrayList<Router> listaVecinos = new ArrayList<>();
    TablaEncaminamiento tablaEncaminamiento = new TablaEncaminamiento();

    public Servidor(InetAddress iplocal, int puerto) throws IOException {

        this.iplocal = iplocal;
        this.puerto = puerto;

        procesarConfiguracion();

        for (int i = 0; i < listaVecinos.size(); i++) {
            System.out.println("Vecino " + i + " " + listaVecinos.get(i).getIp() + " " + listaVecinos.get(i).getPuerto());
        }

        tablaEncaminamiento.imprimirTabla();

    }

    private void procesarConfiguracion() throws IOException {

        String linea, info;
        String ficheroConf = "ripconf-" + iplocal.getHostAddress() + ".txt";
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

                    String[] lineaInfo = new String[0];

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

}

