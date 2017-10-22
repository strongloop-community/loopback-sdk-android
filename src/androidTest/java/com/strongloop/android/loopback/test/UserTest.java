package com.strongloop.android.loopback.test;

import android.content.Context;
import android.util.Log;

import com.strongloop.android.loopback.AccessToken;
import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.loopback.User;
import com.strongloop.android.loopback.UserRepository;

public class UserTest extends AsyncTestCase {

    private RestAdapter adapter;
    private CustomerRepository customerRepo;

    static final private String uid = String.valueOf(new java.util.Date().getTime());
    static final private String userEmail = uid + "@test.com";
    static final private String userPassword = "testpassword";
    static final private String newUserPassword = "newtestpassword";

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
        testContext.clearSharedPreferences(
                CustomerRepository.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
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
        final Customer user = givenCustomer();

        // Login the user using the repository
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {

                customerRepo.loginUser(user.getEmail(), userPassword,
                        new CustomerRepository.LoginCallback() {

                            @Override
                            public void onError(Throwable t) {
                                notifyFailed(t);
                            }

                            @Override
                            public void onSuccess(AccessToken token, Customer currentUser) {
                                assertNotNull("currentUser should be not null", currentUser);
                                assertEquals("currentUser.email", currentUser.getEmail(), user.getEmail());
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

    public void testSetsCurrentUserIdOnLogin() throws Throwable {
        Customer user = givenLoggedInCustomer();
        assertEquals(user.getId(), customerRepo.getCurrentUserId());
    }

    public void testClearsCurrentUserIdOnLogout() throws Throwable {
        givenLoggedInCustomer();
        logout();
        assertNull(customerRepo.getCurrentUserId());
    }

    public void testCurrentUserIdIsStoredInSharedPreferences() throws Throwable {
        Customer customer = givenLoggedInCustomer();
        CustomerRepository anotherRepo = adapter.createRepository(
                CustomerRepository.class);

        assertEquals(customer.getId(), anotherRepo.getCurrentUserId());
    }

    public void testLoginAndChangeCurrentUserPassword() throws Throwable {
        final Customer user = givenCustomer();

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {

                customerRepo.loginUser(user.getEmail(), userPassword,
                        new CustomerRepository.LoginCallback() {

                            @Override
                            public void onError(Throwable t) {
                                notifyFailed(t);
                            }

                            @Override
                            public void onSuccess(AccessToken token, Customer currentUser) {
                                assertNotNull("accessToken should be not null", token);
                                assertEquals("userId", token.getUserId(), currentUser.getId());
                                Log.i("UserTest", "login id: " + currentUser.getId());
                                notifyFinished();
                            }
                        });
            }

        });

        // Change user's password
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                customerRepo.changePassword(userPassword, newUserPassword, new VoidTestCallback(){
                    @Override
                    public void onSuccess() {
                        Log.i("UserTest", "Password Changed");
                        notifyFinished();
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.i("UserTest", "Password Change Failed");
                        notifyFailed(t);
                    }
                });
            }

        });
    }

    public void testFindCurrentUserReturnsCorrectValue() throws Throwable {
        final Customer customer = givenLoggedInCustomer();

        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                customerRepo.findCurrentUser(new ObjectTestCallback<Customer>() {
                    @Override
                    public void onSuccess(Customer current) {
                        assertEquals("id", customer.getId(), current.getId());
                        assertEquals("email", customer.getEmail(), current.getEmail());
                        notifyFinished();
                    }
                });
            }
        });
    }

    public void testFindCurrentUserReturnsNullWhenNotLoggedIn() throws Throwable {
        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                customerRepo.findCurrentUser(new ObjectTestCallback<Customer>() {
                    @Override
                    public void onSuccess(Customer current) {
                        assertNull(current);
                        notifyFinished();
                    }
                });
            }
        });
    }

    public void testGetCachedCurrentUserReturnsNullInitially() throws Throwable {
        Customer current = customerRepo.getCachedCurrentUser();
        assertNull(current);
    }

    public void testGetCachedCurrentUserReturnsValueLoadedByFindCurrentUser()
            throws Throwable {
        givenLoggedInCustomer();
        Customer current = findCurrentUser();

        Customer cached = customerRepo.getCachedCurrentUser();
        assertEquals(current, cached);
    }

    public void testGetCachedCurrentUserReturnsValueLoadedByLogin() throws Throwable {
        Customer current = givenLoggedInCustomer();

        Customer cached = customerRepo.getCachedCurrentUser();

        assertNotNull(cached);
        assertEquals(current.getId(), cached.getId());
    }

    public void testCachedCurrentUserIsClearedOnLogout() throws Throwable {
        givenLoggedInCustomer();
        findCurrentUser();
        logout();

        Customer cached = customerRepo.getCachedCurrentUser();
        assertNull(cached);
    }

    static int counter = 0;
    private Customer givenCustomer() throws Throwable {
        String email = uid + "-" + (++counter) + "@example.com";
        final Customer customer = customerRepo.createUser(email, userPassword);

        await(new AsyncTask() {
            @Override
            public void run() {
                customer.save(new VoidTestCallback());
            }
        });
        return customer;
    }

    private Customer givenLoggedInCustomer() throws Throwable {
        final Customer customer = givenCustomer();

        await(new AsyncTask() {
            @Override
            public void run() {
                customerRepo.loginUser(customer.getEmail(), userPassword,
                        new CustomerRepository.LoginCallback() {

                            @Override
                            public void onSuccess(AccessToken token, Customer currentUser) {
                                notifyFinished();
                           }

                            @Override
                            public void onError(Throwable t) {
                                Log.e("UserTest", "givenLoggedInCustomer failed", t);
                                notifyFailed(t);
                            }

                        }
                );
            }
        });

        return customer;
    }

    private void logout() throws Throwable {
        await(new AsyncTask() {
            @Override
            public void run() {
                customerRepo.logout(new VoidTestCallback());
            }
        });
    }

    private Customer findCurrentUser() throws Throwable {
        final Customer[] currentRef = new Customer[1];

        await(new AsyncTask() {
            @Override
            public void run() {
                customerRepo.findCurrentUser(new ObjectTestCallback<Customer>() {
                    @Override
                    public void onSuccess(Customer current) {
                        currentRef[0] = current;
                        notifyFinished();
                    }
                });
            }
        });

        return currentRef[0];
    }
}
