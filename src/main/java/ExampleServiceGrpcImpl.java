import com.google.protobuf.Timestamp;
import example.*;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.util.Date;
import java.util.LinkedHashMap;

public class ExampleServiceGrpcImpl extends ExampleServiceGrpc.ExampleServiceImplBase {

    private static final LinkedHashMap<StreamObserver<ResponseCall>, String> observers = new LinkedHashMap<>();

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        //onCompleted only after onNext and onError anytime and finishes stream
        Timestamp t = Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build();

        responseObserver.onNext(HelloReply.newBuilder().setDeparture(t).setMessage("merge").build());
        //responseObserver.onError(new Exception("nu merge"));
        responseObserver.onCompleted();
    }

    @Override
    public void sayHellos(HelloRequest request, StreamObserver<HelloReplies> responseObserver) {
        var builder = HelloReplies.newBuilder();
        builder.addArray(HelloReply.newBuilder().setMessage(request.getName() + " 1").build());
        builder.addArray(HelloReply.newBuilder().setMessage(request.getName() + " 2").build());
        builder.addArray(HelloReply.newBuilder().setMessage(request.getName() + " 3").build());
        builder.addArray(HelloReply.newBuilder().setMessage(request.getName() + " 4").build());
        responseObserver.onNext(builder.build());
    }

    @Override
    public StreamObserver<RequestCall> connect(StreamObserver<ResponseCall> responseObserver) {
        System.out.println("Connecting stream observer");
        return new StreamObserver<>() {
            @Override
            public void onNext(RequestCall value) {
                if (value.getType().equals(Type.StartConnection) && !observers.containsKey(responseObserver)) {
                    System.out.println("started connection with " + value.getUserName());
                    observers.put(responseObserver, value.getUserName());
                } else {
                    System.out.println("onNext from server " + value.getMessage());
                    for (var observer : observers.keySet()) {
                        System.out.println(observers.get(observer));
                        if (!observers.get(observer).equals("ion"))
                            observer.onNext(ResponseCall.newBuilder().setRequestId(value.getMessage() + " response").build());
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("on error");
                t.printStackTrace();
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                System.out.println("on completed");
                observers.remove(responseObserver);
                responseObserver.onCompleted();
            }
        };
    }
}
