import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Yeray on 07/05/2016.
 */
class Paquete {

    //                      Para RIPv2 hay un formato de paquete que es:
    // Comando | Numero Version | No usado (0) | AFI | Route TAG | Destino | Mask | Por donde | Distancia
    //       1                1              2     2           2         4      4           4           4
    //                                  En Bytes (octetos)

    static private byte[] password;
    private ByteBuffer datos;
    private int indice = 1;

    Paquete(Comando c, int tableSize) {
        indice = 1;
        //Como es el constructor lo que haremos es inicializar el buffer e introducirle su cabecera.

        datos = ByteBuffer.allocate(4 + 20 * (tableSize + 1)); //Esto es necesario ya que hay que reservar el espacio.

        //Cabecera del paquete (ocupa 4 bytes).
        datos.put((byte) c.valor);              //Tipo de comando
        datos.put((byte) 2);                    //Version RIP
        datos.put((byte) 0);                    //No usado
        datos.put((byte) 0);                    //No usado

        //Parte de autenticación
        datos.put((byte) 255);
        datos.put((byte) 255);
        datos.put((byte) 0);
        datos.put((byte) 2);                         //Tipo
        datos.put(password);                     //Password


        /*
        //Cabecera del paquete (ocupa 4 bytes).
        datos.put((byte) c.valor);              //Tipo de comando
        datos.put((byte) 2);                    //Version RIP
        datos.put((byte) 0);                    //No usado
        datos.put((byte) 0);                    //No usado

        //Parte de autenticación
        datos.put((byte) 255);
        datos.put((byte) 255);
        datos.put((byte) 0);
        datos.put((byte) 3);                    //Tipo 3 crypto

        int pl = (4 + 20 * (tableSize + 1));
        int pl1 = 0x00FF & pl;
        int pl2 = 0x00FF & (pl >> 8);
        datos.put((byte) pl2);                         //RIPv2 packet length
        datos.put((byte) pl1);                         //RIPv2 packet length
        datos.put((byte) 5);                           //Key ID ¿El puerto?
        datos.put((byte) 0);                           //Auth data length
        datos.put((byte) 0);                           //Seq number
        datos.put((byte) 0);                           //Seq number
        datos.put((byte) 0);                           //Seq number
        datos.put((byte) 0);                           //Seq number
        datos.put((byte) 0);                          //No usado
        datos.put((byte) 0);                            //No usado
        datos.put((byte) 0);                           //No usado
        datos.put((byte) 0);                         //No usado
        //Datos
        datos.put((byte) 0);                            //No usado
        datos.put((byte) 0);                            //No usado
        datos.put((byte) 0);                           //No usado
        datos.put((byte) 0x001);                        //No usado
        datos.put(password);                           //Password */

    }

    Paquete(byte[] datagramPacket) {
        datos = ByteBuffer.allocate(datagramPacket.length);
        datos = ByteBuffer.wrap(datagramPacket);
    }

    static void genPassword(String pass) {
        ByteBuffer buffer = null;

        if (pass.length() <= 16) {
            buffer = ByteBuffer.allocate(16);
            buffer.put(pass.getBytes());
            for (int i = 0; i < (16 - pass.length()); i++) {
                buffer.put((byte) 0x00);
            }
        }


        if (pass.length() > 16) {
            pass = pass.substring(0, 16);
            buffer = ByteBuffer.allocate(16);
            buffer.put(pass.getBytes());
            for (int i = 0; i < (16 - pass.length()); i++) {
                buffer.put((byte) 0x00);
            }
        }

        password = buffer.array();

    }

    private static int convertNetmaskToCIDR(InetAddress netmask) {

        byte[] netmaskBytes = netmask.getAddress();
        int cidr = 0;
        boolean zero = false;
        for (byte b : netmaskBytes) {
            int mask = 0x80;

            for (int i = 0; i < 8; i++) {
                int result = b & mask;
                if (result == 0) {
                    zero = true;
                } else if (zero) {
                    throw new IllegalArgumentException("Invalid netmask.");
                } else {
                    cidr++;
                }
                mask >>>= 1;
            }
        }
        return cidr;
    }

