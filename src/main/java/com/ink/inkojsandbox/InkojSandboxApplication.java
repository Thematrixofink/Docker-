package com.ink.inkojsandbox;

import com.ink.inkojsandbox.model.dto.ExecuteCodeRequest;
import com.ink.inkojsandbox.model.dto.ExecuteCodeResponse;
import com.ink.inkojsandbox.service.impl.JavaDockerSandbox;
import com.ink.inkojsandbox.service.impl.JavaNativeSandbox;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;

@SpringBootApplication
public class InkojSandboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(InkojSandboxApplication.class, args);


//            JavaDockerSandbox javaDockerSandbox = new JavaDockerSandbox();
//            //JavaNativeSandbox javaNativeSandbox = new JavaNativeSandbox();
//            ExecuteCodeRequest codeRequest = new ExecuteCodeRequest();
//            codeRequest.setCode("public class Solution {\n" +
//                    "    public static void main(String[] args) {\n" +
//                    "        int a = Integer.parseInt(args[0]);\n" +
//                    "        int b = Integer.parseInt(args[1]);\n" +
//                    "        System.out.println(a+b);\n" +
//                    "    }\n" +
//                    "}");
//            ArrayList<String> input = new ArrayList<>();
//            input.add("1 2");
//            input.add("2 2");
//            codeRequest.setInput(input);
//            codeRequest.setLanguage("java");
//            ExecuteCodeResponse executeCodeResponse = javaDockerSandbox.executeCode(codeRequest);
//            System.out.println(executeCodeResponse);
    }

}
