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
        Servidor.envioUnicast(mensajeActualizado(), tablaEncaminamiento.size());
    }

    private Paquete mensajeActualizado() {
        System.out.println("*********** TABLA ACTUAL ***********");
        tablaEncaminamiento.imprimirTabla();
        Paquete p = new Paquete(Comando.RESPONSE, tablaEncaminamiento.size());
        for (Map.Entry<String, Encaminamiento> e : tablaEncaminamiento.getTabla().entrySet()) {
            p.addEncaminamiento(e.getValue());
        }
        return p;
    }
}