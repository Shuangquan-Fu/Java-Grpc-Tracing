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
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.contrib.grpc.TracingClientInterceptor;
import io.opentracing.contrib.grpc.TracingServerInterceptor;

import java.io.IOException;

public class HServer1 {

    private Server server;
    private int port;
    private JaegerTracer tracer;

    private static final String host="127.0.0.1";
    private static final int ip1 = 50502;


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

        tracer =  Trancing.init("SayAgain");
        TracingServerInterceptor tracingInterceptor = TracingServerInterceptor
                .newBuilder()
                .withTracer(this.tracer)
                .build();
        server = ServerBuilder.forPort(ip1)
                .addService(tracingInterceptor.intercept(new service()))
                .build().start();
        System.out.println("服务开始启动-------");
    }
    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
    private class service extends SayAgainGrpc.SayAgainImplBase {

        @Override
        public void say(SayRequest request, StreamObserver<SayReply> responseObserver) {
            SayReply.Builder builder = SayReply.newBuilder();
            builder.setMessage("Hi" + request.getName());
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }
    }
    private void  blockUntilShutdown() throws InterruptedException {
        if (server!=null){
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final  HServer1 server = new HServer1();
        server.start();
        server.blockUntilShutdown();
    }
}
