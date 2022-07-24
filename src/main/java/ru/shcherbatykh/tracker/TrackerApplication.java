package ru.shcherbatykh.tracker;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@SpringBootApplication
@Configuration
@EnableJpaRepositories("ru.shcherbatykh.repositories")
@EnableTransactionManagement
@EntityScan("ru.shcherbatykh.models")
@ComponentScan (basePackages = "ru.shcherbatykh")
public class TrackerApplication {

	private static final Logger logger = Logger.getLogger(TrackerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TrackerApplication.class, args);
		logger.info("========= Application was started =========");
	}

	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver localResolver = new SessionLocaleResolver();
		localResolver.setDefaultLocale(Locale.ENGLISH);
		logger.debug("Bean 'localeResolver' was created.");
		return localResolver;
	}

	@Bean(name="messageSource")
	public ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("validation");
		logger.debug("Bean 'messageSource' was created.");
		return messageSource;
	}
}