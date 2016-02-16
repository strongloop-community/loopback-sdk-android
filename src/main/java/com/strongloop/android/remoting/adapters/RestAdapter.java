// Copyright (c) 2013 StrongLoop. All rights reserved.

package com.strongloop.android.remoting.adapters;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.strongloop.android.remoting.JsonUtil;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A specific {@link Adapter} implementation for RESTful servers.
 *
 * In addition to implementing the {@link Adapter} interface,
 * <code>RestAdapter</code> contains a single {@link RestContract} to map
 * remote methods to custom HTTP routes. This is only required if the HTTP
 * settings have been customized on the server. When in doubt, try without.
 *
 * @see RestContract
 */
public class RestAdapter extends Adapter {
    private static final String TAG = "remoting.RestAdapter";

    private RestHttpClient client;
    private RestContract contract;

    public RestAdapter(Context context, String url) {
        super(context, url);
        this.contract = new RestContract();
    }

    /**
     * Gets this adapter's {@link RestContract}, a custom contract for
     * fine-grained route configuration.
     * @return the contract.
     */
    public RestContract getContract() {
        return contract;
    }

    /**
     * Get the underlying HTTP client. This allows subclasses to add
     * custom headers like Authorization.
     * @return the client.
     */
    protected AsyncHttpClient getClient() { return client; }

    /**
     * Sets this adapter's {@link RestContract}, a custom contract for
     * fine-grained route configuration.
     * @param contract The contract.
     */
    public void setContract(RestContract contract) {
        this.contract = contract;
    }

    @Override
    public void connect(Context context, String url) {
        if (url == null) {
            client = null;
        }
        else {
            client = new RestHttpClient(context, url);
            client.addHeader("Accept", "application/json");
        }
    }

