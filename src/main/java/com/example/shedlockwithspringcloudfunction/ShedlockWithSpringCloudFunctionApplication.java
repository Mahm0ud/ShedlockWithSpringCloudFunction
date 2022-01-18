package com.example.shedlockwithspringcloudfunction;

import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.PollableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.function.Supplier;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@SpringBootApplication
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
@Slf4j
public class ShedlockWithSpringCloudFunctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShedlockWithSpringCloudFunctionApplication.class, args);
    }

    @PollableBean
    public Supplier<String> getScheduledJob() {
        ScheduledTask task = new ScheduledTask();
        return task::fire;
    }

    @Bean
    public LockProvider lockProvider(MongoTemplate mongoTemplate) {
        CodecRegistry pojoCodecRegistry = fromRegistries(mongoTemplate.getDb().getCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoDatabase database = mongoTemplate.getDb().withCodecRegistry(pojoCodecRegistry);
        return new MongoLockProvider(database);
    }

}

@Slf4j
class ScheduledTask {
    @SchedulerLock(name = "myJobLock", lockAtMostFor = "10s", lockAtLeastFor = "1s")
    public String fire() {
        log.info("Task was fired");
        return String.valueOf(System.currentTimeMillis());
    }
}

