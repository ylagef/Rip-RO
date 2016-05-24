import java.net.InetAddress;
import java.util.ConcurrentModificationException;
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

    public void compruebaTimeouts() {
        /* Comprueba que los tiempos no hayan pasado */
        try {
            for (Map.Entry<String, Encaminamiento> e : tabla.entrySet()) {

                Encaminamiento encaminamientoActual = e.getValue();

                if (encaminamientoActual.getBasura()) {
                    tabla.remove(e.getKey());
                    continue;
                }

                if (e.getValue().getDistanciaInt() == 0) {
                    e.getValue().resetTimer();
                    continue;
                }

                long tiempoInsercion = encaminamientoActual.getTimer();

                long diferencia = (long) ((System.nanoTime() - tiempoInsercion) / (10e8));

                //if ((diferencia >=240) & (diferencia <= 180)) {
                if ((diferencia >= 35) & (diferencia <= 65)) {
                    //Distancia a este encaminamiento infinito (16)
                    encaminamientoActual.setDistancia(16);
                    //} else if (diferencia > 240) {
                } else if (diferencia > 65) {
                    //Lo borra de la tabla
                    tabla.remove(e.getKey());
                }

            }
        } catch (ConcurrentModificationException e) {
            compruebaTimeouts();
        }

        return;
    }

}

