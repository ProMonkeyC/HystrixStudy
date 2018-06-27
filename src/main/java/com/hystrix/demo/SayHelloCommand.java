/**
 * Copyright 2018 asiainfo Inc.
 **/
package com.hystrix.demo;


import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import org.junit.Test;

/**
 * @author chenlong
 * Created on 2018/6/8
 */
public class SayHelloCommand extends HystrixCommand<String>{

  private final String _name;

//  public SayHelloCommand(String _name) {
//    super(HystrixCommandGroupKey.Factory.asKey("HelloService"));
//    this._name = _name;
//  }

//
//  public SayHelloCommand(String _name) {
//    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("HelloServiceGroup"))
//            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(500)));
//    this._name = _name;
//  }

  public SayHelloCommand(String _name){
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("HelloGroup"))
            .andCommandKey(HystrixCommandKey.Factory.asKey("HelloWord"))
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                    .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE))
            //使用HystrixThreadPoolKey工厂定义线程池名称
            .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("HelloWordPool")));
    this._name = _name;
  }

  @Override
  protected String run() throws Exception {
    Thread.sleep(600);

    return String.format("Hello %s!", _name);
  }

  @Override
  protected String getFallback() {
    return String.format("[FallBack]Hello %s!", _name);
  }


  /**
   * test.
   */
  public static class Test{

    @org.junit.Test
    public void test(){
      SayHelloCommand sayHelloCommand = new SayHelloCommand("World");
      System.out.println(sayHelloCommand.execute());
    }

  }

}
