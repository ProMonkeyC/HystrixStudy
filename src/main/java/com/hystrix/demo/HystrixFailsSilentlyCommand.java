package com.hystrix.demo;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * @author chenlong
 * Created on 2018/6/11
 */
public class HystrixFailsSilentlyCommand extends HystrixCommand{

  private final boolean flag;

  public HystrixFailsSilentlyCommand(boolean flag) {
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("HystrixFailsSilentlyCommand")));
    this.flag = flag;
  }

  @Override
  protected List<String> run() throws Exception {

    if (flag){
      throw new RuntimeException("fail from HystrixFailsSilentlyCommand run");
    } else {
      List<String>  list = new ArrayList();
      list.add("hello");
      return list;
    }

  }

  @Override
  protected List<String> getFallback() {
    return Collections.emptyList();
  }

  /**
   * test
   */
  public static class Test{
    @org.junit.Test
    public void test1(){
      Assert.assertEquals("hello", ((List<String>)new HystrixFailsSilentlyCommand(false).execute()).get(0));
    }

    @org.junit.Test
    public void test2(){
      try {
        Assert.assertEquals(0, ((List<String>)new HystrixFailsSilentlyCommand(true).execute()).size());
      } catch (Exception e){
        System.out.println("catch HystrixRuntimeException , message : " + e.getMessage());
        fail("we should not get an exception as we fail silently with a fallback");
      }
    }

  }

}
