/**
 * Copyright 2018 asiainfo Inc.
 **/
package com.hystrix.demo;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.junit.Assert;
import org.junit.Test;
import rx.Observable;
import rx.functions.Action1;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author chenlong
 * Created on 2018/6/11
 */
public class HelloHystrixCommand extends HystrixCommand {

  private final String name;

  public HelloHystrixCommand(String name) {
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("helloHystrixGroup")));
    this.name = name;
  }

  @Override
  protected Object run() throws Exception {
    return "Hello " + name;
  }

  @Override
  protected Object getFallback() {
    return "execute fallback";
  }

  /**
   * Test
   */
  public static class Test{

    @org.junit.Test
    public void executeTest(){

      //execute()同步方式执行；一个对象只能execute()一次；
      Object helloHystrixCommand = new HelloHystrixCommand("Hystrix").execute();
      System.out.println("同步执行结果： " + helloHystrixCommand.toString());
      Assert.assertEquals("Hello Hystrix", helloHystrixCommand.toString());

    }

    @org.junit.Test
    public void queueTest() throws Exception{

      //queue()异步执行，直接返回，同时创建一个helloHystrixCommand.run();一个对象只能queue()一次；
      // queue()等同toObservable().toBlocking().toFuture()
      Future<String> future = new HelloHystrixCommand("Hystrix").queue();

      //使用Future时会阻塞，必须等待helloHystrixCommand.run()执行完毕
      String queueRs = future.get(10000, TimeUnit.MILLISECONDS);

      //String queueRs = future.get(); 等同上面的execute()
      System.out.println("异步执行结果：" + queueRs);
      Assert.assertEquals("Hello Hystrix", queueRs);


    }


  }



}
