import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Servidor {
    public static String Fichero;

    public static void main(String[] args) {

        ArrayList<Host> listaVecinos = new ArrayList<Host>();

        Scanner fichero = LeerFichero(Fichero);

        try {
            String[] linea = null;
            int puerto;
            InetAddress direc = null;

            while (fichero.hasNextLine()) {

                if (fichero.nextLine().contains(":")) {
                    linea = fichero.nextLine().trim().split(":");
                } else if (fichero.nextLine().contains("/")) {
                    linea = fichero.nextLine().trim().split("/");
                }

                try {
                    direc = InetAddress.getByName(linea[0]);
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (!linea[1].isEmpty())
                    puerto = Integer.parseInt(linea[1]);
                else
                    puerto = 5512;

                listaVecinos.add(new Host(direc, puerto));


            }

        } catch (StringIndexOutOfBoundsException e) {
            System.err
                    .println("1.- No se pudo leer el fichero ejecucion (formato incorrecto)\n"
                            + e);
        } catch (NumberFormatException e) {
            System.err
                    .println("2.- No se pudo leer el fichero ejecucion (formato incorrecto)\n"
                            + e);
        } catch (IndexOutOfBoundsException e) {
            System.err
                    .println("3.- No se pudo leer el fichero ejecucion (formato incorrecto)\n"
                            + e);
        }

        fichero.close();

    }

    public static Scanner LeerFichero(String fichero) {

        Scanner lectura = null;
        try {

            lectura = new Scanner(new FileInputStream(fichero));

        } catch (FileNotFoundException e) {
            System.err.println("Fichero Inexistente <" + fichero + ">");
            System.exit(-1);
        }
        return lectura;
    }

}
