package hsf302.se2033jv.project_hsf302_group2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);          // Số thread tối thiểu
        executor.setMaxPoolSize(5);           // Số thread tối đa
        executor.setQueueCapacity(100);       // Hàng đợi khi thread đầy
        executor.setThreadNamePrefix("Email-");
        executor.initialize();
        return executor;
    }
}
