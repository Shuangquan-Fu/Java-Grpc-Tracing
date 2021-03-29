
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.examples.test.SayAgainGrpc;
import io.grpc.examples.test.SayReply;
import io.grpc.examples.test.SayRequest;
import io.grpc.stub.StreamObserver;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.grpc.OpenTracingContextKey;
import io.opentracing.contrib.grpc.TracingClientInterceptor;
import io.opentracing.contrib.grpc.TracingServerInterceptor;
import io.opentracing.util.GlobalTracer;

import java.io.IOException;
public class HServer {
    private final int port = 50001;
    private Server server;
    private Tracer tracer  =  Trancing.init("GetHellow");;
    private final static int port1 = 50001;

    private final static int port_server = 50502;
    private final ManagedChannel channel1;
    private final SayAgainGrpc.SayAgainBlockingStub blockingStub1;


    private static final String host="127.0.0.1";


    HServer(String host, int port) {
        channel1= ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        //存根

        //TracingClientInterceptor tracingInterceptor = TracingClientInterceptor.newBuilder().withTracer(tracer).build();


        TracingClientInterceptor tracingInterceptor = TracingClientInterceptor
                .newBuilder()
                .withTracer(this.tracer)
                .build();
        blockingStub1 = SayAgainGrpc.newBlockingStub(tracingInterceptor.intercept(channel1));
    }


    private void start() throws IOException {
//        Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv()
//                .withType(ConstSampler.TYPE)
//                .withParam(1);
//
//        Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv()
//                .withLogSpans(true);
//
//        Configuration config = new Configuration("server")
//                .withSampler(samplerConfig)
//                .withReporter(reporterConfig);
//
//        GlobalTracer.register(config.getTracer());
//
//        TracingServerInterceptor tracingInterceptor = TracingServerInterceptor.newBuilder().withTracer(GlobalTracer.get()).build();


        TracingServerInterceptor tracingInterceptor = TracingServerInterceptor
                .newBuilder()
                .withTracer(this.tracer)
                .build();
        server = ServerBuilder.forPort(50501)
                .addService(tracingInterceptor.intercept(new service()))
                .build().start();
        System.out.println("服务开始启动-------");
    }
    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
    private class service extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {

            HelloReply.Builder builder = HelloReply.newBuilder();

            //request
           // HelloReply req = blockingStub1.sayHello(request);

            builder.setMessage(request.getName() + test(request.getName()));
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();


        }
    }
    private String test(String name) {

        SayRequest request = SayRequest.newBuilder().setName(name).build();
        SayReply rp = blockingStub1.say(request);
        return rp.getMessage();

    }


    private void  blockUntilShutdown() throws InterruptedException {
        if (server!=null){
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final  HServer server = new HServer(host, 50502);
        server.start();
        server.blockUntilShutdown();
    }
}
