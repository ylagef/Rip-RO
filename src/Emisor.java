import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by Yeray on 06/05/2016.
 */
class Emisor implements Runnable {

    private InetAddress ipLocal;
    private TablaEncaminamiento tablaEncaminamiento;

    Emisor(TablaEncaminamiento tablaEncaminamiento, InetAddress ipLocal) {
        this.tablaEncaminamiento = tablaEncaminamiento;
        this.ipLocal = ipLocal;
    }

    @Override
    public void run() {
        try {
            Servidor.envioUnicast(mensajeActualizado(), tablaEncaminamiento.size());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private Paquete mensajeActualizado() throws NoSuchAlgorithmException {
        System.out.println("\t\t **********************   TABLA ACTUAL   ***********************");
        System.out.println("\t\t[\t\t SUBRED \t\t|\t MÉTRICA \t|\t SIGUIENTE SALTO \t]");
        tablaEncaminamiento.imprimirTabla();
        Paquete p = new Paquete(Comando.RESPONSE, tablaEncaminamiento.size());
        for (Map.Entry<String, Encaminamiento> e : tablaEncaminamiento.getTabla().entrySet()) {
            p.addEncaminamiento(e.getValue());
        }
        return p;
    }
}