package br.com.kimae.dinamicunit.test;

import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class DinamicUnitTestApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DinamicUnitTestApplication.class);

	private final ClassGenerator classGenerator = new ClassGenerator();

	public static void main(String[] args) {
		SpringApplication.run(DinamicUnitTestApplication.class, args);
	}


	@Override
	public void run(final String... args) throws Exception {
		String code ="package br.com.kimae.dinamicunit.test;"
			+ "public class KimHelloWorld implements HelloWorld {"
			+ "    public String execute(String name){"
			+ "        return \"Hello World: {NAME}\".replace(\"{NAME}\", name);"
			+ "    }"
			+ "}";

//		Class<?> clazz = classGenerator.generate("KimHelloWorld", "br.com.kimae.dinamicunit.test",code);
//		HelloWorld h = (HelloWorld)clazz.newInstance();
//		log.info("pah! {}", h.execute("PARTIU"));

		String test = "package br.com.kimae.dinamicunit.test;\n"
			+ "\n"
			+ "import static org.junit.Assert.assertEquals;"
			+ "\n"
			+ "import org.junit.Test;\n"
			+ "\n"
			+ "public class HelloWorldTest {\n"
			+ "\n"
			+ "    private static final String DEFAULT_TEXT = \"Hello World: {NAME}\";\n"
			+ "\n"
			+ "    private final HelloWorld helloWorld = new KimHelloWorld();\n"
			+ "\n"
			+ "    @Test\n"
			+ "    public void teste_1(){\n"
			+ "        final String name = \"Teste\";\n"
			+ "\n"
			+ "        final String result = helloWorld.execute(name);\n"
			+ "\n"
			+ "        assertEquals(DEFAULT_TEXT.replace(\"{NAME}\", name), result);\n"
			+ "    }\n"
		    + "public static class KimHelloWorld implements HelloWorld {"
			+ "    public String execute(String name){"
			+ "        return \"Hello World: {NAME}\".replace(\"{NAME}\", name);"
			+ "    }"
			+ "}"
			+ "}";
		classGenerator.loadClasses("HelloWorldTest", "br.com.kimae.dinamicunit.test",test);

		JUnitCore jUnitCore = new JUnitCore();
		jUnitCore.addListener(new TextListener(System.out));
		jUnitCore.run(Class.forName("br.com.kimae.dinamicunit.test.HelloWorldTest"));
	}

}
