package com.bmac.ffan.modular.timingtask;

import org.apache.log4j.Logger;

public class ThreadPools implements Runnable {

    private Logger logger = Logger.getLogger(ThreadPools.class);

    private Integer index ;

    public  ThreadPools(Integer index)
    {
        this.index=index;
    }

    @Override
    public void run() {
        try{
            logger.info("---------------------------业务代码--------------------------");
            logger.info("开始处理线程！！！");
            logger.info("--------------------------------------------"+index);
            System.out.println("我的线程标识是："+this.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
