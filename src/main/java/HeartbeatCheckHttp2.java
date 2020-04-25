import java.net.InetSocketAddress;
import java.net.Socket;

public class HeartbeatCheckHttp2 {
    public static void main(String[] args) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", 50000), 5000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
