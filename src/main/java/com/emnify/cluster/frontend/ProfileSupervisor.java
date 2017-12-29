package com.emnify.cluster.frontend;

import com.emnify.cluster.messages.ClusterManagement;
import com.emnify.cluster.messages.ClusterManagement.QueryById;
import com.emnify.cluster.messages.ClusterManagement.QueryByImsi;
import com.emnify.cluster.messages.ClusterManagement.QueryByMsisdn;
import com.emnify.cluster.messages.ClusterManagement.EntityEnvelope;
import com.emnify.cluster.messages.ClusterManagement.QueryResult;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.sharding.ClusterSharding;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import data.Endpoint;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Supervisor for endpoint profiles
 *
 */
public class ProfileSupervisor extends AbstractActor {
  private final ActorSystem system = getContext().system();
  private final LoggingAdapter log = Logging.getLogger(system, this);
  private final Cluster cluster = Cluster.get(system);


  private final ActorRef backendRoutingActor =
      getContext().actorOf(Props.create(BackendRoutingActor.class), "backendRoutingActor");
  
  private final ConcurrentHashMap<Long, Long> performance = new ConcurrentHashMap<Long, Long>();
  private final FiniteDuration INITIAL = Duration.create(10, TimeUnit.SECONDS);
  private final FiniteDuration INTERVAL = Duration.create(2, TimeUnit.SECONDS);
  private final int MSG_PER_INTERVAL = 30;
  private Long epId = 0L;
  
  // TODO https://doc.akka.io/docs/akka/2.5.8/cluster-sharding.html#proxy-only-mode
  // private final ActorRef epShardRegionProxy;

  public ProfileSupervisor() {
    // this.epShardRegionProxy = ClusterSharding.get(system).startProxy("Endpoint",
    // Optional.of("frontend"), ClusterManagement.MESSAGE_EXTRACTOR);
  }

  @Override
  public void preStart() {
    cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), ClusterEvent.MemberUp.class);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(QueryById.class, message -> {
      log.info("QueryById for id {}", message.getEndpointId());
      // epShardRegionProxy.forward(message, getContext());
      backendRoutingActor.forward(message, getContext());
    }).match(QueryResult.class, message -> {
      long epId = message.getEp().getId();
      long t = System.currentTimeMillis() - performance.getOrDefault(epId, 0L);
      log.info("QueryResult for Endpoint id {} after {} ms ", epId, t);
      getProfileActor(message.getEp());
    }).match(ClusterEvent.MemberUp.class, message -> {
      Member member = message.member();
      if (member.hasRole("frontend")) {
        // we joined the cluster, send Query messages
        scheduleQueries();
        cluster.unsubscribe(getSelf(), ClusterEvent.MemberUp.class);
      }
    }).match(ReceiveTimeout.class, message -> {
      log.info("ReceiveTimeout");
      // TOOD handle timeout
    }).matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }

  private ActorRef getProfileActor(Endpoint ep) {
    return getContext().actorOf(ProfileActor.props(ep));
  }

  private void scheduleQueries() {

    system.scheduler().schedule(INITIAL, INTERVAL, new Runnable() {
      @Override
      public void run() {
        // send queries
        for (int i = 0; i < MSG_PER_INTERVAL; i++) {
          epId++;
          performance.put(epId, System.currentTimeMillis());
          backendRoutingActor.tell(new QueryById(epId), getSelf());
          backendRoutingActor.tell(new EntityEnvelope(epId, new QueryByImsi("01234567890" + epId)),
              getSelf());
          backendRoutingActor.tell(new EntityEnvelope(epId, new QueryByMsisdn("1111" + epId)),
              getSelf());
          getContext().setReceiveTimeout(Duration.create(5, TimeUnit.SECONDS));
        }
      }
    }, getContext().dispatcher());
  }

  public static Props props() {
    return Props.create(ProfileSupervisor.class, () -> new ProfileSupervisor());
  }
}
