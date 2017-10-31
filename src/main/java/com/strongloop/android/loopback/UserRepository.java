package com.strongloop.android.loopback;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.strongloop.android.loopback.callbacks.ObjectCallback;
import com.strongloop.android.loopback.callbacks.VoidCallback;
import com.strongloop.android.remoting.JsonUtil;
import com.strongloop.android.remoting.adapters.Adapter;
import com.strongloop.android.remoting.adapters.RestContract;
import com.strongloop.android.remoting.adapters.RestContractItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A base class implementing {@link ModelRepository} for the built-in User type.
 * <p>
 * <pre>{@code
 * UserRepository<MyUser> userRepo = new UserRepository<MyUser>("user", MyUser.class);
 * }</pre>
 * <p>
 * Most application are extending the built-in User model and adds new properties
 * like address, etc. You should create your own Repository class
 * by extending this base class in such case.
 * <p>
 * <pre>{@code
 * public class Customer extends User {
 *   // your custom properties and prototype (instance) methods
 * }
 *
 * public class CustomerRepository extends UserRepository<Customer> {
 *     public interface LoginCallback extends LoginCallback<Customer> {
 *     }
 *
 *     public CustomerRepository() {
 *         super("customer", null, Customer.class);
 *     }
 *
 *     // your custom static methods
 * }
 * }</pre>
 */
public class UserRepository<U extends User> extends ModelRepository<U> {
    public static final String SHARED_PREFERENCES_NAME =
            RestAdapter.class.getCanonicalName();
    public static final String PROPERTY_CURRENT_USER_ID = "currentUserId";

    private AccessTokenRepository accessTokenRepository;
    private Object currentUserId;
    private boolean isCurrentUserIdLoaded;
    private U cachedCurrentUser;

    private AccessTokenRepository getAccessTokenRepository() {
        if (accessTokenRepository == null) {
            accessTokenRepository = getRestAdapter()
                    .createRepository(AccessTokenRepository.class);
        }
        return accessTokenRepository;
    }

    /**
     * Creates a new UserRepository, associating it with
     * the static {@code U} user class and the user class name.
     * @param className The remote class name.
     * @param userClass The User (sub)class. It must have a public no-argument constructor.
     */
    public UserRepository(String className, Class<U> userClass) {
        this(className, null, userClass);
    }

    /**
     * Creates a new UserRepository, associating it with
     * the static {@code U} user class and the user class name.
     * @param className The remote class name.
     * @param nameForRestUrl The pluralized class name to use in REST transport.
     *                       Use {@code null} for the default value, which is the plural
     *                       form of className.
     * @param userClass The User (sub)class. It must have a public no-argument constructor.
     */
    public UserRepository(String className, String nameForRestUrl, Class<U> userClass) {
        super(className, nameForRestUrl, userClass);
    }

    /**
     * @return Id of the currently logged in user. null when there is no user logged in.
     */
    public Object getCurrentUserId() {
        loadCurrentUserIdIfNotLoaded();
        return currentUserId;
    }

    protected void setCurrentUserId(Object currentUserId) {
        this.currentUserId = currentUserId;
        cachedCurrentUser = null;
        saveCurrentUserId();
    }

    /**
     * Fetch the data of the currently logged in user. Invokes
     * {@code callback.onSuccess(null)} when no user is logged in.
     * The data is cached, see {@link #getCachedCurrentUser()}
     * @param callback success/error callback
     */
    public void findCurrentUser(final ObjectCallback<U> callback) {
        if (getCurrentUserId() == null) {
            callback.onSuccess(null);
            return;
        }

        this.findById(getCurrentUserId(), new ObjectCallback<U>() {
            @Override
            public void onSuccess(U user) {
                cachedCurrentUser = user;
                callback.onSuccess(user);
            }

            @Override
            public void onError(Throwable t) {
                callback.onError(t);
            }
        });
    }

    /**
     * Get the cached value of the currently logged in user.
     * The value is updated by {@link #findCurrentUser(ObjectCallback)},
     * {@link #loginUser(String, String, LoginCallback)} and
     * {@link #logout(VoidCallback)}
     *
     * <p>
     * The typical usage:
     * <ul>
     * <li>
     * At the application start up and after a successfull login,
     * {@link #findCurrentUser(ObjectCallback)} is called to pre-load the data.
     * </li>
     * <li>
     * All other places call {@link #getCachedCurrentUser()} to access the data
     * of the currently logged in user.
     * </li>
     * </ul>
     *
     * @return The current user or null.
     */
    public U getCachedCurrentUser() {
        return cachedCurrentUser;
    }

