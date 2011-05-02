package com.soundcloud.api.examples;

import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.CloudAPI;
import com.soundcloud.api.Token;

import java.io.File;

/**
 * Creates an API wrapper instance, obtains an access token and serialises the wrapper to disk.
 * The serialised wrapper can then be used for subsequent access to resources without reauthenticating
 * @see GetResource
 */
public final class CreateWrapper {
    public static final File WRAPPER_SER = new File("wrapper.ser");

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("CreateWrapper client_id client_secret login password [sandbox|live]");
            System.exit(1);
        } else {
            final ApiWrapper wrapper = new ApiWrapper(
                    args[0] /* client_id */,
                    args[1] /* client_secret */,
                    null    /* redirect URI */,
                    null    /* token */,
                    args.length == 5 ? CloudAPI.Env.valueOf(args[4].toUpperCase()) : CloudAPI.Env.SANDBOX);

            Token token = wrapper.login(args[2] /* login */, args[3] /* password */);
            System.out.println("got token from server: " + token);

            // for this example the whole wrapper is serialised to disk -
            // in a real application you would just save the tokens and usually have the client_id/client_secret
            // hardcoded in the application
            wrapper.toFile(WRAPPER_SER);

            System.out.println("wrapper serialised to " + WRAPPER_SER);
        }
    }
}
