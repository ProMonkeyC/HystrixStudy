package com.hystrix.demo;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.junit.Assert;
import org.junit.Test;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.util.Iterator;


/**
 * @author chenlong
 * Created on 2018/6/11
 */
public class HelloHystrixObservableCommand extends HystrixObservableCommand<String> {


  private final String name;

  public HelloHystrixObservableCommand(String name) {
    super(HystrixCommandGroupKey.Factory.asKey("observableGroup"));
    this.name = name;
  }

  //Implement this method with code to be executed when observe() or toObservable() are invoked.
  @Override
  protected rx.Observable<String> construct() {
    System.out.println("call construct, thread : " + Thread.currentThread().getName());

    return Observable.create(new Observable.OnSubscribe<String>() {
      @Override
      public void call(Subscriber<? super String> subscriber) {
        try{
          if (!subscriber.isUnsubscribed()){
            subscriber.onNext("Hello");
            subscriber.onNext(name + "!");
            //int i = 1 / 0; //模拟异常
            System.out.println("complete before, thread :" + Thread.currentThread().getName());
            subscriber.onCompleted();
            System.out.println("complete after, thread :" + Thread.currentThread().getName());
            //不会在执行observer的任何方法
            subscriber.onNext("test");
            subscriber.onCompleted();

          }
        } catch (Exception e){
          subscriber.onError(e);
        }
      }
    }).subscribeOn(Schedulers.io());
  }

  @Override
  protected Observable<String> resumeWithFallback() {
    return Observable.create(new Observable.OnSubscribe<String>(){
      @Override
      public void call(Subscriber<? super String> subscriber) {
        try{

          if (!subscriber.isUnsubscribed()){
            subscriber.onNext("fail !");
            subscriber.onNext("find exception");
            subscriber.onCompleted();
          }

        } catch (Exception e){
          subscriber.onError(e);
        }
      }
    }).subscribeOn(Schedulers.io());
  }

  /**
   * test
   *
   * HystrixObservableCommand vs HystrixCommand：
   * 1）前者的命令封装在contruct()，后者在run()；前者的fallback处理封装在resumeWithFallback()，后者在getFallBack()
   * 2）前者用主线程执行contruct()，后者另起线程来执行run()
   * 3）前者可以在contruct()中顺序定义多个onNext，当调用subscribe()注册成功后将依次执行这些onNext，后者只能在run()中返回一个值（即一个onNext）
   *
   */
  public static class Test{

    @org.junit.Test
    public void test(){
      //Assert.assertEquals("Hello World!", new HelloHystrixObservableCommand("World"));

      Observable<String> observable = new HelloHystrixObservableCommand("World").observe();

      observable.subscribe(new Observer<String>() {
        @Override
        public void onCompleted() {
          System.out.println("execute onCompleted");
        }

        @Override
        public void onError(Throwable throwable) {
          System.out.println("onError : " + throwable.getMessage());
        }

        @Override
        public void onNext(String s) {
          System.out.println("onNext : " + s);
        }
      });


    }

    @org.junit.Test
    public void testObservable() {
      Observable<String> observable= new HelloHystrixObservableCommand("World").observe();

      Iterator<String> iterator = observable.toBlocking().getIterator();
      while(iterator.hasNext()) {
        System.out.println(iterator.next());
      }
    }

  }




}
