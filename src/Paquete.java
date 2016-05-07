import java.nio.ByteBuffer;

/**
 * Created by Yeray on 07/05/2016.
 */
public class Paquete {

    //                      Para RIPv2 hay un formato de paquete que es:
    // Comando | Numero Version | No usado (0) | AFI | Route TAG | Destino | Mask | Por donde | Distancia
    //       1                1              2     2           2         4      4           4           4
    //                                  En Bytes (octetos)

    private ByteBuffer datos;

    Paquete(Comando c, int tableSize) {

        //Como es el constructor lo que haremos es inicializar el buffer e introducirle su cabecera.

        datos = ByteBuffer.allocate(4 + 20 * (tableSize + 1)); //Esto es necesario ya que hay que reservar el espacio.

        //Cabecera del paquete (ocupa 4 bytes).
        datos.put((byte) c.valor); //Tipo de comando
        datos.put((byte) 2); //Version RIP
        datos.put((byte) 0); //Se pone siempre a 0
        datos.put((byte) 0); //Siempre a 0

    }

    void addEncaminamiento(Encaminamiento e) {
        //Hay que añadir cada encaminamiento seguido del anterior en el buffer.


    }

}
