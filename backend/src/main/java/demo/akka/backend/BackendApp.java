package demo.akka.backend;

import demo.akka.messages.ClusterManagement;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import org.slf4j.MDC;

public class BackendApp {

  public static void main(String[] args) {

    final Config config = ConfigFactory.parseString("akka.cluster.roles = [backend,sharding]")
        .withFallback(ConfigFactory.load());

    //TODO: check why logback does not pick it up and use it in the logging pattern
    MDC.put("node", "backend");
    MDC.put("port", config.getString("akka.remote.netty.tcp.port"));

    ActorSystem system = ActorSystem.create("ClusterSystem", config.resolve());
    system.actorOf(Props.create(Backend.class), "backend");

    // register Endpoint type in ClusterSharding Region
    ClusterShardingSettings settings = ClusterShardingSettings.create(system);
    final ActorRef epShardingRegion = ClusterSharding.get(system).start("Endpoint",
        Props.create(EndpointActor.class), settings, ClusterManagement.MESSAGE_EXTRACTOR);

    // start top actor(s)
    system.actorOf(Props.create(EndpointSupervisor.class, epShardingRegion), "endpoints");
  }
}
