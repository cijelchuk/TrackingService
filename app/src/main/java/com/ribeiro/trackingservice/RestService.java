package com.ribeiro.trackingservice;

import android.util.Log;

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
    private URL EndPoint ;
    private String HTTPMethod;


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
}
