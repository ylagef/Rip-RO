import java.net.InetAddress;

public class Host {


    private InetAddress IP;
    private int puerto = 5512;

    public Host(InetAddress IP, int puerto) {
        this.IP = IP;
        this.puerto = puerto;
    }

    public InetAddress getIP() {
        return IP;
    }

    public void setIP(InetAddress iP) {
        IP = iP;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }


}
