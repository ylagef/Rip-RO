import java.net.InetAddress;
import java.util.Map;

/**
 * Created by Yeray on 06/05/2016.
 */
public class Emisor implements Runnable {

    InetAddress ipLocal;
    private TablaEncaminamiento tablaEncaminamiento;

    public Emisor(TablaEncaminamiento tablaEncaminamiento, InetAddress ipLocal) {
        this.tablaEncaminamiento = tablaEncaminamiento;
        this.ipLocal = ipLocal;
    }

    @Override
    public void run() {
        Servidor.envioUnicast(mensajeActualizado());
    }

    private Paquete mensajeActualizado() {
        System.out.println("*********** TABLA ACTUAL ***********");
        tablaEncaminamiento.imprimirTabla();
        Paquete p = new Paquete(Comando.RESPONSE, tablaEncaminamiento.size());
        for (Map.Entry<String, Encaminamiento> e : tablaEncaminamiento.getTabla().entrySet()) {

            /*
            //TODO PROBAR ESTO CON IPS
            if (!(e.getValue().getSiguienteRout().getIp() == ipLocal)) { //Solo añade el encaminamiento si no soy el siguiente salto //TODO COMPROBAR ESTO -> SPLIT-HORIZON
                e.getValue().setDistancia(16); //Distancia infinita (>15)
                p.addEncaminamiento(e.getValue());
            }
            */

            p.addEncaminamiento(e.getValue()); //No puedo hacer lo de comparar las ip si lo hago en local.
        }
        return p;
    }
}