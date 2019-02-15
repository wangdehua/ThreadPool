package com.bmac.ffan.modular.timingtask;

import org.apache.log4j.Logger;

import java.util.concurrent.*;

/**
 *  多线程中,线程是不可以重用的 ,因为线程开启后，用完就关闭了，不可以再次开启的，查看源码发现会每次新创建一个线程用来处理业务
 *  比如：Executors.newFixedThreadPool(3);在线程池中保持三个线程可以同时执行，但是注意，并不是说线程池中永远都是这三个线程，
 *        只是说可以同时存在的线程数，当某个线程执行结束后，会有新的线程进来
 */
public class ThreadPoolTest {


    private static Logger logger = Logger.getLogger(ThreadPoolTest.class);

    /**
     * 通过Executors 穿件多线程的方法
     */

    public static void testSingleThreadExecutes(){
        /*
            测试 newSingleThreadExecutors 单线程的线程池
            理解: newSingleThreadExecutors是单线程的线程池 ,
            并不是一条线程处理所有的请求,而是一条线程处理完请求后 , 立刻关闭 , 开启一个新的线程去处理新的请求
            它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行
         */
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        for (int i = 0; i < 100 ; i++) {
            executorService.execute(new ThreadPools(i));
        }
    }

    public static void testFixThreadExecutes(){
        /*
            测试 newFixedThreadPool 创建定长的线程池
            线程池允许同时存在两个线程: 每次只有两个线程在处理，当第前两个线程执行完毕后，新的线程进来开始处理（线程地址不一样）
         */
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 10 ; i++) {
            executorService.execute(new ThreadPools(i));
        }
    }

    public static void testCachedThreadPool(){
        /*
            测试 newCachedThreadPool 创建缓存的线程池
            可以有无限大的线程数进来:其实也有限制的,数目为Interger. MAX_VALUE), 这样可灵活的往线程池中添加线程。)
            如果长时间没有往线程池中提交任务，即如果工作线程空闲了指定的时间(默认为1分钟)，
            则该工作线程将自动终止。终止后，如果你又提交了新的任务，则线程池重新创建一个工作线程。
            在使用CachedThreadPool时，一定要注意控制任务的数量，否则，由于大量线程同时运行，很有会造成系统瘫痪。
         */
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10 ; i++) {
            executorService.execute(new ThreadPools(i));
        }
    }

    public static void testScheduleThreadPool(){
        /*
            测试 newScheduledThreadPool 创建定是任务的线程池
            延迟三秒之后执行，除了延迟执行之外和newFixedThreadPool基本相同，可以用来执行定时任务
         */
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        for (int i = 0; i < 10 ; i++) {
            scheduledExecutorService.schedule(new ThreadPools(i),3, TimeUnit.SECONDS);
        }
    }


    /**
     * 通过 ThreadPoolExecutor 创建线程池
        规则:
                 public ThreadPoolExecutor(  int corePoolSize,
                                             int maximumPoolSize,
                                             long keepAliveTime,
                                             TimeUnit unit,
                                             BlockingQueue<Runnable> workQueue,
                                             ThreadFactory threadFactory,
                                             RejectedExecutionHandler handler)

                                             corePoolSize - 线程池核心池的大小。
                                             maximumPoolSize - 线程池的最大线程数。
                                             keepAliveTime - 当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间。
                                             unit - keepAliveTime 的时间单位。
                                             workQueue - 用来储存等待执行任务的队列。
                                             threadFactory - 线程工厂。
                                             handler - 拒绝策略。


        注意:
             关注点1
                线程池大小
                     线程池有两个线程数的设置，一个为核心池线程数，一个为最大线程数。
                     在创建了线程池后，默认情况下，线程池中并没有任何线程，等到有任务来才创建线程去执行任务，除非调用了prestartAllCoreThreads()或者prestartCoreThread()方法
                     当创建的线程数等于 corePoolSize 时，会加入设置的阻塞队列。当队列满时，会创建线程执行任务直到线程池中的数量等于maximumPoolSize。

             关注点2
                适当的阻塞队列
                     java.lang.IllegalStateException: Queue full
                     方法 抛出异常 返回特殊值 一直阻塞 超时退出
                     插入方法 add(e) offer(e) put(e) offer(e,time,unit)
                     移除方法 remove() poll() take() poll(time,unit)
                     检查方法 element() peek() 不可用 不可用
                     ArrayBlockingQueue ：一个由数组结构组成的有界阻塞队列。
                     LinkedBlockingQueue ：一个由链表结构组成的有界阻塞队列。
                     PriorityBlockingQueue ：一个支持优先级排序的无界阻塞队列。
                     DelayQueue： 一个使用优先级队列实现的无界阻塞队列。
                     SynchronousQueue： 一个不存储元素的阻塞队列。
                     LinkedTransferQueue： 一个由链表结构组成的无界阻塞队列。
                     LinkedBlockingDeque： 一个由链表结构组成的双向阻塞队列。

             关注点3
                    明确拒绝策略
                         ThreadPoolExecutor.AbortPolicy: 丢弃任务并抛出RejectedExecutionException异常。 (默认)
                         ThreadPoolExecutor.DiscardPolicy：也是丢弃任务，但是不抛出异常。
                         ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
                         ThreadPoolExecutor.CallerRunsPolicy：由调用线程处理该任务

             说明：
                    Executors 各个方法的弊端：
                     1）newFixedThreadPool 和 newSingleThreadExecutor:
                     主要问题是堆积的请求处理队列可能会耗费非常大的内存，甚至 OOM(内存溢出)。
                     2）newCachedThreadPool 和 newScheduledThreadPool:
                     主要问题是线程数最大数是 Integer.MAX_VALUE，可能会创建数量非常多的线程，甚至 OOM。

     根据ThreadPool创建线程池的方法:
        newSingleThreadExecutor:
             创建一个单线程的线程池。这个线程池只有一个线程在工作，也就是相当于单线程串行执行所有任务。如果这个唯一的线程因为异常结束，那么会有一个新的线程来替代它。
             此线程池保证所有任务的执行顺序按照任务的提交顺序执行。
             new ThreadPoolExecutor(1, 1,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
        newFixedThreadPool:
             创建固定大小的线程池。每次提交一个任务就创建一个线程，直到线程达到线程池的最大大小。
             线程池的大小一旦达到最大值就会保持不变，如果某个线程因为执行异常而结束，那么线程池会补充一个新线程。
             new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        newCachedThreadPool:
             创建一个可缓存的线程池。如果线程池的大小超过了处理任务所需要的线程，
             那么就会回收部分空闲（60秒不执行任务）的线程，当任务数增加时，此线程池又可以智能的添加新线程来处理任务。
             此线程池不会对线程池大小做限制，线程池大小完全依赖于操作系统（或者说JVM）能够创建的最大线程大小。
             new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,new SynchronousQueue<Runnable>());
     */
    public static void testSingleThreadPoolExecute(){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 3, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        for (int i = 0; i < 10 ; i++) {
            threadPoolExecutor.execute(new ThreadPools(i));
        }
    }

    /**

        补充 :
            1 . Executor接口和Executors工厂类
                Executor是接口 , 子类 ExecutorService
                Executors工厂类 , 它所有的方法返回的都是ThreadPoolExecutor、ScheduledThreadPoolExecutor这两个类的实例
                newFixedThreadPool , newSingleThreadExecutor , newCachedThreadPool , newScheduledThreadPool 可以直接创建
            2 . submit 和 execute 的区别 :
                execute : 没有返回值 , submit : 有返回值 会返回一个Future对象 , 任务的执行结果
            3 . newSingleThreadExecutor 与 newFixedThreadPool(1) 的区别 :
                newSingleThreadExecutor :
                        a . 能保证执行顺序，先提交的先执行。
                        b . 当线程执行中出现异常，去创建一个新的线程替换之。
                newFixedThreadPool(1)  :
                        a . 当线程因为异常终止时，newFixedThreadPool(1)可以新建线程继续执行。
                        b . newFixedThreadPool(1) 不能保证任务的顺序执行

     */


    public static void main(String[] args) {
//        testSingleThreadExecutes();
//        testFixThreadExecutes();
//        testCachedThreadPool();
//        testScheduleThreadPool();
        testSingleThreadPoolExecute();
    }
}
