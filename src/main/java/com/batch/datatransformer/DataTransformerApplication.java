package com.batch.datatransformer;

import com.batch.datatransformer.consumer.config.DataConfig;
import com.batch.datatransformer.consumer.utils.Constants;
import com.batch.datatransformer.process.helpers.BaseProcessHelper;
import com.batch.datatransformer.process.interfaces.ProcessInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.batch.datatransformer.consumer.utils.Constants.INSTANCE_NAME_KEY_ARG;

@Configuration
@ComponentScan
public class DataTransformerApplication {

	static Logger logger = LoggerFactory.getLogger(DataTransformerApplication.class);

	public static boolean programRunInJar;

	public static String resourcePath;

	public static Map<String, String> inputArgs;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext(DataTransformerApplication.class);

		logger.info("------------------ In memory processor Batch => START -------------------------");
		logger.info("Program run in jar: " + programRunInJar);
		logger.info("Resource path: " + resourcePath);

		inputArgs = getInputArgs(args);

		DataConfig dataConfig = context.getBean(DataConfig.class);
		dataConfig.setDatasetName(inputArgs.get(INSTANCE_NAME_KEY_ARG));

		try {
			ProcessInterface pi = context.getBean(BaseProcessHelper.class);

			ExecutorService executorService = Executors.newWorkStealingPool();

			Callable<Boolean> process1 = pi::executeProcess;

			List<Callable<Boolean>> callables = Arrays.asList(process1);

			executorService.invokeAll(callables);
		} catch (Exception e) {
			logger.error("Error: " + e);
		}
		logger.info("------------------ In memory processor Batch => END -------------------------");
	}

	@Bean
	public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws URISyntaxException {
		programRunInJar = DataTransformerApplication.class.getProtectionDomain().getCodeSource().getLocation().getFile().endsWith(".jar");
		PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();

		if (programRunInJar) {
			resourcePath = new File(DataTransformerApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
			propertySourcesPlaceholderConfigurer.setLocations(new FileSystemResource(resourcePath + "/config/application.properties"));
		} else {
			resourcePath = Constants.RESOURCES_DIR;
			propertySourcesPlaceholderConfigurer.setLocations(new ClassPathResource("config/application.properties"));
		}

		return propertySourcesPlaceholderConfigurer;
	}

	public static String getContextFilePath(String filePath) {
		return DataTransformerApplication.resourcePath + "/" + filePath;
	}

	public static Map<String, String> getInputArgs(String[] args) {
		return Arrays.stream(args)
				.map(x -> x.trim().split("="))
				.collect(Collectors.toMap(y -> y[0], y -> y[1]));
	}
}
