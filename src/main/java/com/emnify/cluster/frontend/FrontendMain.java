package com.emnify.cluster.frontend;

import static akka.pattern.Patterns.ask;

import com.emnify.cluster.messages.TransformationMessages.TransformationJob;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.OnSuccess;
import akka.util.Timeout;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FrontendMain {
  private static ActorSystem system;

  public static void main(String[] args) {
    // Override the configuration of the port when specified as program argument
    final String port = args.length > 0 ? args[0] : "0";
    final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]")).
      withFallback(ConfigFactory.load());

    system = ActorSystem.create("ClusterSystem", config);
    system.actorOf(Props.create(ProfileSupervisor.class), "profiles");

    // transformationMessaging();
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
