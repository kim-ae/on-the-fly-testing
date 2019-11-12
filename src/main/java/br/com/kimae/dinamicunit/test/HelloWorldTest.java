package br.com.kimae.dinamicunit.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HelloWorldTest {

    private static final String DEFAULT_TEXT = "Hello World: {NAME}";

    private final HelloWorld helloWorld;

    public HelloWorldTest(final HelloWorld helloWorld){
        this.helloWorld = helloWorld;
    }

    @Test
    public void teste_1(){
        final String name = "Teste";

        final String result = helloWorld.execute(name);

        assertEquals(DEFAULT_TEXT.replace("{NAME}", name), result);
    }
}
