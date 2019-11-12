package br.com.kimae.dinamicunit.test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.apache.bcel.internal.util.ClassPath.ClassFile;


public class ClassGenerator {


    private static final Logger log = LoggerFactory.getLogger(ClassGenerator.class);

    private static final String FULL_NAME_PATTERN = "{PACKAGE}.{NAME}";

    public void loadClasses(final String name, final String packagee, final String content)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String fullName = FULL_NAME_PATTERN.replace("{PACKAGE}", packagee)
            .replace("{NAME}", name);

        JavaSourceFromString file = new JavaSourceFromString(fullName, content);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaSourceFromString> diagnostics = new DiagnosticCollector<JavaSourceFromString>();
        Iterable<? extends JavaSourceFromString> compilationUnits = Arrays.asList(file);
        try(ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null))){
            CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits);

            if(!task.call()){
                log.error("Fatal Error trying to create the class.");
            }

            Lookup lookup = MethodHandles.lookup();
            ClassLoader cl = lookup.lookupClass().getClassLoader();
            Method method = ClassLoader.class.getDeclaredMethod("defineClass",
                String.class,
                byte[].class,
                int.class,
                int.class);

            if (!method.isAccessible())
                method.setAccessible(true);

            for(ClassFile fileObj : fileManager.output){
                byte[] b = fileObj.getBytes();
                method.invoke(cl,
                    fileObj.getClassName(),
                    b,
                    0,
                    b.length);
            }
        }catch (Exception io){
            log.error("Fatal Error trying to create the class.");
        }

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
        private List<ClassFile> output = new ArrayList<>();
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
            final ClassFile file = new ClassFile(className, kind);
            output.add(file);
            return file;
        }
    }

    private static URI toUri(String path)  {
        try {
            return new URI(null, null, path, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException("exception parsing uri", e);
        }
    }

    static final class ClassFile
        extends SimpleJavaFileObject {

        final ByteArrayOutputStream os =
            new ByteArrayOutputStream();
        final String className;

        ClassFile(String name, JavaFileObject.Kind kind) {
            super(URI.create(
                "string:///"
                    + name.replace('.', '/')
                    + kind.extension),
                kind);
            className = name;
        }

        byte[] getBytes() {
            return os.toByteArray();
        }

        String getClassName(){
            return className;
        }

        @Override
        public OutputStream openOutputStream() {
            return os;
        }
    }
}
