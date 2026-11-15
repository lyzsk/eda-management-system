package cn.sichu.system.eda.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 脚本执行线程池：异步执行远程命令，避免 HTTP 同步等待。
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@Configuration
public class ScriptExecConfig {

    @Bean("scriptRunExecutor")
    public ThreadPoolTaskExecutor scriptRunExecutor(
        @Value("${ems.remote.max-parallel:4}") int maxParallel) {
        int max = Math.max(1, maxParallel);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Math.max(1, max / 2));
        executor.setMaxPoolSize(max);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("script-run-");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.initialize();
        return executor;
    }
}
