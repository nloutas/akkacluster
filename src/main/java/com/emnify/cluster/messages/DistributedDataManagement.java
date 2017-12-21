package com.emnify.cluster.messages;

import java.io.Serializable;
import java.util.Optional;

/**
 * AKka DistributedData Management
 *
 */
public interface DistributedDataManagement extends Serializable {

  /**
   * Message to update IMSI mapping
   */
  public static class UpdateImsiMapping implements DistributedDataManagement {
    private static final long serialVersionUID = 1L;
    private final Serializable queryId;
    private final String imsi;
    private final Long endpointId;

    /**
     * @param queryId Serializable
     * @param imsi String
     * @param endpointId Long
     */
    public UpdateImsiMapping(Serializable queryId, String imsi, Long endpointId) {
      this.queryId = queryId;
      this.imsi = imsi;
      this.endpointId = endpointId;
    }

    /**
     * @param imsi String
     * @param endpointId Long
     */
    public UpdateImsiMapping(String imsi, Long endpointId) {
      this(null, imsi, endpointId);
    }

    /**
     * @return Optional of queryId
     */
    public Optional<Serializable> getQueryId() {
      return Optional.ofNullable(queryId);
    }

    /**
     * @return imsi String
     */
    public String getImsi() {
      return imsi;
    }

    /**
     * @return endpointId Long
     */
    public Long getEndpointId() {
      return endpointId;
    }
  }

  /**
   * Message to update Msisdn mapping
   */
  public static class UpdateMsisdnMapping implements DistributedDataManagement {
    private static final long serialVersionUID = 1L;
    private final Serializable queryId;
    private final String msisdn;
    private final Long endpointId;

    /**
     * @param queryId Serializable
     * @param msisdn String
     * @param endpointId Long
     */
    public UpdateMsisdnMapping(Serializable queryId, String msisdn, Long endpointId) {
      this.queryId = queryId;
      this.msisdn = msisdn;
      this.endpointId = endpointId;
    }

    /**
     * @param msisdn String
     * @param endpointId Long
     */
    public UpdateMsisdnMapping(String msisdn, Long endpointId) {
      this(null, msisdn, endpointId);
    }

    /**
     * @return Optional of queryId
     */
    public Optional<Serializable> getQueryId() {
      return Optional.ofNullable(queryId);
    }

    /**
     * @return msisdn String
     */
    public String getMsisdn() {
      return msisdn;
    }

    /**
     * @return endpointId Long
     */
    public Long getEndpointId() {
      return endpointId;
    }
  }
  /**
   * Message to update IP address mapping
   */
  public static class UpdateIpMapping implements DistributedDataManagement {
    private static final long serialVersionUID = 1L;
    private final Serializable queryId;
    private final String ip;
    private final Long endpointId;

    /**
     * @param queryId Serializable
     * @param ip String
     * @param endpointId Long
     */
    public UpdateIpMapping(Serializable queryId, String ip, Long endpointId) {
      this.queryId = queryId;
      this.ip = ip;
      this.endpointId = endpointId;
    }

    /**
     * @param ip String
     * @param endpointId Long
     */
    public UpdateIpMapping(String ip, Long endpointId) {
      this(null, ip, endpointId);
    }

    /**
     * @return Optional of queryId
     */
    public Optional<Serializable> getQueryId() {
      return Optional.ofNullable(queryId);
    }

    /**
     * @return ip String
     */
    public String getIp() {
      return ip;
    }

    /**
     * @return endpointId Long
     */
    public Long getEndpointId() {
      return endpointId;
    }
  }


  /**
   * Message to translate IMSI to Id
   */
  public static class TranslateImsi implements DistributedDataManagement {
    private static final long serialVersionUID = 1L;
    private final Serializable queryId;
    private final String imsi;

    /**
     * @param queryId Serializable
     * @param imsi String
     */
    public TranslateImsi(Serializable queryId, String imsi) {
      this.queryId = queryId;
      this.imsi = imsi;
    }

    /**
     * @param imsi String
     */
    public TranslateImsi(String imsi) {
      this(null, imsi);
    }

    /**
     * @return Optional of queryId
     */
    public Optional<Serializable> getQueryId() {
      return Optional.ofNullable(queryId);
    }

    /**
     * @return imsi String
     */
    public String getImsi() {
      return imsi;
    }
  }

  /**
   * Message to translate Msisdn to Id
   */
  public static class TranslateMsisdn implements DistributedDataManagement {
    private static final long serialVersionUID = 1L;
    private final Serializable queryId;
    private final String msisdn;

    /**
     * @param queryId Serializable
     * @param msisdn String
     */
    public TranslateMsisdn(Serializable queryId, String msisdn) {
      this.queryId = queryId;
      this.msisdn = msisdn;
    }

    /**
     * @param msisdn String
     */
    public TranslateMsisdn(String msisdn) {
      this(null, msisdn);
    }

    /**
     * @return Optional of queryId
     */
    public Optional<Serializable> getQueryId() {
      return Optional.ofNullable(queryId);
    }

    /**
     * @return msisdn String
     */
    public String getMsisdn() {
      return msisdn;
    }
  }

  /**
   * Message for translation result
   */
  public static class TranslateResult implements DistributedDataManagement {
    private static final long serialVersionUID = 1L;
    private final Serializable queryId;
    private final Long endpointId;

    /**
     * @param queryId Serializable
     * @param endpointId Long
     */
    public TranslateResult(Serializable queryId, Long endpointId) {
      this.queryId = queryId;
      this.endpointId = endpointId;
    }

    /**
     * @param endpointId Long
     */
    public TranslateResult(Long endpointId) {
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

}

