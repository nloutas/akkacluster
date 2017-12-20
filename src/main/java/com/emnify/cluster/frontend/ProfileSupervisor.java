package com.emnify.cluster.frontend;

import com.emnify.cluster.messages.ClusterManagement.QueryById;
import com.emnify.cluster.messages.ClusterManagement.QueryResult;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import data.Endpoint;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

/**
 * Supervisor for endpoint profiles
 *
 */
public class ProfileSupervisor extends AbstractActor {
  private final ActorSystem system = getContext().system();
  private final LoggingAdapter log = Logging.getLogger(system, this);
  private final Cluster cluster = Cluster.get(system);

  private final FiniteDuration INTERVAL = Duration.create(20, TimeUnit.SECONDS);
  private final ActorRef backendRoutingActor =
      getContext().actorOf(Props.create(BackendRoutingActor.class), "backendRoutingActor");
  // private final ActorRef epShardRegionProxy;

  public ProfileSupervisor() {
    // this.epShardRegionProxy = ClusterSharding.get(system).startProxy("Endpoint",
    // Optional.empty(), ClusterManagement.MESSAGE_EXTRACTOR);
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
      getProfileActor(message.getEp());
    }).match(ClusterEvent.MemberUp.class, message -> {
      Member member = message.member();
      if (member.hasRole("frontend")) {
        // we joined the cluster, send QueryById messages for 2 Ids
        sendQueryById(2551L);
        sendQueryById(2552L);
        cluster.unsubscribe(getSelf(), ClusterEvent.MemberUp.class);
      }
    }).matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }

  private ActorRef getProfileActor(Endpoint ep) {
    return getContext().actorOf(ProfileActor.props(ep));
  }

  private void sendQueryById(Long id) {

    system.scheduler().schedule(Duration.Zero(), INTERVAL, new Runnable() {
      @Override
      public void run() {
        backendRoutingActor.tell(new QueryById(id), getSelf());
        getContext().setReceiveTimeout(Duration.create(5, TimeUnit.SECONDS));
      }
    }, getContext().dispatcher());
  }

  public static Props props() {
    return Props.create(ProfileSupervisor.class, () -> new ProfileSupervisor());
  }
}
