package com.emnify.cluster.backend;


import com.emnify.cluster.messages.ClusterManagement.QueryById;
import com.emnify.cluster.messages.ClusterManagement.QueryByImsi;
import com.emnify.cluster.messages.ClusterManagement.QueryResult;

import akka.actor.AbstractActor;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.cluster.sharding.ShardRegion;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import data.Endpoint;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * handle endpoint in the backend node
 *
 */
public class EndpointActor extends AbstractActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private final Duration INACTIVITY_TIMEOUT = Duration.create(120, TimeUnit.SECONDS);
  private Endpoint ep;
  private final Long port =
      getContext().system().settings().config().getLong("akka.remote.netty.tcp.port");

  public EndpointActor() {
    log.info("EndpointActor started {}", self().path().toString());
  }

  @Override
  public void preStart() throws Exception {
    super.preStart();
    getContext().setReceiveTimeout(INACTIVITY_TIMEOUT);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(QueryById.class, message -> {
      log.info("BE {}: QueryById for id {}", port, message.getEndpointId());
      queryReply(message.getEndpointId());
    }).match(QueryByImsi.class, message -> {
      log.info("BE {}: QueryByImsi for imsi {}", port, message.getImsi());
      queryReply(message.getImsi());
    }).matchEquals(ReceiveTimeout.getInstance(), msg -> passivate())
        .matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }

  private void queryReply(Long id) {
    if (ep == null) {
      ep = new Endpoint(id, "epName" + id, "imsi", "msisdn", "ip", id % 10, id % 12);
    }
    getSender().tell(new QueryResult(ep), getSelf());
  }

  private void queryReply(String imsi) {
    if (ep == null) {
      ep = new Endpoint(null, "epName", imsi, "msisdn", "ip", 1L, 2L);
    }
    getSender().tell(new QueryResult(ep), getSelf());
  }

  private void passivate() {
    getContext().getParent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
  }

  public static Props props() {
    return Props.create(EndpointActor.class, () -> new EndpointActor());
  }
}
