package com.strongloop.android.loopback;

import java.util.Map;

import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.strongloop.android.remoting.Repository;
import com.strongloop.android.remoting.adapters.Adapter;

/**
 * A local representative of a single user instance on the server. Derived from Model,
 * the data is immediately accessible locally, but can be saved, destroyed, etc. from the
 * server easily. 
 */

public class User extends Model {

    public User(Repository repository,
			Map<String, ? extends Object> creationParameters) {
		super(repository, creationParameters);
	}

	public User() {
		this(null, null);
	}
	
    /**
     * Logs the User out of the server.
     * <p>
     * @param callback The callback to be executed when finished.
     */
 	
	public void logout(final Callback callback) {
		
		String accessToken = getId().toString();
		Repository repository = getRepository();
		ImmutableMap<String, ?extends Object> parameters = ImmutableMap.of("access_token", accessToken);

        repository.invokeStaticMethod("logout", parameters,
                new Adapter.JsonObjectCallback() {

            @Override
            public void onError(Throwable t) {
                callback.onError(t);
            }

            @Override
            public void onSuccess(JSONObject response) {
                callback.onSuccess();
            }
        });
	}
	
    
}
	

