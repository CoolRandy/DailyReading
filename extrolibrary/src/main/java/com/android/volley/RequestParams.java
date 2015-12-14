package com.android.volley;

import android.text.TextUtils;

import org.apache.http.protocol.HTTP;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by randy on 2015/12/14.
 * https://github.com/cat9/EasyVolley/blob/master/library/src/main/java/com/android/volley/RequestParams.java
 */
public class RequestParams {

    private String charset = HTTP.UTF_8;
//    private Charset charset = StandardCharsets.UTF_8;

    private Map<String,StringContent> stringsParams;
    private Map<String,FileContent> filesParams;


    public RequestParams() {
    }

    public RequestParams(String charset) {
        if (!TextUtils.isEmpty(charset)) {
            this.charset = charset;
        }
    }

    public void addBodyParameter(String name, String value) {
        if (stringsParams == null) {
            stringsParams = new HashMap<String, StringContent>();
        }
        StringContent content=new StringContent(value,null);
        stringsParams.put(name, content);
    }

    public void addBodyParameter(String name, String value,String charset) {
        if (stringsParams == null) {
            stringsParams = new HashMap<String, StringContent>();
        }
        StringContent content=new StringContent(value,charset);
        stringsParams.put(name, content);
    }

    public void addBodyParameter(String key, File file) {
        addBodyParameter(key, file, null);
    }

    public void addBodyParameter(String key, File file, String mimeType) {
        addBodyParameter(key,file,mimeType,null);
    }

    public void addBodyParameter(String key, File file, String mimeType, String charset) {
        if (filesParams == null) {
            filesParams = new HashMap<String, FileContent>();
        }
        filesParams.put(key, new FileContent(file, null, mimeType, charset));
    }

    Map<String,StringContent> getStringsParams(){
        return stringsParams;
    }

    Map<String,FileContent> getFilesParams(){
        return filesParams;
    }



    public static class StringContent{
        private final String value;
        private final String charset;

        public StringContent(String value, String charset) {
            this.value = value;
            if(TextUtils.isEmpty(charset)){
                this.charset = charset;
            }else {
                this.charset = charset;
            }


        }

        public String getValue(){
            return value;
        }

        public String getCharset(){
            return charset;
        }
    }

    public static class FileContent{
        private final File file;
        private final String fileName;
        private final String charset;
        private final String mimeType;

        public FileContent(File file, String fileName, String charset, String mimeType) {

            if(null == file){
                throw new RuntimeException("file is null!");
            }
            this.file = file;
            if(fileName != null){
                this.fileName = fileName;
            }else {
                this.fileName = getFileName();
            }
            if(TextUtils.isEmpty(charset)){
                this.charset=charset;
            }else{
                this.charset = charset;
            }


            this.mimeType=mimeType;
        }

        public File getFile() {
            return file;
        }

        public String getFileName() {
            return fileName;
        }

        public String getCharset() {
            return charset;
        }

        public String getMimeType() {
            return mimeType;
        }
    }
}
