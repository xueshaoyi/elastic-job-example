package com.dangdang.ddframe.job.example.listener;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.example.job.simple.JavaSimpleJob;
import com.dangdang.ddframe.job.example.job.simple.SpringSimpleJob;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.config.LiteJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractListenerManager;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;

/**
 * @author xueshaoyi
 * @date 2019/10/16 10:38
 */
public class JobNameListenerManager extends AbstractListenerManager{

    @Resource
    private ZookeeperRegistryCenter regCenter;


    @Resource
    private JobEventConfiguration jobEventConfiguration;

    public JobNameListenerManager(CoordinatorRegistryCenter regCenter, String jobName) {
        super(regCenter, jobName);
    }

    @Override
    public void start() {
        System.out.println("#########################");
        System.out.println("start JobName Listener");
        System.out.println("#########################");
        addDataListener(new JobListener());
    }

    class JobListener extends AbstractJobListener {


        @Override
        protected void dataChanged(String path, TreeCacheEvent.Type eventType, String data) {
            if (path.contains("/com.dangdang.ddframe.job.example.job.simple.SpringSimpleJob/JobNames")) {
                System.out.println("monitor data path : " + path);
                System.out.println("monitor data data : " + data);
                LiteJobConfiguration liteJobConfiguration = LiteJobConfigurationGsonFactory.fromJson(data);

                if (data.isEmpty()) {
                    return;
                }
                String jobClass = liteJobConfiguration.getTypeConfig().getJobClass();
                String[] s = jobClass.split("\\.");
                String str = s[s.length -1 ];
                String job = str.substring(0,1).toLowerCase().concat(str.substring(1));
                System.out.println("monitor data job : " + job);
                SimpleJob simpleJob = new JavaSimpleJob();
//                initTest(liteJobConfiguration.getJobName(), simpleJob, liteJobConfiguration.getTypeConfig().getCoreConfig().getCron(), liteJobConfiguration.getTypeConfig().getCoreConfig().getShardingTotalCount()
//                ,liteJobConfiguration.getTypeConfig().getCoreConfig().getShardingItemParameters());
                initTest(liteJobConfiguration.getJobName(), simpleJob, "0/20 * * * * ?", 1, "0=test,1=test1,2=test2");
            }

        }


    }


    private void initTest(String jobName, final SimpleJob simpleJob, @Value("${simpleJob.cron}") final String cron, @Value("${simpleJob.shardingTotalCount}") final int shardingTotalCount,
                          @Value("${simpleJob.shardingItemParameters}") final String shardingItemParameters){
        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(
                jobName, cron, shardingTotalCount).shardingItemParameters(shardingItemParameters).build(), simpleJob.getClass().getCanonicalName())).overwrite(true)
                .jobShardingStrategyClass("com.dangdang.ddframe.job.lite.api.strategy.impl.RotateServerByNameJobShardingStrategy").build();
        System.out.println(jobEventConfiguration);
        new SpringJobScheduler(simpleJob, regCenter, liteJobConfiguration, jobEventConfiguration).init();
    }

}
