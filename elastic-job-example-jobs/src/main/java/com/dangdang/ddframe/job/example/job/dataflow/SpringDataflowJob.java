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

package com.dangdang.ddframe.job.example.job.dataflow;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.example.fixture.entity.Foo;
import com.dangdang.ddframe.job.example.fixture.repository.FooRepository;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SpringDataflowJob implements DataflowJob<Foo> {
    
    @Resource
    private FooRepository fooRepository;
    
    @Override
    public List<Foo> fetchData(final ShardingContext shardingContext) {
        System.out.println(String.format("Item: %s | Time: %s | Thread: %s | %s",
                shardingContext.getShardingItem(), new SimpleDateFormat("HH:mm:ss").format(new Date()), Thread.currentThread().getId(), "DATAFLOW FETCH"));

        try {
            if (shardingContext.getShardingItem() == 0) {
                Thread.sleep(1000);
                System.out.println("tem: "+shardingContext.getShardingItem()+" Use time 100， " + shardingContext.getShardingParameter());
            } else if (shardingContext.getShardingItem() == 1) {
                Thread.sleep(5000);
                System.out.println("tem: "+shardingContext.getShardingItem()+" Use time 5000， " + shardingContext.getShardingParameter());
            } else {
                Thread.sleep(10000);
                System.out.println("tem: "+shardingContext.getShardingItem()+" Use time 10000， " + shardingContext.getShardingParameter());
            }
        } catch (InterruptedException e) {
                e.printStackTrace();
            }
        return fooRepository.findTodoData(shardingContext.getShardingParameter(), 10);
    }
    
    @Override
    public void processData(final ShardingContext shardingContext, final List<Foo> data) {
        System.out.println(String.format("Item: %s | Time: %s | Thread: %s | %s",
                shardingContext.getShardingItem(), new SimpleDateFormat("HH:mm:ss").format(new Date()), Thread.currentThread().getId(), "DATAFLOW PROCESS"));

        if (shardingContext.getShardingItem() == 0) {
            System.out.println("tem: "+shardingContext.getShardingItem()+" Foo is " + data.get(0).toString());
        } else if (shardingContext.getShardingItem() == 1) {
            System.out.println("tem: "+shardingContext.getShardingItem()+" Foo is " + data.get(0).toString());
        } else {
            System.out.println("tem: "+shardingContext.getShardingItem()+" Foo is " + data.get(0).toString());
        }
        for (Foo each : data) {
            fooRepository.setCompleted(each.getId());
        }
    }
}
