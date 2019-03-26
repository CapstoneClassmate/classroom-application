package edu.uark.classroomapplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class ClassroomApplication implements CommandLineRunner {
	
	private static final Logger logger = LoggerFactory.getLogger(ClassroomApplication.class);
	@Autowired JdbcTemplate database;

	public static void main(String[] args) {
		SpringApplication.run(ClassroomApplication.class, args);
	}
	
    @Override
    public void run(String... strings) throws Exception {
    	// Do database setup.
    	logger.info("Creating database");
    	database.execute("DROP TABLE users IF EXISTS");
    	database.execute("CREATE TABLE users(id VARCHAR(255), username VARCHAR(255))");
    	logger.info("Done creating database");
    }

}
