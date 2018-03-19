package demo.akka.frontend;

import static akka.pattern.Patterns.ask;

import demo.akka.messages.TransformationMessages.TransformationJob;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.OnSuccess;
import akka.util.Timeout;
import org.slf4j.MDC;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FrontendApp {
  private static ActorSystem system;

  public static void main(String[] args) {

    final Config config = ConfigFactory.parseString("akka.cluster.roles = [frontend]")
        .withFallback(ConfigFactory.load());

    //TODO: check why logback does not pick it up and use it in the logging pattern
    MDC.put("node", "frontend");
    MDC.put("port", config.getString("akka.remote.netty.tcp.port"));

    system = ActorSystem.create("ClusterSystem", config.resolve());
    system.actorOf(Props.create(ProfileSupervisor.class), "profiles");

    // disabled to focus on sharding: transformationMessaging();
  }

  private static void transformationMessaging() {
    final ActorRef frontend = system.actorOf(
        Props.create(Frontend.class), "frontend");
    final FiniteDuration interval = Duration.create(2, TimeUnit.SECONDS);
    final Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
    final ExecutionContext ec = system.dispatcher();
    final AtomicInteger counter = new AtomicInteger();
    system.scheduler().schedule(interval, interval, new Runnable() {
      @SuppressWarnings("deprecation")
      public void run() {
        TransformationJob msg = new TransformationJob("hello-" + counter.incrementAndGet());
        ask(frontend, msg, timeout)
            .onSuccess(new OnSuccess<Object>() {
              public void onSuccess(Object result) {
                System.out.println(result);
              }
            }, ec
        );
      }

    }, ec);
  }

}
