import example.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class GrpcExampleClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50002).usePlaintext().build();
        ExampleServiceGrpc.ExampleServiceStub service = ExampleServiceGrpc.newStub(channel);

        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch observerLatch = new CountDownLatch(1);
        final String[] message = new String[1];

        final Throwable[] ex = {new Throwable()};
        service.sayHello(HelloRequest.newBuilder().setName("ion").build(), new StreamObserver<>() {
            @Override
            public void onNext(HelloReply value) {
                System.out.println(value.getMessage() + " on next hello " + Timestamp.from(Instant.ofEpochSecond(value.getDeparture().getSeconds())));
                message[0] = value.getMessage();
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Error()");
                t.printStackTrace();
                ex[0] = t;
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("on completed hello");
                latch.countDown();
            }
        });

        service.sayHellos(HelloRequest.newBuilder().setName("marcel").build(), new StreamObserver<>() {
            @Override
            public void onNext(HelloReplies value) {
                for (HelloReply helloReply : value.getArrayList()) {
                    System.out.println(helloReply.getMessage());
                }

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

        StreamObserver<RequestCall> observer = service.connect(new StreamObserver<>() {
            @Override
            public void onNext(ResponseCall value) {
                System.out.println(value.getRequestId());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("on error");
                ex[0] = t;

            }

            @Override
            public void onCompleted() {
                System.out.println("on completed");
                observerLatch.countDown();
            }
        });

        String name = "ion";

        observer.onNext(RequestCall.newBuilder().setType(Type.StartConnection).setUserName(name).build());

        latch.await();
        if (ex[0].getMessage() != null)
            System.out.println("message:" + ex[0].getMessage());
        System.out.println(message[0]);

        while (true) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String number = reader.readLine();

            //would be handled by login logout
            if (number.equals("0"))
                break;

            Random r = new Random();
            for (int i = 0; i < Integer.parseInt(number); i++)
                observer.onNext(RequestCall.newBuilder().setUserName(name).setMessage(Integer.toString(r.nextInt())).build());
        }

        observer.onCompleted();
    }
}
