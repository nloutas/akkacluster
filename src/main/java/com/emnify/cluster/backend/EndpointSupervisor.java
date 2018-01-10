package com.emnify.cluster.backend;

import com.emnify.cluster.messages.ClusterManagement.EntityEnvelope;
import com.emnify.cluster.messages.ClusterManagement.QueryById;
import com.emnify.cluster.messages.ClusterManagement.QueryByImsi;
import com.emnify.cluster.messages.ClusterManagement.QueryByMsisdn;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.event.Logging;
import akka.event.LoggingAdapter;


/**
 * Endpoint Supervisor
 *
 */
public class EndpointSupervisor extends AbstractActor {
  private final ActorSystem system = getContext().system();
  private final LoggingAdapter log = Logging.getLogger(system, this);
  private final ActorRef ddata;
  private final Long port =
      system.settings().config().getLong("akka.remote.netty.tcp.port");

  public EndpointSupervisor(ActorRef ddata) {
    this.ddata = ddata;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(QueryById.class, message -> {
      log.info("BE {}: QueryById for id {}", port, message.getEndpointId());
      epRegion().forward(message, getContext());
    }).match(QueryByImsi.class, message -> {
      ddata.forward(message, getContext());
    }).match(QueryByMsisdn.class, message -> {
      ddata.forward(message, getContext());
    }).match(EntityEnvelope.class, message -> {
      log.info("BE {}: EntityEnvelope for id {}", port, message.id);
      epRegion().forward(message, getContext());
    }).matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }


  private ActorRef epRegion() {
    return ClusterSharding.get(system).shardRegion("Endpoint");
  }


  public static Props props(ActorRef ddata) {
    return Props.create(EndpointSupervisor.class, () -> new EndpointSupervisor(ddata));
  }
}
