package br.com.kimae.dinamicunit.test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class DinamicUnitTestApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DinamicUnitTestApplication.class);

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

		JavaSourceFromString file = new JavaSourceFromString("br.com.kimae.dinamicunit.test.KimHelloWorld", code);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaSourceFromString> diagnostics = new DiagnosticCollector<JavaSourceFromString>();
		Iterable<? extends JavaSourceFromString> compilationUnits = Arrays.asList(file);
		ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));
		CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits);

		if(!task.call()){
			log.error("Erro ao tentar criar a classe");
		}

		Lookup lookup = MethodHandles.lookup();
		ClassLoader cl = lookup.lookupClass().getClassLoader();
		byte[] b = fileManager.o.getBytes();

		Method method = ClassLoader.class.getDeclaredMethod("defineClass",
			String.class,
			byte[].class,
			int.class,
			int.class);

		if (!method.isAccessible())
			method.setAccessible(true);

		Class<?> clazz = (Class<?>) method.invoke(cl,
			"br.com.kimae.dinamicunit.test.KimHelloWorld",
			b,
			0,
			b.length);
		HelloWorld h = (HelloWorld)clazz.newInstance();
		log.info("pah! {}", h.execute("PARTIU"));

	}

	static class JavaSourceFromString extends SimpleJavaFileObject {

		final String code;

		JavaSourceFromString(String name, String code) {
			super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return code;
		}
	}

	static final class ClassFileManager
		extends ForwardingJavaFileManager<StandardJavaFileManager> {
		JavaFileObject o;
		ClassFileManager(StandardJavaFileManager m) {
			super(m);
		}
		@Override
		public JavaFileObject getJavaFileForOutput(
			JavaFileManager.Location location,
			String className,
			JavaFileObject.Kind kind,
			FileObject sibling
		) {
			return o = new JavaFileObject(className, kind);
		}
	}

	static final class JavaFileObject
		extends SimpleJavaFileObject {
		final ByteArrayOutputStream os =
			new ByteArrayOutputStream();
		JavaFileObject(String name, JavaFileObject.Kind kind) {
			super(URI.create(
				"string:///"
					+ name.replace('.', '/')
					+ kind.extension),
				kind);
		}
		byte[] getBytes() {
			return os.toByteArray();
		}
		@Override
		public OutputStream openOutputStream() {
			return os;
		}
	}
}
