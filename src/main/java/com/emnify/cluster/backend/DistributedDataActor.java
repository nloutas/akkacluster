package com.emnify.cluster.backend;

import com.emnify.cluster.messages.DistributedDataManagement.TranslateImsi;
import com.emnify.cluster.messages.DistributedDataManagement.TranslateMsisdn;
import com.emnify.cluster.messages.DistributedDataManagement.TranslateResult;

import com.emnify.cluster.messages.DistributedDataManagement.UpdateImsiMapping;
import com.emnify.cluster.messages.DistributedDataManagement.UpdateMsisdnMapping;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ddata.DistributedData;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.LWWMap;
import akka.cluster.ddata.LWWMapKey;
import akka.cluster.ddata.Replicator.WriteConsistency;
import akka.cluster.ddata.Replicator.WriteMajority;
import akka.cluster.ddata.Replicator.Changed;
import akka.cluster.ddata.Replicator.Subscribe;
import akka.cluster.ddata.Replicator.Update;
import akka.cluster.ddata.Replicator.UpdateResponse;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;

import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Distributed Data Actor
 *
 */
public class DistributedDataActor extends AbstractActor {
  private final ActorSystem system = getContext().system();
  private final LoggingAdapter log = Logging.getLogger(system, this);
  private final Cluster node = Cluster.get(system);

  private final ActorRef replicator = DistributedData.get(system).replicator();
  private final WriteConsistency writeMajority =
      new WriteMajority(Duration.create(3, TimeUnit.SECONDS), 2);
  // private final static ReadConsistency readMajority =
  // new ReadMajority(Duration.create(3, TimeUnit.SECONDS), 2);

  private final Key<LWWMap<String, Long>> imsi2idKey = LWWMapKey.create("imsi2id");
  private final Key<LWWMap<String, Long>> msisdn2idKey = LWWMapKey.create("msisdn2id");

  private LWWMap<String, Long> imsi2idMap = LWWMap.create();
  private LWWMap<String, Long> msisdn2idMap = LWWMap.create();

  // private final Long port = system.settings().config().getLong("akka.remote.netty.tcp.port");

  public DistributedDataActor() {
  }
  @Override
  public void preStart() {
    replicator.tell(new Subscribe<>(imsi2idKey, self()), ActorRef.noSender());
    replicator.tell(new Subscribe<>(msisdn2idKey, self()), ActorRef.noSender());

  }

  @SuppressWarnings("unchecked")
  @Override
  public Receive createReceive() {
    return receiveBuilder() 
        .match(TranslateImsi.class, msg -> translateImsi(msg))
        .match(TranslateMsisdn.class, msg -> translateMsisdn(msg))
        .match(UpdateImsiMapping.class, msg -> updateImsiMapping(msg))
        .match(UpdateMsisdnMapping.class, msg -> updateMsisdnMapping(msg))
        .match(Changed.class, c -> c.key().equals(imsi2idKey),
            c -> receiveImsiChanged((Changed<LWWMap<String, Long>>) c))
        .match(Changed.class, c -> c.key().equals(msisdn2idKey),
            c -> receiveMsisdnChanged((Changed<LWWMap<String, Long>>) c))
        .match(UpdateResponse.class, u -> {
        }).matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }

  private void translateImsi(TranslateImsi msg) {
    // use local - fastest
    Long id = imsi2idMap.get(msg.getImsi()).get();
    // or ask the replicator?
    // replicator.tell(new Replicator.Get<LWWMap<String, Long>>(imsi2idKey, readMajority,
    // Optional.of(getSender())), getSelf());
    sender().tell(new TranslateResult(id), getSelf());
  }

  private void translateMsisdn(TranslateMsisdn msg) {
    // use local - fastest
    Long id = msisdn2idMap.get(msg.getMsisdn()).get();
    // or ask the replicator?
    // replicator.tell(new Replicator.Get<LWWMap<String, Long>>(imsi2idKey, readMajority,
    // Optional.of(getSender())), getSelf());
    sender().tell(new TranslateResult(id), getSelf());
  }

  private void updateImsiMapping(UpdateImsiMapping msg) {
    log.info("updateImsiMapping: imsi {} to id {}", msg.getImsi(), msg.getEndpointId());
    imsi2idMap.put(node, msg.getImsi(), msg.getEndpointId());

    Update<LWWMap<String, Long>> update = new Update<LWWMap<String, Long>>(imsi2idKey,
        imsi2idMap, writeMajority, curr -> curr.put(node, msg.getImsi(), msg.getEndpointId()));
    replicator.tell(update, self());
  }

  private void updateMsisdnMapping(UpdateMsisdnMapping msg) {
    log.info("updateMsisdnMapping: msisdn {} to id {}", msg.getMsisdn(), msg.getEndpointId());
    msisdn2idMap.put(node, msg.getMsisdn(), msg.getEndpointId());

    Update<LWWMap<String, Long>> update =
        new Update<LWWMap<String, Long>>(msisdn2idKey, msisdn2idMap, writeMajority,
            curr -> curr.put(node, msg.getMsisdn(), msg.getEndpointId()));
    replicator.tell(update, self());
  }

  private void receiveImsiChanged(Changed<LWWMap<String, Long>> c) {
    log.info("receiveImsiChanged: {} entries", c.dataValue().getEntries().size());
    for (Map.Entry<String, Long> entry : c.dataValue().getEntries().entrySet()) {
      imsi2idMap.put(node, entry.getKey(), entry.getValue());
    }
  }

  private void receiveMsisdnChanged(Changed<LWWMap<String, Long>> c) {
    log.info("receiveMsisdnChanged: {} entries", c.dataValue().getEntries().size());
    for (Map.Entry<String, Long> entry : c.dataValue().getEntries().entrySet()) {
      msisdn2idMap.put(node, entry.getKey(), entry.getValue());
    }
  }


  public static Props props() {
    return Props.create(DistributedDataActor.class, () -> new DistributedDataActor());
  }
}
