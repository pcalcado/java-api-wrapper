package com.soundcloud.api.examples;


import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Http;
import com.soundcloud.api.Request;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.io.File;

/**
 * Access a protected resource from the server. This needs a prepared (serialized) API
 * wrapper instance (create one with CreateWrapper).
 *
 * @see CreateWrapper
 */
public final class GetResource {
    public static void main(String[] args) throws Exception {
        final File wrapperFile = CreateWrapper.WRAPPER_SER;

        if (args.length == 0) {
            System.err.println("GetResource resource");
            System.exit(1);
        } else if (!wrapperFile.exists()) {
            System.err.println("\nThe serialised wrapper (" + wrapperFile + ") does not exist.\n" +
                    "Run CreateWrapper first to create it.");
            System.exit(1);
        } else {
            final ApiWrapper wrapper = ApiWrapper.fromFile(wrapperFile);
            final Request resource = Request.to(args[0]);
            System.out.println("GET " + resource);
            try {
                HttpResponse resp = wrapper.get(resource);
                if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    System.out.println("\n" + Http.getJSON(resp).toString(4));
                } else {
                    System.err.println("Invalid status received: " + resp.getStatusLine());
                }
            } finally {
                // serialise wrapper state again (token might have been refreshed)
                wrapper.toFile(wrapperFile);
            }
        }
    }
}