    void addEncaminamiento(Encaminamiento e) {

        //Hay que añadir cada encaminamiento seguido del anterior en el buffer.

        byte[] direccion = e.getDireccion();
        byte[] siguiente = e.getSiguiente();
        byte[] distancia = e.getDistancia();


        //Las entradas van seguidas después de la cabecera concatenadas, entonces las posiciones tienen que ir consecutivas.
        //Empieza en la posición 4 (tras la cabecera) y después van cada 20 (índice*20).
        //datos.put(posicion,byte) posicion inicial = cabecera + 20*indice para concatenar las entradas.

        datos.put(4 + indice * 20, (byte) 0);
        datos.put(5 + indice * 20, (byte) 2);               //Esto en el rfc dice que es dos, pero no es seguro.
        datos.put(6 + indice * 20, (byte) 0);
        datos.put(7 + indice * 20, (byte) 0);
        datos.put(8 + indice * 20, direccion[0]);           //Direccion
        datos.put(9 + indice * 20, direccion[1]);
        datos.put(10 + indice * 20, direccion[2]);
        datos.put(11 + indice * 20, direccion[3]);

        int mascara1 = 0xffffffff << (32 - e.getMascaraInt());
        byte[] mascaraBytes = new byte[]{
                (byte) (mascara1 >>> 24), (byte) (mascara1 >> 16 & 0xff), (byte) (mascara1 >> 8 & 0xff), (byte) (mascara1 & 0xff)};

        datos.put(12 + indice * 20, mascaraBytes[0]);            //Mascara subred
        datos.put(13 + indice * 20, mascaraBytes[1]);
        datos.put(14 + indice * 20, mascaraBytes[2]);
        datos.put(15 + indice * 20, mascaraBytes[3]);
        datos.put(16 + indice * 20, siguiente[0]);          //Siguiente salto
        datos.put(17 + indice * 20, siguiente[1]);
        datos.put(18 + indice * 20, siguiente[2]);
        datos.put(19 + indice * 20, siguiente[3]);
        datos.put(20 + indice * 20, distancia[0]);          //Distancia
        datos.put(21 + indice * 20, distancia[1]);
        datos.put(22 + indice * 20, distancia[2]);
        datos.put(23 + indice * 20, distancia[3]);
        indice++;                                           //La proxima vez empezará tras esta entrada y se concatenarán todas en el paquete.

    }

    DatagramPacket getDatagramPacket(InetAddress iPdestino, int puertoDestino) {

        Paquete p = new Paquete(Comando.RESPONSE, getEncaminamientosDelPacket().size());
        for (Encaminamiento e : this.getEncaminamientosDelPacket()) {
            if (e.getSiguienteRout().getIp().getHostAddress().equals(iPdestino))
                e.setDistancia(16);
            p.addEncaminamiento(e);
        }

        return new DatagramPacket(p.datos.array(), p.datos.limit(), iPdestino, puertoDestino);
    }

    ArrayList<Encaminamiento> getEncaminamientosDelPacket() {

        ArrayList<Encaminamiento> encaminamientos = new ArrayList<>();

        for (int j = 1; j < (datos.limit() - 4) / 20; j++) {
            try {
                byte[] nombreIp = new byte[]{datos.get(j * 20 + 8), datos.get(j * 20 + 9), datos.get(j * 20 + 10), datos.get(j * 20 + 11)};
                InetAddress ip = InetAddress.getByAddress(nombreIp);

                if (ip.getHostAddress().contains("0.0.0.0")) {
                    continue;
                }

                InetAddress msk = InetAddress.getByAddress(new byte[]{datos.get(j * 20 + 12), datos.get(j * 20 + 13), datos.get(j * 20 + 14), datos.get(j * 20 + 15)});

                int mascara = convertNetmaskToCIDR(msk);

                Router siguiente = new Router(InetAddress.getByAddress(new byte[]{datos.get(j * 20 + 16), datos.get(j * 20 + 17), datos.get(j * 20 + 18), datos.get(j * 20 + 19)}));

                int distancia = datos.get(j * 20 + 23);

                encaminamientos.add(new Encaminamiento(ip, mascara, siguiente, distancia));

            } catch (UnknownHostException e) {
                System.out.println("EXCEPCION EN:   PAQUETE > getEncaminamientosDelPacket");
            }

        }

        return encaminamientos;
    }

    boolean isPassValid() {
        byte[] passToValidate = new byte[16];
        for (int i = 8; i < 24; i++) {
            passToValidate[i - 8] = datos.get(i);
        }
        return Arrays.equals(passToValidate, password);
    }
}