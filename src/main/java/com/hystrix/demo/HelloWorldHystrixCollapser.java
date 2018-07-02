/**
 * Copyright 2018 asiainfo Inc.
 **/
package com.hystrix.demo;

import com.netflix.hystrix.HystrixCollapser;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixEventType;
import com.netflix.hystrix.HystrixInvokableInfo;
import com.netflix.hystrix.HystrixRequestLog;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author chenlong
 * Created on 2018/6/11
 *
 * There are 2 styles of request-collapsing supported by Hystrix: request-scoped and globally-scoped.
 * This is configured at collapser construction, and defaulted to request-scoped.
 * A request-scoped collapser collects a batch per HystrixRequestContext,
 * while a globally-scoped collapser collects a batch across multiple HystrixRequestContexts.
 * As a result, if your downstream dependencies cannot handle multiple HystrixRequestContexts in a single command invocation, request-scoped collapsing is the proper choice.
 */
public class HelloWorldHystrixCollapser extends HystrixCollapser<List<String>, String, Integer> {

  private final Integer key;

  public HelloWorldHystrixCollapser(Integer key) {
    this.key = key;
  }

  @Override
  public Integer getRequestArgument() {
    return key;
  }

  @Override
  protected HystrixCommand<List<String>> createCommand(Collection<CollapsedRequest<String, Integer>> collection) {
    return new BatchCommand(collection);
  }

  @Override
  protected void mapResponseToRequests(List<String> strings, Collection<CollapsedRequest<String, Integer>> collection) {

    int count = 0;
    for (CollapsedRequest<String, Integer> request : collection) {
      request.setResponse(strings.get(count++));
    }

  }

  //Command内部类
  private static final class BatchCommand extends HystrixCommand<List<String>> {

    private final Collection<CollapsedRequest<String, Integer>> requests;

    private BatchCommand(Collection<CollapsedRequest<String, Integer>> requests){
      super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("BatchCommandGroup"))
              .andCommandKey(HystrixCommandKey.Factory.asKey("BatchCommandKey")));
      this.requests = requests;
    }

    @Override
    protected List<String> run() throws Exception {

      ArrayList<String> response = new ArrayList<>();
      //处理没个请求，返回结果、
      for (CollapsedRequest<String, Integer> request : requests) {

        response.add("ValueForKey : " + request.getArgument() + ", thread : " + Thread.currentThread().getName());
      }

      return response;
    }
  }

  public static class UnitTest{

    //相邻两个请求可以自动合并的前提是两者足够“近”：启动执行的间隔时间足够小，默认10ms

    @Test
    public void collapsedTest() throws Exception{

      HystrixRequestContext context = HystrixRequestContext.initializeContext();
      try {

        Future<String> f1 = new HelloWorldHystrixCollapser(1).queue();
        Future<String> f2 = new HelloWorldHystrixCollapser(2).queue();

        System.out.println(new HelloWorldHystrixCollapser(11).execute());  //可能会合并到f1和f2的批量请求中
        System.out.println(new HelloWorldHystrixCollapser(12).execute());  //IO打印，这条很可能不会合并到f1和f2的批量请求中

        Future<String> f3 = new HelloWorldHystrixCollapser(3).queue();
        Future<String> f4 = new HelloWorldHystrixCollapser(4).queue();

        Future<String> f5 = new HelloWorldHystrixCollapser(5).queue();

        //f5和f6，如何sleep时间足够小则会合并，如果sleep时间足够大则不会合并，默认10ms
        TimeUnit.MILLISECONDS.sleep(15);

        Future<String> f6 = new HelloWorldHystrixCollapser(6).queue();

        System.out.println(f1.get());
        System.out.println(f2.get());
        System.out.println(f3.get());
        System.out.println(f4.get());
        System.out.println(f5.get());
        System.out.println(f6.get());

        System.out.println(new HelloWorldHystrixCollapser(7).execute());
        System.out.println(new HelloWorldHystrixCollapser(8).queue().get());
        System.out.println(new HelloWorldHystrixCollapser(9).queue().get());


        //获取执行命令的数量
        int executedNum = HystrixRequestLog.getCurrentRequest().getAllExecutedCommands().size();
        System.out.println("executedNum : " + executedNum);

        int numLogs = 0;
        for (HystrixInvokableInfo<?> command : HystrixRequestLog.getCurrentRequest().getAllExecutedCommands()) {
          numLogs++;
          System.out.println(command.getCommandKey().name() + " -> command.getExcutionEvents() : " + command.getExecutionEvents());

//          assertTrue(command.getExecutionEvents().contains(HystrixEventType.COLLAPSED)); //检查是否存在COLLAPSED类型的事件
          assertTrue(command.getExecutionEvents().contains(HystrixEventType.SUCCESS));

        }

        assertEquals(executedNum, numLogs);

      } finally {
        context.shutdown();
      }


    }




  }


}
