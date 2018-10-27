package com.example.esardini.infotainment.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ResponseParser {

    public static Object parseHttpResponse(org.apache.http.HttpEntity responseEntity, Class inputClass){

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        Object mappedObj = null;

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            ObjectMapper mapper = new ObjectMapper();
            mappedObj = mapper.readValue(sb.toString(), inputClass);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return mappedObj;

    }
}
