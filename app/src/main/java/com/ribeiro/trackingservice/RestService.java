package com.ribeiro.trackingservice;

import android.os.Message;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

class RestService {
    private static final String TAG = "RIBEIRO_RestService";
    private static String message;
    private String client_id;
    private String client_secret;
    private URL EndPoint ;
    private String HTTPMethod;

    public static String getMessage() {
        return message;
    }

    public static void setMessage(String message) {
        RestService.message = message;
    }

    public Boolean  getAccessToken(String userName, String userPassword){
        //formateo los datos a enviar
        //client_id=67d198a7569d479c8885f4fa90a26f46&client_secret=a4d3c5b870f545cc95b79d4dfd484fa8&granttype=password&scope=FullControl&username=admin&password=admin123
        String loginData = "";
        loginData = "client_id="+client_id.trim()+"&client_secret="+client_secret.trim()+"&granttype=password&scope=FullControl&username="+userName.trim()+"&password="+userPassword.trim();
        // Create connection
        HttpURLConnection myConnection = null;
        try {
            myConnection = (HttpURLConnection) EndPoint.openConnection();
            Log.d(TAG, "Se crea Conexion...");
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            setMessage(e.getMessage());
            return Boolean.FALSE;
        }
        myConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        myConnection.setRequestProperty("GENEXUS-AGENT", "SmartDevice Application");
        try {
            myConnection.setRequestMethod("POST");
            Log.d(TAG, "Setea Method...");
        } catch (ProtocolException e) {
            Log.d(TAG, e.getMessage());
            setMessage(e.getMessage());
            return Boolean.FALSE;
        }

        Log.d(TAG,loginData);

        try {
            // Success
            // Further processing here
            // Enable writing
            Log.d(TAG,"Enviando Informacion...");
            myConnection.setDoOutput(true);
            OutputStream outputStream = new BufferedOutputStream(myConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
            writer.write(loginData);
            writer.flush();
            writer.close();
            outputStream.close();
            JSONObject jsonObject = new JSONObject();
            JSONObject respuestaJSON = new JSONObject();
            InputStream inputStream;
            // get stream
            if (myConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream = myConnection.getInputStream();
            } else {
                inputStream = myConnection.getErrorStream();
            }
            // parse stream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String temp, response = "";
            while ((temp = bufferedReader.readLine()) != null) {
                response += temp;
            }
            // put into JSONObject
            try {
                jsonObject.put("Content", response);
                jsonObject.put("Message", myConnection.getResponseMessage());
                jsonObject.put("Length", myConnection.getContentLength());
                jsonObject.put("Type", myConnection.getContentType());
                //response = response.replace("\"","");
                respuestaJSON.put("Content", response);

                Log.d(TAG, "respuesta:" + response);

                String content = respuestaJSON.getString("Content");
                JSONObject contentObject = new JSONObject(content);
                String Token = contentObject.optString("access_token");
                String scope = contentObject.optString("scope");
                String refresh_token = contentObject.optString("refresh_token");
                String user_guid = contentObject.optString("user_guid");
                if (Token.isEmpty()) {
                    String error = contentObject.optString("error");
                    JSONObject errorObject = new JSONObject(error);
                    String msg = errorObject.optString("message");
                    Log.d(TAG, "datos:" + message );
                    setMessage(msg);
                    return Boolean.FALSE;

                }else {

                    Log.d(TAG, "datos:" + Token + scope + refresh_token + user_guid);

                    return Boolean.TRUE;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                setMessage(e.getMessage());
                return Boolean.FALSE;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Error...");
            setMessage(e.getMessage());
            return Boolean.FALSE;
        }
    }
    public void RestService(String endPoint, String httpMethod){
        setEndPoint(endPoint);
        setHTTPMethod(httpMethod);
    }
    public Boolean Send(String myData){
        // Create connection
        HttpURLConnection myConnection = null;
        try {
            myConnection = (HttpURLConnection) EndPoint.openConnection();
            Log.d(TAG, "Se crea Conexion...");
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            return false;
        }
        myConnection.setRequestProperty("Content-Type", "application/json");
        try {
            myConnection.setRequestMethod("POST");
            Log.d(TAG, "Setea Method...");
        } catch (ProtocolException e) {
            Log.d(TAG, e.getMessage());
            return false;
        }

        Log.d(TAG,myData);

        try {
            // Success
            // Further processing here
            // Enable writing
            Log.d(TAG,"Enviando Informacion...");
            myConnection.setDoOutput(true);
            OutputStream outputStream = new BufferedOutputStream(myConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
            writer.write(myData);
            writer.flush();
            writer.close();
            outputStream.close();
            JSONObject jsonObject = new JSONObject();
            InputStream inputStream;
            // get stream
            if (myConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream = myConnection.getInputStream();
            } else {
                inputStream = myConnection.getErrorStream();
            }
            // parse stream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String temp, response = "";
            while ((temp = bufferedReader.readLine()) != null) {
                response += temp;
            }
            // put into JSONObject
            try {
                jsonObject.put("Content", response);
                jsonObject.put("Message", myConnection.getResponseMessage());
                jsonObject.put("Length", myConnection.getContentLength());
                jsonObject.put("Type", myConnection.getContentType());
                Log.d(TAG,"respuesta:"+ jsonObject.toString());
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Error...");
            return false;
        }

    }


    public URL getEndPoint() {
        return EndPoint;
    }

    public void setEndPoint(String endPoint) {
        try {
            EndPoint = new URL(endPoint);
        } catch (MalformedURLException e) {
            Log.d(TAG, e.getMessage());
        }
    }



    public String getHTTPMethod() {
        return HTTPMethod;
    }

    public void setHTTPMethod(String HTTPMethod) {
        this.HTTPMethod = HTTPMethod;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }
}