    /**
     * Callback passed to loginUser to receive success and newly created
     * {@code U} user instance or thrown error.
     */
    public interface LoginCallback<U> {
        public void onSuccess(AccessToken token, U currentUser);
        public void onError(Throwable t);
    }

    /**
     * Creates a {@code U} user instance given an email and a password.
     * @param email
     * @param password
     * @return A {@code U} user instance.
     */
    public U createUser(String email, String password)     {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("email", email);
        map.put("password", password);
        U user = createObject(map);
        return user;
    }

     /**
     * Creates a {@link com.strongloop.android.remoting.adapters.RestContract} representing the user type's custom
     * routes. Used to extend an {@link com.strongloop.android.remoting.adapters.Adapter} to support user. Calls
     * super {@link ModelRepository} createContract first.
     *
     * @return A {@link com.strongloop.android.remoting.adapters.RestContract} for this model type.
     */

    public RestContract createContract() {
        RestContract contract = super.createContract();

        String className = getClassName();

        contract.addItem(new RestContractItem("/" + getNameForRestUrl() + "/login?include=user", "POST"),
                className + ".login");
        contract.addItem(new RestContractItem("/" + getNameForRestUrl() + "/logout", "POST"),
                className + ".logout");
        contract.addItem(new RestContractItem("/" + getNameForRestUrl() + "/change-password", "POST"),
                className + ".change-password");
        return contract;
    }

    /**
     * Creates a new {@code U} user given the email, password and optional parameters.
     * @param email - user email
     * @param password - user password
     * @param parameters - optional parameters
     * @return A new {@code U} user instance.
     */
    public U createUser(String email, String password,
            Map<String, ? extends Object> parameters) {

        HashMap<String, Object> allParams = new HashMap<String, Object>();
        allParams.putAll(parameters);
        allParams.put("email", email);
        allParams.put("password", password);
        U user = createObject(allParams);

        return user;
    }

    /**
     * Login a user given an email and password.
     * Creates a {@link AccessToken} and {@code U} user models if successful.
     * @param email - user email
     * @param password - user password
     * @param callback - success/error callback
     */
    public void loginUser(String email, String password,
            final LoginCallback<U> callback) {

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
                        AccessToken token = getAccessTokenRepository()
                                .createObject(JsonUtil.fromJson(response));
                        getRestAdapter().setAccessToken(token.getId().toString());

                        JSONObject userJson = response.optJSONObject("user");
                        U user = userJson != null
                                ? createObject(JsonUtil.fromJson(userJson))
                                : null;

                        setCurrentUserId(token.getUserId());
                        cachedCurrentUser = user;
                        callback.onSuccess(token, user);
                    }
                });
    }

    /**
     * Change the current user's password
     * <p>
     * @param oldPassword The current password.
     * @param newPassword The new Password.
     * @param callback The callback to be executed when finished.
     */
    public void changePassword(String oldPassword, String newPassword,
                               final VoidCallback callback) {

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("oldPassword",  oldPassword);
        params.put("newPassword",  newPassword);

        invokeStaticMethod("change-password", params,
                new Adapter.Callback() {

                    @Override
                    public void onError(Throwable t) {
                        callback.onError(t);
                    }

                    @Override
                    public void onSuccess(String response) {
                        callback.onSuccess();
                    }
                });
    }

    /**
     * Logs the current user out of the server and removes the access
     * token from the system.
     * <p>
     * @param callback The callback to be executed when finished.
     */

    public void logout(final VoidCallback callback) {

        invokeStaticMethod("logout", null,
                new Adapter.Callback() {

            @Override
            public void onError(Throwable t) {
                callback.onError(t);
            }

            @Override
            public void onSuccess(String response) {
                RestAdapter radapter = getRestAdapter();
                radapter.clearAccessToken();
                setCurrentUserId(null);
                callback.onSuccess();
            }
        });
    }

    private void saveCurrentUserId() {
        final SharedPreferences.Editor editor = getSharedPreferences().edit();
        final String json = new JSONArray().put(getCurrentUserId()).toString();
        editor.putString(PROPERTY_CURRENT_USER_ID, json);
        editor.commit();
    }

    private void loadCurrentUserIdIfNotLoaded() {
        if (isCurrentUserIdLoaded) return;
        isCurrentUserIdLoaded = true;

        String json = getSharedPreferences().getString(PROPERTY_CURRENT_USER_ID, null);
        if (json == null) return;

        try {
            Object id = new JSONArray(json).get(0);
            setCurrentUserId(id);
        } catch (JSONException e) {
            String msg = "Cannot parse current user id '" + json + "'";
            Log.e("LoopBack", msg, e);
        }
    }

    private SharedPreferences getSharedPreferences() {
        return getApplicationContext().getSharedPreferences(
                SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }

}
