package com.yongkj.pocketyun_new.schedule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {
	
	//文章点赞定时器任务Cron [ "0 0 3 * * ?" ,每天晚上凌晨三点执行 ]
	@Value("${quartz.myJobCountCron}")
	private String myJobCountCron;
	
	//创建Job对象
	@Bean
	public JobDetailFactoryBean jobDetailFactoryBean() {
		JobDetailFactoryBean factory = new JobDetailFactoryBean();
		//设置任务
		factory.setJobClass(MyJob.class);
		return factory;
	}

	//创建Trigger对象
	@Bean
	public CronTriggerFactoryBean cronTriggerFactoryBean(JobDetailFactoryBean jobDetialFactoryBean) {
		CronTriggerFactoryBean factory = new CronTriggerFactoryBean();
		factory.setJobDetail(jobDetialFactoryBean.getObject());
		//设置Cron
		factory.setCronExpression(myJobCountCron);
		return factory;
	}

	//创建Scheduler对象
	@Bean
	public SchedulerFactoryBean schedulerFactoryBean(CronTriggerFactoryBean cronTriggerFactoryBean,
			MyAdaptableFactory myAdaptableFactory) {
		SchedulerFactoryBean factory = new SchedulerFactoryBean();
		factory.setTriggers(cronTriggerFactoryBean.getObject());
		factory.setJobFactory(myAdaptableFactory);
		return factory;
	}

}
