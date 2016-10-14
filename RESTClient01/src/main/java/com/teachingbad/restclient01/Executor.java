package com.teachingbad.restclient01;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author ck
 */
public class Executor {

    private static final String CLIENT_KEY = "JJt_xfuNutSmGgg64uCdRB52bgYa";
    private static final String CLIENT_SECRET = "kZf818WxJJfQr2hja7b4jnIhz6oa";
    private static final String USERNAME = "walter";
    private static final String PASSOWRD = "wso2123_";

    private static final String TOKEN_FILE_NAME = "config.properties";
    private static final String API_GATEWAY_HOST = "http://api.democloud.com:8280";
    private static final String TOKEN_API_HOST = "http://security.democloud.com:8280";
    private static final String BASE_API_PATH = "/travelroutes/1.0/transport-options";

    private static String accessToken = "";
    private static boolean regenerateToken = false;

    public static void main(String[] args) throws ClientProtocolException, IOException {

        invokeAPI(API_GATEWAY_HOST + BASE_API_PATH + "/routes?from=KT89HA&to=NN14JL&at=08%3A00");
        //invokeAPI(API_HOST + BASE_API_PATH +"/routes/LNDN00014");

        // To demo the Peak hit
        //for(int i = 0; i < 100; i++){
        //System.out.println("Invocation "+i);
        //invokeAPI("http://52.91.85.201:8280/democal/1.0.0/add?x=100&y="+(i*100));
        //invokeAPI(API_HOST + BASE_API_PATH +"/routes?from=KT89HA&to=NN14JL&at=08%3A00");
        //invokeAPI(API_HOST + BASE_API_PATH +"/routes/LNDN00012");
        //}
    }

    public static void invokeAPI(String url) throws ClientProtocolException, IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request;

        request = new HttpGet(url);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.setHeader("Authorization", getToken(false));
        HttpResponse response = client.execute(request);

        if (response.getStatusLine().getStatusCode() == 401) {
            System.out.println("EXISTING TOKEN IS INVALID");
            if (response.getEntity() != null) {
                response.getEntity().consumeContent();
            }
            request.setHeader("Authorization", getToken(true));
            response = client.execute(request);
        } else {
            System.out.println("EXISTING TOKEN IS VALID");
        }
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = null;
        while ((line = rd.readLine()) != null) {
            System.out.println(line);
        }
    }

    public static String getToken(boolean isRefreshed) throws IOException {

        HttpClient client = new DefaultHttpClient();
        HttpPost request;
        String authHeader = CLIENT_KEY + ":" + CLIENT_SECRET;
        byte[] encodedBytes = Base64.encodeBase64(authHeader.getBytes());

        Properties prop;
        FileOutputStream output;
        InputStream input = null;

        //System.out.println("encodedBytes " + new String(encodedBytes));
        if (isRefreshed) {
            System.out.println("REQUESTING TOKEN FROM API-M SERVER");
            request = new HttpPost(TOKEN_API_HOST + "/token?grant_type=password&username=" + USERNAME + "&password=" + PASSOWRD);
            request.setHeader("Authorization", "Basic " + new String(encodedBytes));
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");
            HttpResponse response = client.execute(request);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = null;
            String responseStr = "";
            while ((line = rd.readLine()) != null) {
                responseStr += line;
            }
            System.out.println(responseStr);
            JSONObject jObject = new JSONObject(responseStr); // json
            accessToken = jObject.getString("access_token");

            System.out.println("RECORDING NEW ACCESS TOKEN FOR " + USERNAME);
            prop = new Properties();
            prop.load(new FileInputStream(TOKEN_FILE_NAME));
            prop.put(USERNAME, accessToken);
            output = new FileOutputStream(TOKEN_FILE_NAME);
            prop.store(output, "This is overwrite file");
        } else {
            input = new FileInputStream(TOKEN_FILE_NAME);
            prop = new Properties();
            prop.load(input);
            accessToken = prop.getProperty(USERNAME);
            System.out.println("RETURNING ACCESS TOKEN FOR " + USERNAME);
        }
        return "Bearer " + accessToken;
    }
}
