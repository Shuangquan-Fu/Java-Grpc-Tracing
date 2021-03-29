import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.grpc.TracingClientInterceptor;
import io.opentracing.util.GlobalTracer;

import java.util.concurrent.TimeUnit;

public class Client {
    private final ManagedChannel channel1;
    private final GreeterGrpc.GreeterBlockingStub blockingStub1;


    private static final String host="127.0.0.1";
    private static final int ip1 = 50501;

    private final Tracer tracer;

    public Client(String host, int port1) {


        channel1 = ManagedChannelBuilder.forAddress(host, port1).usePlaintext().build();
        //存根

        //TracingClientInterceptor tracingInterceptor = TracingClientInterceptor.newBuilder().withTracer(tracer).build();

        tracer =  Trancing.init("client");
        TracingClientInterceptor tracingInterceptor = TracingClientInterceptor
                .newBuilder()
                .withTracer(this.tracer)
                .build();
        blockingStub1 = GreeterGrpc.newBlockingStub(tracingInterceptor.intercept(channel1));

        //存根



    }
    public void shutdown() throws InterruptedException {
        channel1.shutdown().awaitTermination(5, TimeUnit.SECONDS);

    }
    public void test(String name){

        HelloRequest req = HelloRequest.newBuilder().setName(name).build();
        HelloReply helloReply1 = blockingStub1.sayHello(req);
        System.out.println(helloReply1.getMessage());



    }

    public static void main(String[] args) {

        Client c = new Client( host, ip1);
        c.test("res>>>"+"sdsd");


    }
}
