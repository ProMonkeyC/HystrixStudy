package com.hystrix.demo;

import com.hystrix.demo.entity.User;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.junit.Assert;

/**
 * @author chenlong
 * Created on 2018/6/29
 */
public class CommandWithStubbedFallback extends HystrixCommand<User>{

  private final String userId;

  private final String countryCodeFromGeoLookup;

  public CommandWithStubbedFallback(String userId, String countryCodeFromGeoLookup) {
    super(HystrixCommandGroupKey.Factory.asKey("exGroup"));
    this.userId = userId;
    this.countryCodeFromGeoLookup = countryCodeFromGeoLookup;
  }

  @Override
  protected User run() throws Exception {
//    return null;
    throw new RuntimeException("forcing failure for example");
  }

  /**
   * Return stubbed fallback with some static defaults, placeholders,
   * and an injected value 'countryCodeFromGeoLookup' that we'll use
   * instead of what we would have retrieved from the remote service.
   */
  @Override
  protected User getFallback() {
    return new User(userId, "Unknown Name", countryCodeFromGeoLookup,
            true, true, true);
  }

  /**
   * Test.
   */
  public static class Test{

    @org.junit.Test
    public void test(){

      CommandWithStubbedFallback command =
              new CommandWithStubbedFallback("1", "zh");
      User user = command.execute();

      Assert.assertTrue(command.isFailedExecution());
      Assert.assertTrue(command.isResponseFromFallback());

      Assert.assertEquals("1", user.getUserId());
      Assert.assertEquals("zh", user.getCountryCode());
      Assert.assertEquals(true, user.getFeatureXPermitted());
      Assert.assertEquals(true, user.getFeatureYPermitted());
      Assert.assertEquals(true, user.getFeatureZPermitted());

    }


  }



}
