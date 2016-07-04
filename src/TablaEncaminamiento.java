import java.net.InetAddress;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yeray on 04/05/2016.
 */
class TablaEncaminamiento {

    /*Esta clase será la de la tabla de encaminamiento propia de cada Router, con sus datos privados y
    getters y setters que el Router utiliza para comparar y procesar.
     */

    private static HashMap<String, Encaminamiento> tabla = new HashMap<>();

    void put(InetAddress direccion, Encaminamiento encaminamiento) {
        String dir = direccion.getHostAddress();
        tabla.put(dir, encaminamiento);
    }

    void imprimirTabla() {
        try {
            for (Map.Entry<String, Encaminamiento> e : tabla.entrySet()) {
                e.getKey();
                e.getValue();
                System.out.println("        " + e.getValue().toString());
            }
            System.out.print("\n");
        } catch (ConcurrentModificationException ignored) {
        }

    }

    int size() {
        return tabla.size();
    }

    HashMap<String, Encaminamiento> getTabla() {
        return tabla;
    }

    void compruebaTimeouts() {
        /* Comprueba que los tiempos no hayan pasado */
        try {
            for (Map.Entry<String, Encaminamiento> e : tabla.entrySet()) {

                Encaminamiento encaminamientoActual = e.getValue();


                if (e.getValue().getDistanciaInt() == 1 && e.getValue().getSiguienteRout() == null) {
                    e.getValue().resetTimer();
                    continue;
                }

                long tiempoInsercion = encaminamientoActual.getTimer();

                long diferencia = (long) ((System.nanoTime() - tiempoInsercion) / (10e8));


                if ((diferencia >= 20) & (diferencia <= 30)) { //TODO poner a 60 y 100 al acabar
                    //Distancia a este encaminamiento infinito (16)
                    encaminamientoActual.setDistancia(16);
                    System.out.println("Triggered Update");
                } else if (diferencia > 100) {
                    //Lo borra de la tabla
                    tabla.remove(e.getKey());
                }
            }
        } catch (ConcurrentModificationException e) {
            compruebaTimeouts();
        }

    }

}
