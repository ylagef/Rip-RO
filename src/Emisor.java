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

        while (true) {
            Servidor.envioUnicast(mensajeActualizado());

            synchronized (this) {
                try {

                    wait(2000);

                } catch (InterruptedException e) {
                }
            }
        }
    }

    private Paquete mensajeActualizado() {
        System.out.println("*********** Enviando tabla... ***********");
        System.out.println("***********      Estado:      ***********");
        tablaEncaminamiento.imprimirTabla();
        Paquete p = new Paquete(Comando.RESPONSE, tablaEncaminamiento.size());
        return p;
    }
}
