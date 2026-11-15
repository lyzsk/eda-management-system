package cn.sichu.admin;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@SpringBootApplication(scanBasePackages = "cn.sichu")
@MapperScan(basePackages = "cn.sichu.system", annotationClass = Mapper.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
