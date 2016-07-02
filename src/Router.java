import java.net.InetAddress;

class Router {

    private InetAddress ip;
    private int puerto = 5512;

    Router(InetAddress ip, int puerto) {
        this.ip = ip;
        this.puerto = puerto;
    }

    Router(InetAddress ip) {
        this.ip = ip;
    }

    InetAddress getIp() {
        return ip;
    }

    int getPuerto() {
        return puerto;
    }

}