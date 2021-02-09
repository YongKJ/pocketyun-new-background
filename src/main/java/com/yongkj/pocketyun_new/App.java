package com.yongkj.pocketyun_new;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Hello world!
 *
 */
//public class App 
//{
//    public static void main( String[] args )
//    {
//        System.out.println( "Hello World!" );
//    }
//}

@SpringBootApplication
@EnableScheduling
public class App {
	
	@Bean
	public MultipartConfigElement multipartConfigElement() {
	    MultipartConfigFactory factory = new MultipartConfigFactory();
	    //  设置单个文件大小
	    factory.setMaxFileSize("1024MB");//KB 或者 MB 都可以 1MB=1024KB。1KB=1024B(字节) 
	    /// 设置总上传文件大小
	    factory.setMaxRequestSize("1024MB");//KB 或者 MB 都可以 1MB=1024KB。1KB=1024B(字节) 
	    return factory.createMultipartConfig();
	}
	
    public static void main( String[] args ){
	    try {
	        SpringApplication.run(App.class, args);
		} catch (Exception e) {
			System.out.println("SpringApplication: ================" + e.getMessage());
		}
    }
  
}