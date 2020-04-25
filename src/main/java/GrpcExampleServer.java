import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;

public class GrpcExampleServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = NettyServerBuilder.forAddress(new InetSocketAddress("localhost", 50002))
                .executor(MoreExecutors.directExecutor())
                .addService(new ExampleServiceGrpcImpl()).build()
                .start();


        System.out.println("Server has started");
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }
}
