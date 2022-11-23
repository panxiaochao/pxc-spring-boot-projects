package io.github.panxiaochao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.InetAddress;

/**
 * @author Lypxc
 */
@SpringBootApplication
public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    /**
     * @param args args
     * @throws Exception Exception
     */
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext application = SpringApplication.run(Application.class, args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String applicationName = env.getProperty("spring.application.name");
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        if (StringUtils.isEmpty(path) || "/".equals(path)) {
            path = "";
        }

        LOG.info("\n----------------------------------------------------------\n\t{}{}{}{}",
                applicationName + " is running! Access URLs:",
                "\n\tLocal   访问网址: \thttp://localhost:" + port + path,
                "\n\tExternal访问网址: \thttp://" + ip + ":" + port + path,
                "\n----------------------------------------------------------\n");
    }
}
