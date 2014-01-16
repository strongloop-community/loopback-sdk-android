package com.strongloop.android.loopback;

import java.util.Map;

import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.strongloop.android.remoting.JsonUtil;
import com.strongloop.android.remoting.adapters.Adapter;
import com.strongloop.android.remoting.adapters.RestContract;
import com.strongloop.android.remoting.adapters.RestContractItem;

/**
 * Locally create or retrieves a user type from the server, encapsulating
 * the name of the user type for easy {@link User} creation, discovery, and
 * management.
 */
public class UserRepository extends ModelRepository<User> {
	
	private AccessTokenRepository accessTokenRepository;

	public UserRepository() {
		super("user", User.class);
	}
	
    public interface LoginCallback {
        public void onSuccess(User user);
        public void onError(Throwable t);
    }

	public User createUser() {
		User user = createModel(null);
		return user;	
	}
	
	/**
	 * Creates a {@link User} given an email and a password.
	 * @param email
	 * @param password
	 * @return A {@link User}.
	 */
	public User createUser(String email, String password) 	{
		User user = createModel(ImmutableMap.of("email", email, 
				"password", password));
		user.setRepository(this);
		return user;
	}
	
	 /**
     * Creates a {@link RestContract} representing the user type's custom
     * routes. Used to extend an {@link Adapter} to support user. Calls
     * super {@link ModelRepository) createContract first. 
     *
     * @return A {@link RestContract} for this model type.
     */
 	
    public RestContract createContract() {
    	RestContract contract = super.createContract();
    	
        String className = getClassName();

        contract.addItem(new RestContractItem("/" + getNameForRestUrl() + "/login", "POST"),
                className + ".login");
        contract.addItem(new RestContractItem("/" + getNameForRestUrl() + "/logout", "GET"),
        		className + ".logout");
        return contract;
    }
	
    /**
     * Creates a new {@link User} given the email, password and optional parameters.
     * @param email - user email
     * @param password - user password
     * @param parameters - optional parameters
     * @return A new {@link User}
     */
	public User createUser(String email, String password, 
			Map<String, ? extends Object> parameters) {

		ImmutableMap<String, Object> allParams = 
				new ImmutableMap.Builder<String, Object>()
				.putAll(parameters)
				.put("email", email)
				.put("password", password)
				.build();
		
		User user = createModel(allParams);
		
		return user;	
	}
		
	/**
	 * Login a user given an email and password. Creates a User model if successful.
	 * @param email - user email
	 * @param password - user password
	 * @param callback - success/error callback
	 */
	public void loginUser(String email, String password, 
			final LoginCallback callback) {
		
		Map<String, ?extends Object> params = ImmutableMap.of("email", email, 
				"password", password);
		
        invokeStaticMethod("login", params,
                new Adapter.JsonObjectCallback() {

            @Override
            public void onError(Throwable t) {
                callback.onError(t);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (response == null) {
                    // Not found
                    callback.onSuccess(null);
                }
                else {
            		RestAdapter radapter = (RestAdapter)getAdapter();
                	if ( accessTokenRepository == null )
                	{
                		accessTokenRepository = radapter.createRepository(AccessTokenRepository.class);
                	}
                	AccessToken accessTokenModel = accessTokenRepository.createModel(JsonUtil.fromJson(response));
                	radapter.setAccessToken(accessTokenModel.getId());
                	
                	getUser(accessTokenModel, callback);
                }
            }
		
        });
		
	}
	
	public void getUser(AccessToken accessTokenModel, 
			final LoginCallback callback ) {

		Map<String, ?extends Object> params = ImmutableMap.of("id", accessTokenModel.getUserId(),
				"access_token", accessTokenModel.getId()); 
		
        invokeStaticMethod("findById", params,
                new Adapter.JsonObjectCallback() {

            @Override
            public void onError(Throwable t) {
                callback.onError(t);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (response == null) {
                    // Not found
                    callback.onSuccess(null);
                }
                else {
                	callback.onSuccess(
                			createModel(JsonUtil.fromJson(response)));
                }
            }
		
        });
		
	}
	
}
