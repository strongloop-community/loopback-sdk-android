package com.strongloop.android.loopback.test;

import android.util.Log;

import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.loopback.User;
import com.strongloop.android.loopback.AccessToken;
import com.strongloop.android.loopback.UserRepository;

public class UserTest extends AsyncTestCase {

    private RestAdapter adapter;
    private CustomerRepository customerRepo;

    static final private String uid = String.valueOf(new java.util.Date().getTime());
    static final private String userEmail = uid + "@test.com";
    static final private String userPassword = "testpassword";

    /**
     * Custom sub-class of User.
     */
    public static class Customer extends User {
    }

    /**
     * Repository for our custom User sub-class.
     */
    public static class CustomerRepository extends UserRepository<Customer> {
        public interface LoginCallback extends UserRepository.LoginCallback<Customer> {
        }

        public CustomerRepository() {
            super("customer", null, Customer.class);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        adapter = createRestAdapter();
        customerRepo = adapter.createRepository(CustomerRepository.class);
    }

    // create and save
    public void testCreateSave() throws Throwable {

        // create user
       final Customer user = customerRepo.createUser(userEmail, userPassword);

        assertEquals(userEmail, user.getEmail());
        assertEquals(userPassword, user.getPassword());
        assertNull(user.getId());

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                user.save(new VoidTestCallback() {

                    @Override
                    public void onSuccess() {
                        assertNotNull(user.getId());
                        Log.i("UserTest: create/save", "id: " + user.getId());
                        notifyFinished();
                    }
                });
            }
        });
    }

    // login / logout
    public void testLoginLogout() throws Throwable {
        // Login the user using the repository
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {

                customerRepo.loginUser(userEmail, userPassword,
                        new CustomerRepository.LoginCallback() {

                            @Override
                            public void onError(Throwable t) {
                                notifyFailed(t);
                            }

                            @Override
                            public void onSuccess(AccessToken token, Customer currentUser) {
                                assertNotNull("currentUser should be not null", currentUser);
                                assertEquals("currentUser.email", currentUser.getEmail(), userEmail);
                                assertNotNull("accessToken should be not null", token);
                                assertEquals("userId", token.getUserId(), currentUser.getId());
                                Log.i("UserTest", "login id: " + currentUser.getId());
                                notifyFinished();
                            }
                        });
               }

        });

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {

                customerRepo.logout(new VoidTestCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i("UserTest", "logout succeeded");
                        notifyFinished();
                    }
                });

            }
        });

    }
}
