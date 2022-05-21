package com.praca.remoteadmin.Brudnopis;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.print.DocFlavor;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Multithreading {

    final Set<CommandCallable> threads = new HashSet<>();


    public static void main(String[] args) {
        //BasicConfigurator.configure();

        try {
            Thread.sleep(2555);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Multithreading m = new Multithreading();
        //m.init();
    }

    void init() {
        int cnt = 10;
        for (int i =0; i < cnt;i++) {
            threads.add(new CommandCallable());
        }
        ExecutorService executorService = Executors.newFixedThreadPool(cnt);
        List<Future<Integer>> futures = null;
        try {
            futures = executorService.invokeAll(threads);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for(Future<Integer> future : futures){
            try {
                if(future.get().intValue() != 0) {
                    //System.out.println("Wraca  <<"+future.get().intValue()+">>");
                }


            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }


        executorService.shutdown();
    }

    class CommandCallable implements Callable<Integer> {

        public CommandCallable() {
        }

        @Override
        public Integer call() throws Exception {
            Random rnd = new Random();

            int x = rnd.nextInt(10000) + 500;
            System.out.println("THREAD"+ Thread.currentThread()+ " <<"+x+">>");
            Thread.sleep(x);
            return x;
        }
    }
}
