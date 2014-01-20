package com.strongloop.android.loopback;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

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

	/**
	 * Creates a new UserRepository, associating it with the static {@link User}
	 * model class and the user class name.
	 */
	public UserRepository() {
		super("user", User.class);
	}
	
	/**
	 * Callback passed to loginUser to receive success and newly created
	 * {@link User} instance or thrown error. 
	 */
    public interface LoginCallback {
        public void onSuccess(User user);
        public void onError(Throwable t);
    }

	/**
	 * Creates a {@link User} given an email and a password.
	 * @param email
	 * @param password
	 * @return A {@link User}.
	 */
	public User createUser(String email, String password) 	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("email", email);
		map.put("password", password);
		User user = createModel(map);
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

		HashMap<String, Object> allParams = new HashMap<String, Object>();
		allParams.putAll(parameters);
		allParams.put("email", email);
		allParams.put("password", password);
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
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("email",  email);
		params.put("password",  password);
		
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

	/**
	 * Retrieves a User given an {@link AccessToken}. This is normally used
	 * internally by loginUser.
	 * @param accessTokenModel - access token model.
	 * @param callback - A {@LoginCallback} to get successfully created 
	 * {@link User} or an error.
	 */
	public void getUser(AccessToken accessTokenModel, 
			final LoginCallback callback ) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", accessTokenModel.getUserId());
		params.put("access_token", accessTokenModel.getId());
		
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
