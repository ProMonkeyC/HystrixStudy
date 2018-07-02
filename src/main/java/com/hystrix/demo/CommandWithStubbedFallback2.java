package com.hystrix.demo;

import com.hystrix.demo.entity.User;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.junit.Assert;
import org.junit.Test;
import rx.Observable;

/**
 * @author chenlong
 * Created on 2018/6/29
 */
public class CommandWithStubbedFallback2 extends HystrixObservableCommand<User>{

  private final String userId;

  private final String countryCodeFromGeoLookup;

  public CommandWithStubbedFallback2(String userId, String countryCodeFromGeoLookup) {
    super(HystrixCommandGroupKey.Factory.asKey("exGroup"));
    this.userId = userId;
    this.countryCodeFromGeoLookup = countryCodeFromGeoLookup;
  }

  @Override
  protected Observable<User> construct() {
//    return null;
    throw new RuntimeException("forcing failure for example");
  }

  @Override
  protected Observable<User> resumeWithFallback() {
    return Observable.just(new User(userId, "Unknown Name", countryCodeFromGeoLookup,
            true, true, true));
  }


  /**
   * Test.
   */
  public static class Test{
    @org.junit.Test
    public void test(){

      CommandWithStubbedFallback2 command = new CommandWithStubbedFallback2("2", "en");

      User user = command.observe().toBlocking().single();

      Assert.assertTrue(command.isFailedExecution());
      Assert.assertTrue(command.isResponseFromFallback());

      Assert.assertEquals("2", user.getUserId());
      Assert.assertEquals("en", user.getCountryCode());
      Assert.assertEquals(true, user.getFeatureXPermitted());
      Assert.assertEquals(true, user.getFeatureYPermitted());
      Assert.assertEquals(true, user.getFeatureZPermitted());

    }

  }

}
