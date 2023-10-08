package com.rookie.utils;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.devtools.classpath.ClassPathChangedEvent;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.boot.devtools.filewatch.ChangedFile.Type;
import org.springframework.boot.devtools.filewatch.ChangedFiles;
import org.springframework.boot.devtools.remote.client.ClassPathChangeUploader;
import org.springframework.boot.devtools.remote.client.HttpHeaderInterceptor;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * @author yayee
 */
public class ClassUploadLauncher {

    private static final Logger logger = LoggerFactory.getLogger(ClassUploadLauncher.class);
    private static final String DEFAULT_SOURCE_DIRECTORY = "target/classes/";
    private static final String DEFAULT_CLASS_FILE = "target/classes/com/rookie/controller/system/SysUserController.class";
    private static final String DEFAULT_IP = "127.0.0.1";
    private static final String DEFAULT_PORT = "8080";
    private static final String DEFAULT_PROTOCOL = "http://";
    private static final String DEFAULT_COMMAND = "/.~~spring-boot!~/restart";

    public static ClientHttpRequestFactory clientHttpRequestFactory() {
        List<ClientHttpRequestInterceptor> interceptors = Collections.singletonList(getSecurityInterceptor());
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        return new InterceptingClientHttpRequestFactory(requestFactory, interceptors);
    }

    private static ClientHttpRequestInterceptor getSecurityInterceptor() {
        String secretHeaderName = "X-AUTH-TOKEN";
        String secret = "mysecret";
        return new HttpHeaderInterceptor(secretHeaderName, secret);
    }

    /**
     * - 待更新类文件： "D:\workspace\free\rookie-devtools\target\classes\com\rookie\controller\system\SysUserController.class"
     *
     * - 启动命令： java -jar .\rookie-devtools-launcher.jar D:\workspace\free\rookie-devtools\target\classes
     * D:\workspace\free\rookie-devtools\target\classes\com\rookie\controller\system\SysUserController.class [ip]
     * [port]
     */
    public static void main(String[] args) {
        String sourceDirectory;
        String classFile;
        String host;
        String port;
        try {
            sourceDirectory = args[0];
            classFile = args[1];
            host = args[2];
            port = args[3];
        } catch (RuntimeException ex) {
            sourceDirectory = DEFAULT_SOURCE_DIRECTORY;
            classFile = DEFAULT_CLASS_FILE;
            host = DEFAULT_IP;
            port = DEFAULT_PORT;
        }
        logger.debug("sourceDirectory: {}", sourceDirectory);
        logger.debug("classFile: {}", classFile);
        Set<ChangedFile> changedFileSet = new HashSet<>();
        Set<ChangedFiles> changedFilesSet = new HashSet<>();
        changedFileSet.add(new ChangedFile(new File(sourceDirectory), new File(classFile), Type.MODIFY));
        ChangedFiles changedFiles = new ChangedFiles(new File(sourceDirectory), changedFileSet);
        changedFilesSet.add(changedFiles);
        String url = DEFAULT_PROTOCOL.concat(host).concat(":").concat(port).concat(DEFAULT_COMMAND);
        logger.debug("remote url: {}", url);
        Object source = "modifyTest";
        try {
            ClassPathChangeUploader classPathChangeUploader = new ClassPathChangeUploader(url,
                clientHttpRequestFactory());
            ClassPathChangedEvent changedEvent = new ClassPathChangedEvent(source, changedFilesSet, true);
            classPathChangeUploader.onApplicationEvent(changedEvent);
        } catch (Exception e) {
            logger.error("LAUNCHER ERROR !", e);
        }
    }
}
