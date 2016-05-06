import java.net.InetAddress;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Yeray on 04/05/2016.
 */
public class TablaEncaminamiento {

    /*Esta clase será la de la tabla de encaminamiento propia de cada Router, con sus datos privados y
    getters y setters que el Router utiliza para comparar y procesar.
     */

    private static TreeMap<String, Encaminamiento> tabla = new TreeMap<>();

    public void put(InetAddress direccion, Encaminamiento encaminamiento) {
        String dir = direccion.getHostAddress();
        tabla.put(dir, encaminamiento);
    }

    public Encaminamiento get(InetAddress direccion) {
        return tabla.get(direccion.getHostAddress());
    }

    public void imprimirTabla() {
        for (Map.Entry<String, Encaminamiento> e : tabla.entrySet()) {
            e.getKey();
            e.getValue();
            System.out.println(e.getValue().toString());
        }
    }
}

