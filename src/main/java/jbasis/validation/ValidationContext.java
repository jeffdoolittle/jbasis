package jbasis.validation;

import jbasis.ioc.ServiceFactory;

public final class ValidationContext {

  private final Object target;
  private final ServiceFactory serviceFactory;
  private String memberName;
  private String memberDisplayName;

  public ValidationContext(Object target, ServiceFactory serviceFactory) {
    this.target = target;
    this.serviceFactory = serviceFactory;
  }

  public String getMemberDisplayName() {
    return memberDisplayName;
  }

  public void setMemberDisplayName(String memberDisplayName) {
    this.memberDisplayName = memberDisplayName;
  }

  public String getMemberName() {
    return memberName;
  }

  public void setMemberName(String memberName) {
    this.memberName = memberName;
  }

  public Class<?> targetType() {
    return this.targetType().getClass();
  }

  public Object target() {
    return this.target;
  }

  public ServiceFactory serviceFactory() {
    return this.serviceFactory;
  }
}