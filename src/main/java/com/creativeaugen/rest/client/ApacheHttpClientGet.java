package com.creativeaugen.rest.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.HTTPTokener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ApacheHttpClientGet {

	private static final String LOGIN_URL = "http://localhost:8080/RestSecurityOauth/oauth/token?grant_type=password&client_id=restapp&client_secret=restapp&username=creativeaugen&password=creativeaugen";
	private static final String ACCESS_RESOURCE_URL = "http://localhost:8080/RestSecurityOauth/api/users/?access_token=";
	private static final String REFRESH_TOKEN_URL = "http://localhost:8080/RestSecurityOauth/oauth/token?grant_type=refresh_token&client_id=restapp&client_secret=restapp&refresh_token=";

	public static void main(String[] args) throws Exception {
		try {

			HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
			CloseableHttpClient httpClient = httpClientBuilder.build();
//			JSONObject object=new JSONObject();
//			object.append("grant_type", "password");
//			object.append("client_id", "restapp");
//			object.append("client_secret", "restapp");
//			object.append("username", "creativeaugen");
//			object.append("password", "creativeaugen");
			HttpResponse response = sendRequest(httpClient, LOGIN_URL, "GET", null);
			Object returnValue = getResponseAsJsonObject(response);
			if (returnValue instanceof JSONObject) {
				String access_token = ((JSONObject) returnValue).getString("access_token");
				String token_type = ((JSONObject) returnValue).getString("token_type");
				String refresh_token = ((JSONObject) returnValue).getString("refresh_token");
				Integer expires_in = ((JSONObject) returnValue).getInt("expires_in");

				// you should store these somewhere (sqlite as a hint)
				System.out.println("access_token=" + access_token);
				System.out.println("token_type=" + token_type);
				System.out.println("refresh_token=" + refresh_token);
				System.out.println("expires_in=" + expires_in);

				// request users list
				response = sendRequest(httpClient, ACCESS_RESOURCE_URL + access_token, "GET", null);
				returnValue = getResponseAsJsonObject(response);
				if (returnValue instanceof JSONArray) {
					System.out.println(returnValue.toString());
				}

				// this just to make the thread sleep so the token expires
				Thread.currentThread().sleep((expires_in.longValue() * 1000) + 1000L);
				// request users list
				response = sendRequest(httpClient, ACCESS_RESOURCE_URL + access_token, "GET", null);
				returnValue = getResponseAsJsonObject(response);
				if (returnValue instanceof JSONArray) {
					System.out.println(returnValue.toString());
				} else {
					System.err.println(returnValue.toString());
				}

				response = sendRequest(httpClient, REFRESH_TOKEN_URL + refresh_token, "GET", null);
				returnValue = getResponseAsJsonObject(response);
				if (returnValue instanceof JSONObject) {
					access_token = ((JSONObject) returnValue).getString("access_token");
					token_type = ((JSONObject) returnValue).getString("token_type");
					refresh_token = ((JSONObject) returnValue).getString("refresh_token");
					expires_in = ((JSONObject) returnValue).getInt("expires_in");

					// you should store these somewhere (sqlite as a hint)
					System.out.println("access_token=" + access_token);
					System.out.println("token_type=" + token_type);
					System.out.println("refresh_token=" + refresh_token);
					System.out.println("expires_in=" + expires_in);
				}

			}

			httpClient.close();

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static Object getResponseAsJsonObject(HttpResponse response)
			throws UnsupportedEncodingException, IOException, JSONException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		StringBuilder builder = new StringBuilder();
		for (String line = null; (line = reader.readLine()) != null;) {
			builder.append(line).append("\n");
		}
		reader.close();
		HTTPTokener tokener = new HTTPTokener(builder.toString());
		return tokener.nextValue();
	}

	private static HttpResponse sendRequest(CloseableHttpClient httpClient, String url, String method, String json)
			throws IOException, ClientProtocolException {
		RequestBuilder builder = RequestBuilder.create(method);
		builder.setHeader("accept", "application/json");
		if (json != null) {
			builder.setEntity(new StringEntity(json));
		}
		builder.setUri(url);
		HttpUriRequest request = builder.build();
		CloseableHttpResponse response = httpClient.execute(request);
		if (response.getStatusLine().getStatusCode() != 200) {
			System.err.println("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
		}
		return response;
	}

}