    @Override
    public boolean isConnected() {
        return client != null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the contract is not set
     * (see {@link #setContract(RestContract)})
     * or the adapter is not connected.
     */
    @Override
    public void invokeStaticMethod(String method,
            Map<String, ? extends Object> parameters,
            final Callback callback) {
        AsyncHttpResponseHandler httpHandler = new CallbackHandler(callback);
        invokeStaticMethod(method, parameters, httpHandler);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the contract is not set
     * (see {@link #setContract(RestContract)})
     * or the adapter is not connected.
     */
    @Override
    public void invokeStaticMethod(String method,
                                   Map<String, ? extends Object> parameters,
                                   final BinaryCallback callback) {
        AsyncHttpResponseHandler httpHandler = new BinaryHandler(callback);
        invokeStaticMethod(method, parameters, httpHandler);
    }

    private void invokeStaticMethod(String method, Map<String, ? extends Object> parameters, AsyncHttpResponseHandler httpHandler) {
        if (contract == null) {
            throw new IllegalStateException("Invalid contract");
        }

        String verb = contract.getVerbForMethod(method);
        String path = contract.getUrlForMethod(method, parameters);
        ParameterEncoding parameterEncoding = contract.getParameterEncodingForMethod(method);

        request(path, verb, parameters, parameterEncoding, httpHandler);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the contract is not set
     * (see {@link #setContract(RestContract)})
     * or the adapter is not connected.
     */
    @Override
    public void invokeInstanceMethod(String method,
            Map<String, ? extends Object> constructorParameters,
            Map<String, ? extends Object> parameters,
            final Callback callback) {
        AsyncHttpResponseHandler httpHandler = new CallbackHandler(callback);
        invokeInstanceMethod(method, constructorParameters, parameters, httpHandler);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the contract is not set
     * (see {@link #setContract(RestContract)})
     * or the adapter is not connected.
     */
    @Override
    public void invokeInstanceMethod(String method,
                                     Map<String, ? extends Object> constructorParameters,
                                     Map<String, ? extends Object> parameters,
                                     final BinaryCallback callback) {
        AsyncHttpResponseHandler httpHandler = new BinaryHandler(callback);
        invokeInstanceMethod(method, constructorParameters, parameters, httpHandler);
    };

    private void invokeInstanceMethod(String method,
                                      Map<String, ? extends Object> constructorParameters,
                                      Map<String, ? extends Object> parameters,
                                      AsyncHttpResponseHandler httpHandler) {
        if (contract == null) {
            throw new IllegalStateException("Invalid contract");
        }

        Map<String, Object> combinedParameters = new HashMap<String, Object>();
        if (constructorParameters != null) {
            combinedParameters.putAll(constructorParameters);
        }
        if (parameters != null) {
            combinedParameters.putAll(parameters);
        }

        String verb = contract.getVerbForMethod(method);
        String path = contract.getUrlForMethod(method, combinedParameters);
        ParameterEncoding parameterEncoding = contract.getParameterEncodingForMethod(method);

        request(path, verb, combinedParameters, parameterEncoding, httpHandler);
    }

    private void request(String path,
                         String verb,
                         Map<String, ? extends Object> parameters,
                         ParameterEncoding parameterEncoding,
                         AsyncHttpResponseHandler responseHandler) {

        if (!isConnected()) {
            throw new IllegalStateException("Adapter not connected");
        }

        client.request(verb, path, parameters, parameterEncoding, responseHandler);
    }

    class CallbackHandler extends AsyncHttpResponseHandler {
        private final Callback callback;

        public CallbackHandler(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void onSuccess(int status, Header[] headers, byte[] body) {
            try {
                String response = body == null ? null : new String(body, getCharset());
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "Success (string): " + response);
                callback.onSuccess(response);
            } catch (Throwable t) {
                callback.onError(t);
            }
        }

        @Override
        public void onFailure(int statusCode,
                              org.apache.http.Header[] headers,
                              byte[] responseBody,
                              java.lang.Throwable error) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                String message;
                if (error != null) {
                    message = error.toString();
                } else {
                    message = statusCode + "\n";
                    try {
                        message += new String(responseBody, getCharset());
                    } catch (UnsupportedEncodingException e) {
                        message += new String(responseBody);
                    }
                }
                Log.w(TAG, "HTTP request (string) failed: " + message);
            }
            callback.onError(error);
        }
    }

    class BinaryHandler extends BinaryHttpResponseHandler {
        private final BinaryCallback callback;

        public BinaryHandler(BinaryCallback callback) {
            super(new String[]{ ".*" });
            this.callback = callback;
        }

        @Override
        public void onFailure(int statusCode,
                              org.apache.http.Header[] headers,
                              byte[] responseBody,
                              java.lang.Throwable error) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                String message;
                if (error != null) {
                    message = error.toString();
                } else {
                    message = statusCode + "\n";
                    try {
                        message += new String(responseBody, getCharset());
                    } catch (UnsupportedEncodingException e) {
                        message += new String(responseBody);
                    }
                }
                Log.w(TAG, "HTTP request (binary) failed: " + message);
            }
            callback.onError(error);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
            if (Log.isLoggable(TAG, Log.DEBUG))
                Log.d(TAG, "Success (binary): " + binaryData.length + " bytes");
            try {
                String contentType = null;
                for (Header h: headers) {
                    if (h.getName().equalsIgnoreCase("content-type"))
                        contentType = h.getValue();
                }
                callback.onSuccess(binaryData, contentType);
            } catch (Throwable t) {
                callback.onError(t);
            }
        }
    }

    //
    // Mimic AFNetworking as much as possible.
    //
    // Internally, it's using "Android Asynchronous Http Client".
    // http://loopj.com/android-async-http/
    // The benefit is connection pools, persistent cookies,
    // an asynchronous API, Android bug workarounds, etc.
    // The drawback is it doesn't support HEAD or OPTION.
    //

    enum ParameterEncoding {
        FORM_URL,
        JSON,
        FORM_MULTIPART
    }

    private static class RestHttpClient extends AsyncHttpClient {

        private static String getVersionName(Context context) {
            String appVersion = null;
            try {
                PackageInfo pinfo = context.getPackageManager().getPackageInfo(
                		context.getPackageName(), 0);
                appVersion = pinfo.versionName;
            }
            catch (NameNotFoundException e) {
                // Do nothing
            }
            return (appVersion != null) ? appVersion : "";
        }

        private static String getDeviceName() {
            String deviceName = android.os.Build.MODEL;
            if (deviceName == null || deviceName.length() == 0) {
                deviceName = android.os.Build.DEVICE;
                if (deviceName == null || deviceName.length() == 0) {
                    deviceName = "Unknown";
                }
            }
            return deviceName;
        }

        private Context context;
        private String baseUrl;

        public RestHttpClient(Context context, String baseUrl) {
            if (baseUrl == null) {
                throw new IllegalArgumentException(
                		"The baseUrl cannot be null");
            }

            this.context = context;
            this.baseUrl = baseUrl;

            // Make sure base url ends with a trailing slash.
            if (!this.baseUrl.endsWith("/")) {
                this.baseUrl += "/";
            }

            // More useful User-Agent, similar to AFNetworing.
            String appName;
            if (context != null) {
                String appPackageName = context.getPackageName();
                String appVersion = getVersionName(context);
                appName = appPackageName + "/" + appVersion;
            }
            else {
                appName = "StongLoopRemoting App";
            }
            String deviceName = getDeviceName();
            String androidVersion = android.os.Build.VERSION.RELEASE +
            		"/API-" + android.os.Build.VERSION.SDK_INT;
            String userAgent = appName + " (" + deviceName +
            		" Android " + androidVersion + ")";
            setUserAgent(userAgent);
        }

        public void request(String method, String path,
                Map<String, ? extends Object> parameters,
                ParameterEncoding parameterEncoding,
                final AsyncHttpResponseHandler httpCallback) {
            Uri.Builder uri = Uri.parse(baseUrl).buildUpon();
            if (path != null) {
                if (path.startsWith("/")) {
                    uri.appendEncodedPath(path.substring(1));
                }
                else {
                    uri.appendEncodedPath(path);
                }
            }
            AbstractHttpEntity body = null;
            RequestParams requestParams = null;
            String charset = "utf-8";

            if (parameters != null) {
                if ("GET".equalsIgnoreCase(method) ||
                        "HEAD".equalsIgnoreCase(method) ||
                        "DELETE".equalsIgnoreCase(method)) {

                    try {
                        for (Map.Entry<String, ? extends Object> entry :
                                flattenParameters(parameters).entrySet()) {
                            uri.appendQueryParameter(entry.getKey(),
                                    String.valueOf(entry.getValue()));
                        }
                    } catch(JSONException ex) {
                        // FIXME(bajtos) we should rethrow
                        Log.e(TAG, "Couldn't convert parameters to JSON", ex);
                    }

                }
                else if (parameterEncoding == ParameterEncoding.FORM_URL) {
                	// NOTE: Code for "x-www-form-urlencoded" is not used
                	// and is untested.
                    List<NameValuePair> nameValuePairs =
                    		new ArrayList<NameValuePair>();
                    for (Map.Entry<String, ? extends Object> entry :
                    	parameters.entrySet()) {
                        nameValuePairs.add(
                        		new BasicNameValuePair(entry.getKey(),
                        				String.valueOf(entry.getValue())));
                    }
                    try {
                        body = new UrlEncodedFormEntity(nameValuePairs,
                        		charset);
                        body.setContentType(
                                "application/x-www-form-urlencoded; charset=" + charset);
                    }
                    catch (UnsupportedEncodingException e) {
                        // Won't happen
                        Log.e(TAG, "Couldn't encode url params", e);
                    }
                }
                else if (parameterEncoding == ParameterEncoding.FORM_MULTIPART) {
                    if (!"POST".equalsIgnoreCase(method)) {
                        throw new UnsupportedOperationException(
                                "RestAdapter does not support multipart PUT requests");
                    }

                    try {
                        requestParams = buildRequestParameters(
                                flattenParameters(parameters));

                    } catch(JSONException e) {
                        // FIXME(bajtos) we should rethrow
                        Log.e(TAG, "Couldn't convert parameters to JSON", e);
                    } catch (FileNotFoundException e1) {
                        throw new IllegalArgumentException("Invalid File parameter");
                    }
                }
                else if (parameterEncoding == ParameterEncoding.JSON) {
                    String s = "";
                    try {
                        s = String.valueOf(JsonUtil.toJson(parameters));
                    }
                    catch (JSONException e) {
                        // FIXME(bajtos) we should rethrow
                        Log.e(TAG, "Couldn't convert parameters to JSON", e);
                    }
                    try {
                        body = new StringEntity(s, charset);
                        body.setContentType("application/json; charset=" + charset);
                    }
                    catch (UnsupportedEncodingException e) {
                        // Won't happen
                        Log.e(TAG, "Couldn't encode JSON params", e);
                    }
                }

            }

            Header[] headers = {
                    new BasicHeader("Accept", "application/json"),
            };

            String url = uri.build().toString();
            logRequest(method, url, body, requestParams);

            if ("GET".equalsIgnoreCase(method)) {
                get(context, url, headers, null, httpCallback);
            }
            else if ("DELETE".equalsIgnoreCase(method)) {
                delete(context, url, headers, httpCallback);
            }
            else if ("POST".equalsIgnoreCase(method)) {
                if (requestParams != null)
                    post(context, url, headers, requestParams, null, httpCallback);
                else
                    post(context, url, headers, body, null, httpCallback);
            }
            else if ("PUT".equalsIgnoreCase(method)) {
                put(context, url, headers, body, null, httpCallback);
            }
            else {
                throw new IllegalArgumentException("Illegal method: " +
                        method + ". Only GET, POST, PUT, DELETE supported.");
            }
        }

        private void logRequest(String method, String url, AbstractHttpEntity body, RequestParams requestParams) {
            if (!Log.isLoggable(TAG, Log.DEBUG)) return;
            Log.d(TAG, method + " " + url);
            if (requestParams != null)
                Log.d(TAG, requestParams.toString());
        }

        private Map<String, Object> flattenParameters(
                final Map<String, ? extends Object> parameters) throws JSONException {

            // This method converts nested objects/arrays in to JSON strings
            //   Input:  { "here": { "lat": 10, "lng": 20 } }
            //   Output: { "here": "{\"lat\":10,\"lng\":20}" }
            // NOTE(bajtos) while it's possible to encode nested values using
            // qs syntax like here[lat]=10&here[lng]=20, the encoding gets rather
            // complex quickly, especially when arrays are involved

            Map<String, Object> result = new HashMap<String, Object>();

            for (Map.Entry<String, ? extends Object> entry
                    : parameters.entrySet()) {

                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof Map || value instanceof List) {
                    result.put(key, String.valueOf(JsonUtil.toJson(value)));
                } else {
                    result.put(key, value);
                }
            }

            return result;
        }

        static protected RequestParams buildRequestParameters(
                Map<String, ? extends Object> parameters) throws FileNotFoundException
        {
            RequestParams requestParams = new RequestParams();

            for (Map.Entry<String, ? extends Object> entry :
                    parameters.entrySet()) {
                Object value = entry.getValue();
                if ( value != null ) {
                    if ( value instanceof java.io.File ) {
                        requestParams.put(entry.getKey(), (java.io.File)value);
                    }
                    else if (value instanceof StreamParam) {
                        ((StreamParam) value).putTo(requestParams, entry.getKey());
                    }
                    else if ( value instanceof String ) {
                        requestParams.put(entry.getKey(), (String) entry.getValue());
                    }
                    else {
                        throw new IllegalArgumentException(
                                "Unknown param type for RequestParams: "
                                        + value.getClass().getName());
                    }
                }
            }

            return requestParams;
        }
    }
}
