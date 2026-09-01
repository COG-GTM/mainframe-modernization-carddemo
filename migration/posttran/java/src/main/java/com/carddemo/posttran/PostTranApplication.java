package com.carddemo.posttran;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Entry point for the migrated POSTTRAN step.
 *
 * <p>Run it the way the JCL runs it, by pointing the six DD properties at datasets:
 *
 * <pre>
 * java -jar posttran.jar \
 *   --posttran.dd.dalytran=data/DALYTRAN --posttran.dd.tranfile=out/TRANFILE \
 *   --posttran.dd.xreffile=data/XREFFILE --posttran.dd.dalyrejs=out/DALYREJS \
 *   --posttran.dd.acctfile=out/ACCTFILE  --posttran.dd.tcatbalf=out/TCATBALF
 * </pre>
 *
 * <p>ACCTFILE and TCATBALF are read and rewritten in place, exactly as the step does.
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class PostTranApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(PostTranApplication.class, args);
        System.exit(SpringApplication.exit(context, context.getBean(StepReturnCode.class)));
    }
}
