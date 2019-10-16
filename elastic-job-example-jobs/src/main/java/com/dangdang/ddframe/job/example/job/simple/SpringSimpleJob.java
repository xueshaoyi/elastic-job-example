/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.example.job.simple;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.example.fixture.entity.Foo;
import com.dangdang.ddframe.job.example.fixture.repository.FooRepository;
import com.dangdang.ddframe.job.executor.handler.JobProperties;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.config.LiteJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import javafx.print.JobSettings;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpringSimpleJob implements SimpleJob {


    @Resource
    private ZookeeperRegistryCenter regCenter;



    private CuratorFramework client;

    @Resource
    private JobEventConfiguration jobEventConfiguration;

    @Override
    public void execute(final ShardingContext shardingContext) {
        System.out.println("init test Job");
        JavaSimpleJob job = new JavaSimpleJob();
        List<String> nameList = new ArrayList<>();
        for (int i = 0 ; i < 6; i ++) {
            String name = "-JobTest"+ i;
            nameList.add(name);
        }
        JobNodeStorage jobNodeStorage = new JobNodeStorage(regCenter, new SpringSimpleJob().getClass().getName());

        nameList.forEach(name -> {
            initTest(name, job, "0/20 * * * * ?", 1, "0=test,1=test1,2=test2", jobNodeStorage);
        });





    }

    private void initTest(String jobName, final SimpleJob simpleJob, @Value("${simpleJob.cron}") final String cron, @Value("${simpleJob.shardingTotalCount}") final int shardingTotalCount,
                          @Value("${simpleJob.shardingItemParameters}") final String shardingItemParameters, JobNodeStorage jobNodeStorage){
        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(
                simpleJob.getClass().getName()+jobName, cron, shardingTotalCount).shardingItemParameters(shardingItemParameters).build(), simpleJob.getClass().getCanonicalName())).overwrite(true)
                .jobShardingStrategyClass("com.dangdang.ddframe.job.lite.api.strategy.impl.RotateServerByNameJobShardingStrategy").build();

        String configJson = LiteJobConfigurationGsonFactory.toJson(liteJobConfiguration);

        String key = "/com.dangdang.ddframe.job.example.job.simple.SpringSimpleJob/JobNames/" + liteJobConfiguration.getJobName();
        System.out.println("persist Job");
        regCenter.persist(key, configJson);
    }
}
