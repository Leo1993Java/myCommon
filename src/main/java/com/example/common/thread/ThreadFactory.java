package com.example.common.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ligq
 * @Description:
 * @date 2019/11/24 1:19 下午
 */
public class ThreadFactory {

    ExecutorService executor = Executors.newFixedThreadPool(7);
}
