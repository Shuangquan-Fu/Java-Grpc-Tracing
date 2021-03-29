import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.examples.test.SayAgainGrpc;
import io.grpc.examples.test.SayReply;
import io.grpc.examples.test.SayRequest;
import io.opentracing.Tracer;
import io.opentracing.contrib.grpc.TracingClientInterceptor;

import java.util.concurrent.TimeUnit;

public class Client1 {

    private final ManagedChannel channel1;
    private final SayAgainGrpc.SayAgainBlockingStub blockingStub1;


    private static final String host="127.0.0.1";
    private static final int ip1 = 50502;

    private final Tracer tracer;

    public Client1(String host, int port1) {


        channel1= ManagedChannelBuilder.forAddress(host, port1).usePlaintext().build();
        //存根

        //TracingClientInterceptor tracingInterceptor = TracingClientInterceptor.newBuilder().withTracer(tracer).build();

        tracer =  Trancing.init("client1");
        TracingClientInterceptor tracingInterceptor = TracingClientInterceptor
                .newBuilder()
                .withTracer(this.tracer)
                .build();
        blockingStub1 = SayAgainGrpc.newBlockingStub(tracingInterceptor.intercept(channel1));

        //存根



    }
    public void shutdown() throws InterruptedException {
        channel1.shutdown().awaitTermination(5, TimeUnit.SECONDS);

    }
    public String test(String name){

        SayRequest req = SayRequest.newBuilder().setName(name).build();



        SayReply helloReply1 = blockingStub1.say(req);
        System.out.println(helloReply1.getMessage());

return  helloReply1.getMessage();

    }

    public static void main(String[] args) {

        Client1 c = new Client1( host, ip1);
        c.test("res>>>"+"sdsd");





    }
}
