package com.hystrix.demo;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import org.junit.Assert;

/**
 * @author chenlong
 * Created on 2018/6/11
 */
public class HelloFallback2Command extends HystrixCommand {

  private final String _name;

  public HelloFallback2Command(String _name) {
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("fallback2Group"))
            .andCommandKey(HystrixCommandKey.Factory.asKey("fallback2Key"))
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(500)));
    this._name = _name;
  }

  @Override
  protected Object run() throws Exception {
    //模拟超时
    Thread.sleep(600);
    return "*hello " + _name;
    //模拟异常抛出
//    throw new RuntimeException("fail test");
  }

  @Override
  protected Object getFallback() {
//    return new HelloHystrixCommand(_name).execute();
    return new FB2Command(_name).execute();
  }


  private static class FB2Command extends HystrixCommand{
    private final String name;

    public FB2Command(String name) {
      super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("fallback2Group"))
              .andCommandKey(HystrixCommandKey.Factory.asKey("fb2Key"))
              .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("fb2Fallback"))); //不同线程池隔离，防止上层线程池跑满，影响降级处理
      this.name = name;
    }

    @Override
    protected Object run() throws Exception {
      return "Hello " + name;
    }

    @Override
    protected Object getFallback() {
      return super.getFallback();
    }
  }


  /**
   * Test
   */
  public static class Test{
    @org.junit.Test
    public void testFallback(){

      Object obj = new HelloFallback2Command("FallBack").execute();

      System.out.println("obj : " + obj);

      Assert.assertEquals("Hello FallBack", obj.toString());


    }


  }



























}
