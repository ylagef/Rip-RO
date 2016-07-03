import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by Yeray on 07/05/2016.
 */
class Paquete {

    //                      Para RIPv2 hay un formato de paquete que es:
    // Comando | Numero Version | No usado (0) | AFI | Route TAG | Destino | Mask | Por donde | Distancia
    //       1                1              2     2           2         4      4           4           4
    //                                  En Bytes (octetos)
    private static int numEnc;
    static private byte[] password;
    private final int key = 5; //La clave de cifrado. Key ID.
    public ByteBuffer datos;
    private int ns = 0;
    private int authLength = 16; //MD5
    private int tableSize;
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

        datos.put((byte) key);                           //Key ID ¿El puerto?
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
    }

    Paquete(byte[] datagramPacket) {
        datos = ByteBuffer.allocate(datagramPacket.length);
        datos = ByteBuffer.wrap(datagramPacket);
        setVars();
        getEncaminamientosDelPacket();
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

    void setVars() {

        byte[] a1 = new byte[]{datos.get(12), datos.get(13), datos.get(14), datos.get(15)};
        ByteBuffer wrapped = ByteBuffer.wrap(a1); // big-endian by default
        this.ns = wrapped.getInt();
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
        numEnc = 0;
        for (int j = 1; j < ((datos.limit() - 49) / 20) + 1; j++) {
            try {
                byte[] nombreIp = new byte[]{datos.get(j * 20 + 8), datos.get(j * 20 + 9), datos.get(j * 20 + 10), datos.get(j * 20 + 11)};
                InetAddress ip = InetAddress.getByAddress(nombreIp);

                if (ip.getHostAddress().contains("0.0.0.0")) {
                    continue;
                }

                int mascara;
                try {
                    InetAddress msk = InetAddress.getByAddress(new byte[]{datos.get(j * 20 + 12), datos.get(j * 20 + 13), datos.get(j * 20 + 14), datos.get(j * 20 + 15)});
                    mascara = convertNetmaskToCIDR(msk);
                } catch (IllegalArgumentException e) {
                    continue;
                }


                Router siguiente = new Router(InetAddress.getByAddress(new byte[]{datos.get(j * 20 + 16), datos.get(j * 20 + 17), datos.get(j * 20 + 18), datos.get(j * 20 + 19)}));

                int distancia = datos.get(j * 20 + 23);
                numEnc++;
                encaminamientos.add(new Encaminamiento(ip, mascara, siguiente, distancia));
            } catch (UnknownHostException e) {
                System.out.println("EXCEPCION EN:   PAQUETE > getEncaminamientosDelPacket");
            }
        }
        return encaminamientos;
    }

    void setSeqNumber(int ns) {
        this.ns = ns;
        int ns1 = 0x00FF & ns;
        int ns2 = 0x00FF & (ns >> 8);
        int ns3 = 0x00FF & (ns >> 16);
        int ns4 = 0x00FF & (ns >> 24);
        datos.put(12, (byte) ns4);                           //Seq number
        datos.put(13, (byte) ns3);                           //Seq number
        datos.put(14, (byte) ns2);                           //Seq number
        datos.put(15, (byte) ns1);                           //Seq number
    }

    void autenticarPaquete() throws NoSuchAlgorithmException {
        if (tableSize == 0) {

        } else {
            numEnc = tableSize;
        }

        //Password + Data + Key ID + ADlength + Seq Number

        ByteBuffer encaminamientos = null;
        encaminamientos = ByteBuffer.allocate(13 * numEnc + 1);

        if (numEnc == 0) {
            return;
        }

        for (int i = 1; i < numEnc + 1; i++) {
            encaminamientos.put(datos.get(i * 20 + 8));
            encaminamientos.put(datos.get(i * 20 + 9));
            encaminamientos.put(datos.get(i * 20 + 10));
            encaminamientos.put(datos.get(i * 20 + 11));
            encaminamientos.put(datos.get(i * 20 + 12));
            encaminamientos.put(datos.get(i * 20 + 13));
            encaminamientos.put(datos.get(i * 20 + 14));
            encaminamientos.put(datos.get(i * 20 + 15));
            encaminamientos.put(datos.get(i * 20 + 16));
            encaminamientos.put(datos.get(i * 20 + 17));
            encaminamientos.put(datos.get(i * 20 + 18));
            encaminamientos.put(datos.get(i * 20 + 19));
            encaminamientos.put(datos.get(i * 20 + 23));
        }

        ByteBuffer autenticar;
        autenticar = ByteBuffer.allocate(16 + (13 * numEnc + 1) + 1 + 1 + 4);

        autenticar.put(password);

        byte[] encs = new byte[numEnc * 13];
        System.arraycopy(encaminamientos.array(), 0, encs, 0, numEnc * 13);

        autenticar.put(encs);
        autenticar.put((byte) key);
        datos.put(10, (byte) key);
        autenticar.put((byte) authLength);
        int ns1 = 0x00FF & ns;
        int ns2 = 0x00FF & (ns >> 8);
        int ns3 = 0x00FF & (ns >> 16);
        int ns4 = 0x00FF & (ns >> 24);
        autenticar.put((byte) ns4);                           //Seq number
        autenticar.put((byte) ns3);                           //Seq number
        autenticar.put((byte) ns2);                           //Seq number
        autenticar.put((byte) ns1);

        byte[] aut = new byte[autenticar.limit() - 1];
        System.arraycopy(autenticar.array(), 0, aut, 0, autenticar.limit() - 1);

        MessageDigest mDigest = MessageDigest.getInstance("MD5");
        byte[] result = mDigest.digest(aut);

        int desde = datos.limit() - 20;

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
    }

    boolean esAutentico() throws NoSuchAlgorithmException {
        String autenticacionRecibida;
        int desde = datos.limit() - 19;

        for (int x = 0; x < 500; x++) {
            if (datos.get(datos.limit() - x - 1) != 0) {
                desde = datos.limit() - x - 16 - 4;
                break;
            }
        }

        byte[] auth = new byte[]{
                datos.get(desde + 4),
                datos.get(desde + 5),
                datos.get(desde + 6),
                datos.get(desde + 7),
                datos.get(desde + 8),
                datos.get(desde + 9),
                datos.get(desde + 10),
                datos.get(desde + 11),
                datos.get(desde + 12),
                datos.get(desde + 13),
                datos.get(desde + 14),
                datos.get(desde + 15),
                datos.get(desde + 16),
                datos.get(desde + 17),
                datos.get(desde + 18),
                datos.get(desde + 19)
        };
        autenticacionRecibida = "" + auth[0] + auth[1] + auth[2] + auth[3] + auth[4] + auth[5] + auth[6] + auth[7] +
                auth[8] + auth[9] + auth[10] + auth[11] + auth[12] + auth[13] + auth[14] + auth[15];

        String autenticacionActual;

        autenticarPaquete();
        int d = datos.limit() - 16 - 4;

        for (int y = 0; y < 500; y++) {
            if (datos.get(datos.limit() - y - 1) != 0) {
                d = datos.limit() - y - 16 - 4;
                break;
            }
        }
        byte[] authent = new byte[]{
                datos.get(d + 4),
                datos.get(d + 5),
                datos.get(d + 6),
                datos.get(d + 7),
                datos.get(d + 8),
                datos.get(d + 9),
                datos.get(d + 10),
                datos.get(d + 11),
                datos.get(d + 12),
                datos.get(d + 13),
                datos.get(d + 14),
                datos.get(d + 15),
                datos.get(d + 16),
                datos.get(d + 17),
                datos.get(d + 18),
                datos.get(d + 19)
        };
        autenticacionActual = "" + authent[0] + authent[1] + authent[2] + authent[3] + authent[4] + authent[5] + authent[6] + authent[7] + authent[8] +
                authent[9] + authent[10] + authent[11] + authent[12] + authent[13] + authent[14] + authent[15];

        return autenticacionRecibida.contentEquals(autenticacionActual);
    }

}