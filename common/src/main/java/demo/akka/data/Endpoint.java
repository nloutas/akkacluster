package demo.akka.data;

import java.io.Serializable;

/**
 * Endpoint data class
 *
 */
public class Endpoint implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private Long id;
  private String name;
  private String imsi;
  private String msisdn;
  private String ip;
  private Long spId;
  private Long tpId;

  /**
   * @param id Long
   * @param name String
   * @param imsi String
   * @param msisdn String
   * @param ip String
   * @param spId Long
   * @param tpId Long
   */
  public Endpoint(Long id, String name, String imsi, String msisdn, String ip, Long spId,
      Long tpId) {
    this.id = id;
    this.name = name;
    this.imsi = imsi;
    this.msisdn = msisdn;
    this.ip = ip;
    this.spId = spId;
    this.tpId = tpId;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the imsi
   */
  public String getImsi() {
    return imsi;
  }

  /**
   * @param imsi the imsi to set
   */
  public void setImsi(String imsi) {
    this.imsi = imsi;
  }

  /**
   * @return the msisdn
   */
  public String getMsisdn() {
    return msisdn;
  }

  /**
   * @param msisdn the msisdn to set
   */
  public void setMsisdn(String msisdn) {
    this.msisdn = msisdn;
  }

  /**
   * @return the ip
   */
  public String getIp() {
    return ip;
  }

  /**
   * @param ip the ip to set
   */
  public void setIp(String ip) {
    this.ip = ip;
  }

  /**
   * @return the spId
   */
  public Long getSpId() {
    return spId;
  }

  /**
   * @param spId the spId to set
   */
  public void setSpId(Long spId) {
    this.spId = spId;
  }

  /**
   * @return the tpId
   */
  public Long getTpId() {
    return tpId;
  }

  /**
   * @param tpId the tpId to set
   */
  public void setTpId(Long tpId) {
    this.tpId = tpId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Endpoint [" + (id != null ? "id=" + id + ", " : "")
        + (name != null ? "name=" + name + ", " : "") + (imsi != null ? "imsi=" + imsi + ", " : "")
        + (msisdn != null ? "msisdn=" + msisdn + ", " : "") + (ip != null ? "ip=" + ip + ", " : "")
        + (spId != null ? "spId=" + spId + ", " : "") + (tpId != null ? "tpId=" + tpId : "") + "]";
  }

}
