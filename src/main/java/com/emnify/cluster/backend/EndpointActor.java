package com.emnify.cluster.backend;

import com.emnify.cluster.messages.ClusterManagement.QueryById;
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
      queryEp(message.getEndpointId());
      getSender().tell(new QueryResult(ep), getSelf());
    }).matchEquals(ReceiveTimeout.getInstance(), msg -> passivate())
        .matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }

  private Endpoint queryEp(Long id) {
    if (ep == null) {
      ep = new Endpoint(id, "epName" + id, "112201234567890" + id, "111111" + id, "10.0.0.1",
        id + 1L, id + 2L);
    }
    return ep;
  }

  private void passivate() {
    getContext().getParent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
  }

  public static Props props() {
    return Props.create(EndpointActor.class, () -> new EndpointActor());
  }
}
