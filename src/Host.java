import java.net.InetAddress;

public class Host {


    TablaEncaminamiento tablaEnc;
    private InetAddress ip;
    private int puerto = 5512;

    public Host(InetAddress ip, int puerto) {
        this.ip = ip;
        this.puerto = puerto;
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
