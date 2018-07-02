/**
 * Copyright 2018 asiainfo Inc.
 **/
package com.hystrix.demo;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.Assert;

/**
 * @author chenlong
 * Created on 2018/6/29
 */
public class CommandFacdeWithPrimarySecondary extends HystrixCommand<String> {

  private final String name;

  private final static DynamicBooleanProperty primary = DynamicPropertyFactory.getInstance()
          .getBooleanProperty("primaryFlag", true);

  public CommandFacdeWithPrimarySecondary(String name) {
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("commandFPS"))
            .andCommandKey(HystrixCommandKey.Factory.asKey("FPSCommand"))
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                    .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)));
    this.name = name;
  }

  @Override
  protected String run() throws Exception {

    if (primary.get()){
      return new PrimaryCommand(name).execute();
    } else {
      return new SecondaryCommand(name).execute();
    }

  }

  @Override
  protected String getFallback() {
    return "fallback_" + name;
  }

  @Override
  protected String getCacheKey() {
    return name;
  }

  /**
   * PrimaryCommand.
   */
  private static class PrimaryCommand extends HystrixCommand<String>{

    private final String _name;

    public PrimaryCommand(String _name) {
      super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("primaryGroup"))
              .andCommandKey(HystrixCommandKey.Factory.asKey("primaryKey"))
              .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("primaryPool"))
              .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(600)));
      this._name = _name;
    }

    @Override
    protected String run() throws Exception {
      return "PrimaryCommand_name : " + _name;
    }
  }

  /**
   * SecondaryCommand.
   */
  private static class SecondaryCommand extends HystrixCommand<String>{

    private final String _name;

    public SecondaryCommand(String _name) {
      super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("secondaryGroup"))
              .andCommandKey(HystrixCommandKey.Factory.asKey("secondaryKey"))
              .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("secondaryPool"))
              .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                      .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)));
      this._name = _name;
    }

    @Override
    protected String run() throws Exception {
      return "SecondaryCommand_name : " + _name;
    }
  }

  /**
   * Test.
   */
  public static class Test{

    @org.junit.Test
    public void testPrimary(){

      HystrixRequestContext context = HystrixRequestContext.initializeContext();
      try {
        ConfigurationManager.getConfigInstance().setProperty("primaryFlag", true);
        Assert.assertEquals("PrimaryCommand_name : testPrimary",
                new CommandFacdeWithPrimarySecondary("testPrimary").execute());
      } finally {
        context.shutdown();
        ConfigurationManager.getConfigInstance().clear();
      }

    }

    @org.junit.Test
    public void testSecondary(){

      HystrixRequestContext context = HystrixRequestContext.initializeContext();

      try{
        ConfigurationManager.getConfigInstance().setProperty("primaryFlag", false);
        Assert.assertEquals("SecondaryCommand_name : testSecondary",
                new CommandFacdeWithPrimarySecondary("testSecondary").execute());
      } finally {
        context.shutdown();
        ConfigurationManager.getConfigInstance().clear();
      }



    }


  }




}
