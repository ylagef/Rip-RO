import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Yeray on 02/07/2016.
 */
public class TriggeredUpdate implements Runnable {
    LinkedBlockingQueue<Encaminamiento> triggeredPaquetes;
    private LinkedList<Encaminamiento> paquetesTriggeredPendientes = new LinkedList<>();


    TriggeredUpdate(LinkedBlockingQueue<Encaminamiento> link) {
        this.triggeredPaquetes = link;
    }

    @Override
    public void run() {
        long timer = System.nanoTime();
        long timeToWait;
        Random r = new Random();
        long elapsed;
        Encaminamiento e;
        while (true) {
            paquetesTriggeredPendientes = new LinkedList<>();
            try {
                e = triggeredPaquetes.take();
                paquetesTriggeredPendientes.add(e);
            } catch (InterruptedException ignored) {
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
            elapsed = System.nanoTime() - timer;
            timeToWait = (1 + r.nextInt(4)) * 1000000000L;

            if (elapsed < timeToWait) {
                try {
                    Thread.sleep(timeToWait - elapsed);
                } catch (InterruptedException ignored) {
                }
            }
            System.out.println("TRIGGERED UPDATE");
            triggeredPaquetes.drainTo(paquetesTriggeredPendientes);
            Servidor.envioUnicast(getTriggeredPaquete(), triggeredPaquetes.size());
        }
    }

    private Paquete getTriggeredPaquete() {
        Paquete p = new Paquete(Comando.RESPONSE, paquetesTriggeredPendientes.size());
        System.err.println("-----TRIGGERED-------");

        for (Encaminamiento e : paquetesTriggeredPendientes) {
            System.out.println(e);
            p.addEncaminamiento(e);
        }
        System.out.println("--------------------");

        return p;
    }
}
