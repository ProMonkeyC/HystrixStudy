package com.hystrix.demo;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author chenlong
 * Created on 2018/6/11
 */
public class RequestCache extends HystrixCommand{


  private final int value;
  private final String value1;

  public RequestCache(int value, String value1) {
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RequestCacheGroup")));
    this.value = value;
    this.value1 = value1;
  }

  @Override
  protected Object run() throws Exception {
    System.out.println("run execute, thread = " + Thread.currentThread().getName());
    return value == 0 || value % 2 == 0;
  }

  @Override
  protected String getCacheKey() {
    return String.valueOf(value) + value1;
  }


  public static class UnitTest{
//    @Test
    public void testWithoutCache(){

      HystrixRequestContext context = HystrixRequestContext.initializeContext();
      try {

        assertTrue(Boolean.valueOf(new RequestCache(2, "HLX").execute().toString()));
        assertTrue(Boolean.valueOf(new RequestCache(1, "HLX").execute().toString()));


      } finally {
        context.shutdown();
      }

    }

    @Test
    public void testWithCacheHits(){
      HystrixRequestContext context = HystrixRequestContext.initializeContext();
      try {

        RequestCache requestCache1 = new RequestCache(2, "HLX");
        RequestCache requestCache2 = new RequestCache(2, "HLX");
        RequestCache requestCache3 = new RequestCache(2, "HLX123");

        assertTrue(Boolean.valueOf(requestCache1.execute().toString()));
        assertFalse(requestCache1.isResponseFromCache);

        assertTrue(Boolean.valueOf(requestCache2.execute().toString()));
        assertTrue(requestCache2.isResponseFromCache); //同一个context中缓存中存在结果可以直接从getCache中获取

        assertTrue(Boolean.valueOf(requestCache3.execute().toString()));
        assertFalse(requestCache3.isResponseFromCache);
      } finally {
        context.shutdown();
      }

      context = HystrixRequestContext.initializeContext();

      try {

        RequestCache requestCache4 = new RequestCache(2, "HLX");
        RequestCache requestCache5 = new RequestCache(2, "HLX");

        assertTrue(Boolean.valueOf(requestCache4.execute().toString()));
        assertFalse(requestCache4.isResponseFromCache); //重新初始化的context不会从getCache中获取
        //没有执行requestCache5.execute(),Boolean.valueOf(requestCache5.execute().toString())一直非false
//        assertFalse(Boolean.valueOf(requestCache5.execute().toString()));
      } finally {
        context.shutdown();
      }


    }

  }

}
