import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Yeray on 04/05/2016.
 */
class TablaEncaminamiento extends ArrayList<Encaminamiento> {

    /*Esta clase será la de la tabla de encaminamiento propia de cada Router, con sus datos privados y
    getters y setters que el Router utiliza para comparar y procesar.
     */

    final static double TIMEOUT = 180 * 1000000000L;
    final static double GARBAGETIMEOUT = 120 * 1000000000L;
    private static HashMap<String, Encaminamiento> tabla = new HashMap<>();
    LinkedBlockingQueue<Encaminamiento> TriggeredPackets;

    TablaEncaminamiento() {
    }

    TablaEncaminamiento(LinkedBlockingQueue<Encaminamiento> TriggeredPackets) {
        super();
        this.TriggeredPackets = TriggeredPackets;
        TablaEncaminamiento table = this;
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (table) {
                    for (int i = 0; i < table.size(); i++) {
                        Encaminamiento e = tabla.get(i);

                        if (e.getDistanciaInt() == 1) continue;

                        double elapsed1 = (System.nanoTime() - e.getTimer()) / 1000000000L;
                        double elapsed = System.nanoTime() - e.getTimer();
                        System.err.println(elapsed1);


                        if (!e.garbage & (elapsed > TIMEOUT | e.getDistanciaInt() == 16)) { //Marcando como basura cuando se cumple el tiempo
                            boolean triggered = false;
                            if (e.getDistanciaInt() != 16) triggered = true;
                            System.out.println("Marcando como basura: " + e);
                            e.setDistancia(16);
                            e.resetTimer();
                            e.garbage = true;
                            table.set(i, e);
                            if (triggered) {
                                try {
                                    TriggeredPackets.put(e);
                                } catch (InterruptedException ignored) {
                                }
                            }
                            continue;
                        }
                        if (e.garbage & elapsed > GARBAGETIMEOUT) { //Eliminamos la basura
                            System.out.println("Eliminando: " + e);
                            table.remove(e);
                        }
                    }
                }

            }
        }, 150 * 1000, 15 * 1000);
    }

    void put(InetAddress direccion, Encaminamiento encaminamiento) {
        String dir = direccion.getHostAddress();
        tabla.put(dir, encaminamiento);
    }

    public void set(Encaminamiento encaminamiento) {
        synchronized (this) {
            Encaminamiento e = tabla.get(encaminamiento.getDireccionInet().getHostAddress());

            int index = this.indexOf(e);
            e.resetTimer();
            this.set(index, e);
            try {
                TriggeredPackets.put(e);
            } catch (InterruptedException ignored) {
            }

        }
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

    public int size() {
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


                if ((diferencia >= 60) & (diferencia <= 100)) {
                    //Distancia a este encaminamiento infinito (16)
                    encaminamientoActual.setDistancia(16);

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
