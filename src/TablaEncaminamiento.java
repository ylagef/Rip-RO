import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yeray on 04/05/2016.
 */
public class TablaEncaminamiento {

    /*Esta clase será la de la tabla de encaminamiento propia de cada Router, con sus datos privados y
    getters y setters que el Router utiliza para comparar y procesar.
     */

    private static HashMap<String, Encaminamiento> tabla = new HashMap<>();

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
            System.out.println("        " + e.getValue().toString());
        }
        System.out.print("\n");
    }

    public int size() {
        return tabla.size();
    }

    public HashMap<String, Encaminamiento> getTabla() {
        return tabla;
    }


}

