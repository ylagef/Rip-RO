import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    public ByteBuffer datos;
    int ns, key = 0;
    String authData = "";
    int authLength = 16; //MD5
    int tableSize;
    private int indice = 0;

    Paquete(Comando c, int ts) throws NoSuchAlgorithmException {
        indice = 0;
        //Como es el constructor lo que haremos es inicializar el buffer e introducirle su cabecera.
        this.tableSize = ts;
        datos = ByteBuffer.allocate(44 + 20 * (tableSize)); //Esto es necesario ya que hay que reservar el espacio.

        //Cabecera del paquete (ocupa 4 bytes).
        datos.put((byte) c.valor);              //Tipo de comando
        datos.put((byte) 2);                    //Version RIP
        datos.put((byte) 0);                    //No usado
        datos.put((byte) 0);                    //No usado

        //Parte de autenticación
        datos.put((byte) 0xFF);
        datos.put((byte) 0xFF);
        datos.put((byte) 0x00);                          //Tipo 3 crypto
        datos.put((byte) 0x03);                          //Tipo 3 crypto

        int pl = (24 + 20 * (tableSize));
        int pl1 = 0x00FF & pl;
        int pl2 = 0x00FF & (pl >> 8);
        datos.put((byte) pl2);                           //RIPv2 packet length
        datos.put((byte) pl1);                           //RIPv2 packet length

        key = 5;
        datos.put((byte) key);                             //Key ID ¿El puerto?
        datos.put((byte) authLength);                    //Auth data length

        int ns1 = 0x00FF & ns;
        int ns2 = 0x00FF & (ns >> 8);
        int ns3 = 0x00FF & (ns >> 16);
        int ns4 = 0x00FF & (ns >> 24);
        datos.put((byte) ns4);                           //Seq number
        datos.put((byte) ns3);                           //Seq number
        datos.put((byte) ns2);                           //Seq number
        datos.put((byte) ns1);                           //Seq number

        datos.put((byte) 0);                             //No usado
        datos.put((byte) 0);                             //No usado
        datos.put((byte) 0);                             //No usado
        datos.put((byte) 0);                             //No usado
        datos.put((byte) 0);                             //No usado
        datos.put((byte) 0);                             //No usado
        datos.put((byte) 0);                             //No usado
        datos.put((byte) 0);                             //No usado

        //Datos

        int desde = 24 + (20 * (tableSize));

        datos.put(desde, (byte) 0xFF);                              //No usado
        datos.put(desde + 1, (byte) 0xFF);                          //No usado
        datos.put(desde + 2, (byte) 0x00);                          //No usado
        datos.put(desde + 3, (byte) 0x01);                          //No usado

        authData = "hola";
        MessageDigest mDigest = MessageDigest.getInstance("MD5");
        byte[] result = mDigest.digest(authData.getBytes());

        datos.put(desde + 4, result[0]);                           //Auth Data
        datos.put(desde + 5, result[1]);                           //Auth Data
        datos.put(desde + 6, result[2]);                           //Auth Data
        datos.put(desde + 7, result[3]);                           //Auth Data
        datos.put(desde + 8, result[4]);                           //Auth Data
        datos.put(desde + 9, result[5]);                           //Auth Data
        datos.put(desde + 10, result[6]);                           //Auth Data
        datos.put(desde + 11, result[7]);                           //Auth Data
        datos.put(desde + 12, result[8]);                           //Auth Data
        datos.put(desde + 13, result[9]);                           //Auth Data
        datos.put(desde + 14, result[10]);                           //Auth Data
        datos.put(desde + 15, result[11]);                           //Auth Data
        datos.put(desde + 16, result[12]);                           //Auth Data
        datos.put(desde + 17, result[13]);                           //Auth Data
        datos.put(desde + 18, result[14]);                           //Auth Data
        datos.put(desde + 19, result[15]);                           //Auth Data
        /*datos.put(desde + 20, result[16]);                           //Auth Data
        datos.put(desde + 21, result[17]);                           //Auth Data
        datos.put(desde + 22, result[18]);                           //Auth Data
        datos.put(desde + 23, result[19]);                           //Auth Data
*/
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

        int empieza = 24;
        datos.put((empieza + indice * 20), (byte) 0);
        datos.put((empieza + 1 + indice * 20), (byte) 2);               //Esto en el rfc dice que es dos, pero no es seguro.
        datos.put((empieza + 2 + indice * 20), (byte) 0);
        datos.put((empieza + 3 + indice * 20), (byte) 0);
        datos.put((empieza + 4 + indice * 20), direccion[0]);           //Direccion
        datos.put(empieza + 5 + indice * 20, direccion[1]);
        datos.put(empieza + 6 + indice * 20, direccion[2]);
        datos.put(empieza + 7 + indice * 20, direccion[3]);

        int mascara1 = 0xffffffff << (32 - e.getMascaraInt());
        byte[] mascaraBytes = new byte[]{
                (byte) (mascara1 >>> 24), (byte) (mascara1 >> 16 & 0xff), (byte) (mascara1 >> 8 & 0xff), (byte) (mascara1 & 0xff)};

        datos.put(empieza + 8 + indice * 20, mascaraBytes[0]);            //Mascara subred
        datos.put(empieza + 9 + indice * 20, mascaraBytes[1]);
        datos.put(empieza + 10 + indice * 20, mascaraBytes[2]);
        datos.put(empieza + 11 + indice * 20, mascaraBytes[3]);
        datos.put(empieza + 12 + indice * 20, siguiente[0]);          //Siguiente salto
        datos.put(empieza + 13 + indice * 20, siguiente[1]);
        datos.put(empieza + 14 + indice * 20, siguiente[2]);
        datos.put(empieza + 15 + indice * 20, siguiente[3]);
        datos.put(empieza + 16 + indice * 20, distancia[0]);          //Distancia
        datos.put(empieza + 17 + indice * 20, distancia[1]);
        datos.put(empieza + 18 + indice * 20, distancia[2]);
        datos.put(empieza + 19 + indice * 20, distancia[3]);
        indice++;                                           //La proxima vez empezará tras esta entrada y se concatenarán todas en el paquete.

    }

    ArrayList<Encaminamiento> getEncaminamientosDelPacket() {

        ArrayList<Encaminamiento> encaminamientos = new ArrayList<>();

        for (int j = 1; j < ((datos.limit() - 49) / 20) + 1; j++) {
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