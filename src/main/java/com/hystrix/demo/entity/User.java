package com.hystrix.demo.entity;

/**
 * @author chenlong
 * Created on 2018/6/29
 */
public class User {

  private String userId;

  private String userName;

  private String countryCode;

  private Boolean isFeatureXPermitted;

  private Boolean isFeatureYPermitted;

  private Boolean isFeatureZPermitted;

  public User(String userId, String userName, String countryCode, Boolean isFeatureXPermitted, Boolean isFeatureYPermitted, Boolean isFeatureZPermitted) {
    this.userId = userId;
    this.userName = userName;
    this.countryCode = countryCode;
    this.isFeatureXPermitted = isFeatureXPermitted;
    this.isFeatureYPermitted = isFeatureYPermitted;
    this.isFeatureZPermitted = isFeatureZPermitted;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public Boolean getFeatureXPermitted() {
    return isFeatureXPermitted;
  }

  public void setFeatureXPermitted(Boolean featureXPermitted) {
    isFeatureXPermitted = featureXPermitted;
  }

  public Boolean getFeatureYPermitted() {
    return isFeatureYPermitted;
  }

  public void setFeatureYPermitted(Boolean featureYPermitted) {
    isFeatureYPermitted = featureYPermitted;
  }

  public Boolean getFeatureZPermitted() {
    return isFeatureZPermitted;
  }

  public void setFeatureZPermitted(Boolean featureZPermitted) {
    isFeatureZPermitted = featureZPermitted;
  }
}
