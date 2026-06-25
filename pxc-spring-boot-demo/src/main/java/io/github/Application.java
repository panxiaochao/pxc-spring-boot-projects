package io.github;

import io.github.panxiaochao.boot3.common.constants.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import java.net.InetAddress;

/**
 * <p>
 * Multiple Spring Data modules found, entering strict repository configuration mode.
 * exclude RedisRepositoriesAutoConfiguration.class
 * </p>
 *
 * @author Lypxc
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableCaching
@EnableScheduling
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
		if (!StringUtils.hasText(path) || "/".equals(path)) {
			path = "";
		}
		LOG.info("\n----------------------------------------------------------\n\t{}{}{}{}{}",
				String.join("", applicationName, " is running! Access URLs:"),
				String.join("", "\n\tLocal    访问网址: \t", Protocol.HTTP.getFormat(), "localhost:", port, path),
				String.join("", "\n\tExternal 访问网址: \t", Protocol.HTTP.getFormat(), ip, ":", port, path),
				String.join("", "\n\tDoc      访问网址: \t", Protocol.HTTP.getFormat(), ip, ":" + port, path, "/doc.html"),
				"\n----------------------------------------------------------\n");
	}

}
