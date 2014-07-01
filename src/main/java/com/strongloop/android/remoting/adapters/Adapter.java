// Copyright (c) 2013 StrongLoop. All rights reserved.

package com.strongloop.android.remoting.adapters;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;

/**
 * The entry point to all networking accomplished with LoopBack. Adapters
 * encapsulate information consistent to all networked operations, such as base
 * URL, port, etc.
 */
public abstract class Adapter {

    /**
     * A callback receiving the HTTP response body in the binary
     * form.
     */
    public interface BinaryCallback {

        /**
         * The method invoked when the call completes successfully.
         * @param body The response body.
         * @param contentType The value of "Content-Type" response header.
         */
        public void onSuccess(byte[] body, String contentType);

        /**
         * The method invoked when an error occurs.
         * @param t The Throwable.
         */
        public void onError(Throwable t);
    }

    /**
     * A callback that returns the HTTP response body.
     */
    public interface Callback {

        /**
         * The method invoked when the call completes successfully.
         * @param response The HTTP response body.
         */
        public void onSuccess(String response);

        /**
         * The method invoked when an error occurs.
         * @param t The Throwable.
         */
        public void onError(Throwable t);
    }

    /**
     * A callback that parses a JSON response. If the return value
     * is not valid JSON, then the {@link #onError(Throwable)} method is called.
     */
    public abstract static class JsonCallback implements Callback {

    	/**
         * The method invoked when the call completes successfully. The
         * response is a {@link org.json.JSONObject},
         * {@link org.json.JSONArray}, String, Number, Boolean, or
         * {@link org.json.JSONObject#NULL}.
         * @param response The JSON object.
         */
        public abstract void onSuccess(Object response);

        @Override
        public void onSuccess(String response) {
        	if (response == null) {
        		onError(new JSONException("Invalid null response"));
        	}
        	else {
	            try {
	            	onSuccess(new JSONTokener(response).nextValue());
		        }
		        catch (JSONException e) {
		            onError(e);
		        }
        	}
        }
    }

    /**
     * A callback that returns a JSON object. If the return value is
     * not a JSON object or "null", then the {@link #onError(Throwable)} method
     * is called.
     */
    public abstract static class JsonObjectCallback extends JsonCallback {

        /**
         * The method invoked when the call completes successfully and the
         * response is a JSON object or <code>null</code> if the response
         * string is "null".
         * @param response The JSON object.
         */
        public abstract void onSuccess(JSONObject response);

        @Override
        public void onSuccess(Object response) {
        	if (response instanceof JSONObject) {
	            onSuccess((JSONObject)response);
        	}
        	else if (response == JSONObject.NULL) {
	            onSuccess((JSONObject)null);
    		}
        	else {
        		onError(new JSONException(
                		"Expecting a JSON object: " + response));
        	}
        }
    }

    /**
     * A callback that returns a JSON array. If the return value is
     * not a JSON array or "null", then the {@link #onError(Throwable)} method
     * is called.
     */
    public abstract static class JsonArrayCallback extends JsonCallback {

        /**
         * The method invoked when the call completes successfully and the
         * response is a JSON array or <code>null</code> if the response
         * string is "null".
         * @param response The JSON array.
         */
        public abstract void onSuccess(JSONArray response);

        @Override
        public void onSuccess(Object response) {
        	if (response instanceof JSONArray) {
	            onSuccess((JSONArray)response);
        	}
        	else if (response == JSONObject.NULL) {
	            onSuccess((JSONArray)null);
    		}
        	else {
        		onError(new JSONException(
                		"Expecting a JSON array: " + response));
        	}
        }
    }

    /**
     * Creates a new, disconnected Adapter.
     */
    public Adapter(Context context) {
        this(context, null);
    }

    /**
     * Creates a new Adapter, connecting it to `url`.
     * @param url The URL to connect to.
     */
    public Adapter(Context context, String url) {
        if (url != null) {
            connect(context, url);
        }
    }

    /**
     * Connects the Adapter to `url`.
     * @param url The URL to connect to.
     */
    public abstract void connect(Context context, String url);

    /**
     * Gets whether this adapter is connected to a server.
     * @return <code>true</code> if connected, <code>false</code> otherwise.
     */
    public abstract boolean isConnected();

    /**
     * Invokes a remotable method exposed statically on the server.
     * <p>
     * Unlike {@link #invokeInstanceMethod(String, Map, Map, Callback)}, no
     * object needs to be created on the server.
     * @param method The method to invoke, e.g.
     * 		<code>"module.doSomething"</code>.
     * @param parameters The parameters to invoke with.
     * @param callback The callback to invoke when the execution finishes.
     */
    public abstract void invokeStaticMethod(String method,
    		Map<String, ? extends Object> parameters, Callback callback);

    /**
     * Invokes a remotable method exposed statically on the server,
     * parses the response as binary data.
     * <p>
     * Unlike {@link #invokeInstanceMethod(String, Map, Map, BinaryCallback)}, no
     * object needs to be created on the server.
     * @param method The method to invoke, e.g.
     * 		<code>"module.doSomething"</code>.
     * @param parameters The parameters to invoke with.
     * @param callback The callback to invoke when the execution finishes.
     */
    public void invokeStaticMethod(String method,
                                   Map<String, ? extends Object> parameters,
                                   BinaryCallback callback) {
        throw new UnsupportedOperationException(
                getClass().getName() + " does not support binary responses.");
    }

    /**
     * Invokes a remotable method exposed within a prototype on the server.
     * <p>
     * This should be thought of as a two-step process. First, the server loads
     * or creates an object with the appropriate type. Then and only then is
     * the method invoked on that object. The two parameter dictionaries
     * correspond to these two steps: `creationParameters` for the former, and
     * `parameters` for the latter.
     *
     * @param method The method to invoke, e.g.
     * 		<code>"MyClass.prototype.doSomething"</code>.
     * @param constructorParameters The parameters the virtual object should be
     * created with.
     * @param parameters The parameters to invoke with.
     * @param callback The callback to invoke when the execution finishes.
     */
    public abstract void invokeInstanceMethod(String method,
    		Map<String, ? extends Object> constructorParameters,
    		Map<String, ? extends Object> parameters, Callback callback);

    /**
     * Invokes a remotable method exposed within a prototype on the server,
     * parses the response as binary data.
     * <p>
     * This should be thought of as a two-step process. First, the server loads
     * or creates an object with the appropriate type. Then and only then is
     * the method invoked on that object. The two parameter dictionaries
     * correspond to these two steps: `creationParameters` for the former, and
     * `parameters` for the latter.
     *
     * @param method The method to invoke, e.g.
     * 		<code>"MyClass.prototype.doSomething"</code>.
     * @param constructorParameters The parameters the virtual object should be
     * created with.
     * @param parameters The parameters to invoke with.
     * @param callback The callback to invoke when the execution finishes.
     */
    public void invokeInstanceMethod(String method,
                                     Map<String, ? extends Object> constructorParameters,
                                     Map<String, ? extends Object> parameters,
                                     BinaryCallback callback) {
        throw new UnsupportedOperationException(
                getClass().getName() + " does not support binary responses.");
    }

}
