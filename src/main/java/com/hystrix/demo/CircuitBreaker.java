/**
 * Copyright 2018 asiainfo Inc.
 **/
package com.hystrix.demo;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author chenlong
 * Created on 2018/6/11
 */
public class CircuitBreaker extends HystrixCommand{

  private final String name;

  public CircuitBreaker(String name) {
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("CircuitBreakerGroup"))
            .andCommandKey(HystrixCommandKey.Factory.asKey("CircuitBreakerKey"))
            .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("CircuitBreakerPoolKey")) //配置线程池
            .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                    .withCoreSize(200) //配置线程池里的线程数，设置足够多线程
            ).andCommandPropertiesDefaults( //配置熔断器
                    HystrixCommandProperties.Setter()
                            .withCircuitBreakerEnabled(true)
                            .withCircuitBreakerRequestVolumeThreshold(3)
                            .withCircuitBreakerErrorThresholdPercentage(80)
                            .withCircuitBreakerForceOpen(true)  //设置为true时，所有请求都将被拒绝，直接到fallback
                            .withCircuitBreakerForceOpen(true)  //设置为true时，将忽略错误
                            .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)  //信号隔离
                            .withExecutionTimeoutInMilliseconds(500)


            )
    );
    this.name = name;
  }

  @Override
  protected Object run() throws Exception {
    System.out.println("running run() : " + name);
    int num = Integer.valueOf(name);
    if (num % 2 == 0 && num < 10){
      return name;
    } else {
      int j = 0;
      while (true){
        j++;
      }
    }
  }

  @Override
  protected Object getFallback() {
    return "CircuitBreaker fallback : " + name;
  }

  public static void main(String[] args) {

    try {
      circuitBreakTest();
    } catch (Exception e){
      System.out.println("e = " + e.getMessage());
    }

  }

  private static void circuitBreakTest() throws Exception{
    for (int i = 0; i < 50; i++) {
      try {
        System.out.println("-------------" + new CircuitBreaker(String.valueOf(i)).execute());

//        try {
//          TimeUnit.MILLISECONDS.sleep(1000);
//        } catch (Exception e) {
//
//        }
//
//        Future<String> future = new CircuitBreaker(String.valueOf(i)).queue();
//        System.out.println("==========" + future);

      } catch (Exception e) {
        System.out.println("run() 抛出HystrixBadRequestException时，被捕获到" + e.getMessage());
      }
    }

    System.out.println("**********开始打印现有线程***********");
    Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
    for (Thread thread : map.keySet()) {
      System.out.println(thread.getName());
    }

    System.out.println("thread num : " + map.size());

    System.in.read();
  }

}
