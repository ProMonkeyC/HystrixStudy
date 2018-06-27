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
import org.junit.Test;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;

import java.io.IOError;
import java.io.IOException;

/**
 * @author chenlong
 * Created on 2018/6/20
 */
public class HelloObserveCommand extends HystrixCommand<String> {

  private final String name;

  public HelloObserveCommand(String name) {
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("testObserverCommand"))
            .andCommandKey(HystrixCommandKey.Factory.asKey("testObserverCommandKey"))
            .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("testObserverCommandThread"))
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                    .withCircuitBreakerErrorThresholdPercentage(80)
                    .withExecutionTimeoutInMilliseconds(5000))
            .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(1)));
    this.name = name;
  }

  @Override
  protected String run() throws Exception {
    return "Hello, " + name + " ! thread : " + Thread.currentThread().getName();
  }


  /**
   * test.
   */
  public static class ObserveTest{

    /**
     * HystrixCommand的observe()与toObservable()的区别：
     * 1）observe()会立即执行HelloObserveCommand.run()；toObservable()要在toBlocking().single()或subscribe()HelloObserveCommand.run()
     * 2）observe()中，toBlocking().single()和subscribe()可以共存；在toObservable()中不行，因为两者都会触发执行HelloObserveCommand.run()，这违反了同一个HelloObserveCommand对象只能执行run()一次原则
     * @throws Exception
     */

    @Test
    public void testObserver() throws IOException{

      //observe()异步非阻塞执行，类似queue
      Observable<String> observable = new HelloObserveCommand("tom").observe();

      System.out.println("observable single result: " + observable.toBlocking().single());//single()阻塞

      //注册观察者事件,subscribe()是非阻塞的
      observable.subscribe(new Action1<String>() {
        @Override
        public void call(String s) {
          //s为 HelloObserveCommand 返回结果
          //此处主要是用户对数据对二次处理
          System.out.println("call actions, s= " + s);
        }
      });


      observable.subscribe(new Observer<String>() {
        @Override
        public void onCompleted() {
          //最终执行，onNext/onError后执行
          System.out.println("execute onCompleted");
        }

        @Override
        public void onError(Throwable throwable) {
          //产生异常时回调
          System.out.println("onError = " + throwable.getMessage());
          throwable.printStackTrace();
        }

        @Override
        public void onNext(String s) {
          //获取结果后回调
          System.out.println("onNext = " + s);

        }
      });

      //System.in.read();

    }

    @Test
    public void testToObserver() throws IOException{

      Observable<String> toObservable = new HelloObserveCommand("world").toObservable(); //异步非阻塞

      System.out.println("toObservable single result : " + toObservable.toBlocking().single());

      toObservable.subscribe(new Observer<String>() {
        @Override
        public void onCompleted() {
          System.out.println("execute onCompleted");
        }

        @Override
        public void onError(Throwable throwable) {
          System.out.println("onError : " + throwable.getMessage());
          throwable.printStackTrace();
        }

        @Override
        public void onNext(String s) {
          System.out.println("onNext : " + s);
        }
      });

      //非阻塞
//      toObservable.subscribe(new Action1<String>() {
//        @Override
//        public void call(String s) { //类似于onNext()
//          System.out.println("toObservable call s : " + s);
//        }
//      });


      System.in.read();


    }




  }



}
