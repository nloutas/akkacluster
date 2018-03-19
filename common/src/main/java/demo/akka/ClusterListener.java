package demo.akka;

import akka.actor.AbstractActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Listener class to monitor the akka cluster for members joining/leaving
 *
 */
public class ClusterListener extends AbstractActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  Cluster cluster = Cluster.get(getContext().system());

  // subscribe to cluster changes
  @Override
  public void preStart() {
    cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class,
        UnreachableMember.class);
  }

  // unsubscribe when stopping
  @Override
  public void postStop() {
    cluster.unsubscribe(getSelf());
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(UnreachableMember.class, message -> {
      log.info("Member detected as unreachable: {}", message.member());
    }).match(MemberRemoved.class, message -> {
      log.info("Member is Removed: {}", message.member());

    }).match(MemberEvent.class, message -> {
      // ignore
    }).match(MemberUp.class, message -> {
      log.info("Member is Up: {}", message.member());
    }).matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }

}
