package com.emnify.cluster.backend;

import com.emnify.cluster.messages.DistributedDataManagement.UpdateImsiMapping;
import com.emnify.cluster.messages.DistributedDataManagement.UpdateMsisdnMapping;

import com.emnify.cluster.messages.ClusterManagement.QueryById;
import com.emnify.cluster.messages.ClusterManagement.QueryByImsi;
import com.emnify.cluster.messages.ClusterManagement.QueryByMsisdn;
import com.emnify.cluster.messages.ClusterManagement.QueryResult;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
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
  private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private final Duration INACTIVITY_TIMEOUT = Duration.create(120, TimeUnit.SECONDS);
  private final ActorRef ddata;
  private Endpoint ep;
  private final Long port =
      getContext().system().settings().config().getLong("akka.remote.netty.tcp.port");

  public EndpointActor(ActorRef ddata) {
    log.info("EndpointActor started {}", self().path().toString());
    this.ddata = ddata;
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
      queryImsiReply(message.getImsi());
    }).match(QueryByMsisdn.class, message -> {
      log.info("BE {}: QueryByMsisdn for msisdn {}", port, message.getMsisdn());
      queryMsisdnReply(message.getMsisdn());
    }).matchEquals(ReceiveTimeout.getInstance(), msg -> passivate())
        .matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }

  private void queryReply(Long id) {
    if (ep == null) {
      String imsi = "01234567890" + id;
      String msisdn = "1111" + id;
      ep = new Endpoint(id, "epName" + id, imsi, msisdn, "ip", id % 10, id % 12);
      updateMappings(id, imsi, msisdn);
    }
    getSender().tell(new QueryResult(ep), getSelf());
  }

  private void queryImsiReply(String imsi) {
    if (ep == null) {
      Long id = new Long(imsi.substring(10));
      String msisdn = "1111" + id;
      ep = new Endpoint(id, "epName", imsi, msisdn, "ip", 1L, 2L);
      updateMappings(id, imsi, msisdn);
    }
    getSender().tell(new QueryResult(ep), getSelf());
  }

  private void queryMsisdnReply(String msisdn) {
    if (ep == null) {
      Long id = new Long(msisdn.substring(4));
      String imsi = "01234567890" + id;
      ep = new Endpoint(id, "epName", imsi, "msisdn", "ip", 1L, 2L);
      updateMappings(id, imsi, msisdn);
    }
    getSender().tell(new QueryResult(ep), getSelf());
  }

  private void updateMappings(Long id, String imsi, String msisdn) {
    ddata.tell(new UpdateImsiMapping(imsi, id), self());
    ddata.tell(new UpdateMsisdnMapping(msisdn, id), self());
  }

  private void passivate() {
    getContext().getParent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
  }

  public static Props props(ActorRef ddata) {
    return Props.create(EndpointActor.class, () -> new EndpointActor(ddata));
  }
}
