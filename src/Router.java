import java.net.InetAddress;
import java.util.ArrayList;

public class Router {

    private ArrayList<InetAddress> listaVecinos;
    private TablaEncaminamiento tablaEnc;
    private InetAddress ip;
    private int puerto = 5512;

    public Router(InetAddress ip, int puerto) {
        this.ip = ip;
        this.puerto = puerto;
    }

    public Router(InetAddress ip) {
        this.ip = ip;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIP(InetAddress iP) {
        ip = ip;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

}
