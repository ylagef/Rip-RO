import java.util.Map;

/**
 * Created by Yeray on 06/05/2016.
 */
public class Emisor implements Runnable {

    private TablaEncaminamiento tablaEncaminamiento;

    public Emisor(TablaEncaminamiento tablaEncaminamiento) {
        this.tablaEncaminamiento = tablaEncaminamiento;
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
            p.addEncaminamiento(e.getValue());
        }
        return p;
    }
}