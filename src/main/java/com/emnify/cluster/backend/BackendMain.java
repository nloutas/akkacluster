package com.emnify.cluster.backend;

import com.emnify.cluster.messages.ClusterManagement;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import org.slf4j.MDC;

public class BackendMain {

  public static void main(String[] args) {
    // Override the configuration of the port when specified as program argument
    final String port = args.length > 0 ? args[0] : "0";
    final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
        .withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend,sharding]"))
        .withFallback(ConfigFactory.load());

    //TODO: check why logback does not pick it up and use it in the logging pattern
    MDC.put("node", "backend");
    MDC.put("port", port);

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
