package com.emnify.cluster.messages;


import akka.cluster.sharding.ShardRegion;

import java.io.Serializable;
import java.util.Optional;

/**
 * Cluster Management
 *
 */
public interface ClusterManagement extends Serializable {

  public static class EntityEnvelope implements ClusterManagement {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    final public long id;
    final public Object payload;

    public EntityEnvelope(long id, Object payload) {
      this.id = id;
      this.payload = payload;
    }
  }

  /**
   * Message to retrieve a single EP
   */
  public static class QueryById implements ClusterManagement {
    private static final long serialVersionUID = 1L;
    private final Serializable queryId;
    private final Long endpointId;

    /**
     * @param queryId Serializable
     * @param endpointId Long
     */
    public QueryById(Serializable queryId, Long endpointId) {
      this.queryId = queryId;
      this.endpointId = endpointId;
    }

    /**
     * @param endpointId Long
     */
    public QueryById(Long endpointId) {
      this(null, endpointId);
    }

    /**
     * @return Optional of queryId
     */
    public Optional<Serializable> getQueryId() {
      return Optional.ofNullable(queryId);
    }

    /**
     * @return endpointId Long
     */
    public Long getEndpointId() {
      return endpointId;
    }

  }

  /**
   * Message for the result
   */
  public static class QueryResult implements ClusterManagement {
    private static final long serialVersionUID = 1L;

    private final Serializable queryId;
    private final String ep;

    /**
     * Constructor for result
     *
     * @param queryId Serializable
     * @param ep String
     */
    public QueryResult(Serializable queryId, String ep) {
      this.queryId = queryId;
      this.ep = ep;
    }



    /**
     * Constructor for successful retrieval
     *
     * @param ep String
     */
    public QueryResult(String ep) {
      this(null, ep);
    }


    /**
     * @return Serializable queryId
     */
    public Optional<Serializable> getQueryId() {
      return Optional.ofNullable(queryId);
    }

    /**
     * @return Ep String
     */
    public String getEp() {
      return ep;
    }

  }


  static final ShardRegion.MessageExtractor MESSAGE_EXTRACTOR = new ShardRegion.MessageExtractor() {

    @Override
    public String entityId(Object message) {
      if (message instanceof EntityEnvelope)
        return String.valueOf(((EntityEnvelope) message).id);
      else if (message instanceof QueryById)
        return String.valueOf(((QueryById) message).getEndpointId());
      else
        return null;
    }

    @Override
    public Object entityMessage(Object message) {
      if (message instanceof EntityEnvelope)
        return ((EntityEnvelope) message).payload;
      else
        return message;
    }

    @Override
    public String shardId(Object message) {
      int numberOfShards = 10;
      if (message instanceof EntityEnvelope) {
        long id = ((EntityEnvelope) message).id;
        return String.valueOf(id % numberOfShards);
      } else if (message instanceof QueryById) {
        long id = ((QueryById) message).getEndpointId();
        return String.valueOf(id % numberOfShards);
      } else {
        return null;
      }
    }
  };

}
