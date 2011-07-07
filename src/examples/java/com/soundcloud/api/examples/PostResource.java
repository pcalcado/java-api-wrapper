package com.soundcloud.api.examples;

import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Http;
import com.soundcloud.api.Request;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class PostResource {
    public static void main(String[] args) throws Exception {
        final File wrapperFile = CreateWrapper.WRAPPER_SER;

        if (args.length < 2) {
            System.err.println("PostResource resource content [content-type]");
            System.exit(1);
        } else if (!wrapperFile.exists()) {
            System.err.println("\nThe serialised wrapper (" + wrapperFile + ") does not exist.\n" +
                    "Run CreateWrapper first to create it.");
            System.exit(1);
        } else {
            final ApiWrapper wrapper = ApiWrapper.fromFile(wrapperFile);

            String contentType = args.length == 3 ? args[2] : null;
            wrapper.setDefaultContentType(contentType);

            final Request resource = Request.to(args[0]).withContent(args[1], contentType);

            System.out.println("POST " + resource);
            try {
                HttpResponse resp = wrapper.post(resource);
                if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                    System.out.println("\n" + formatJSON(Http.getString(resp)));
                } else {
                    System.err.println("Invalid status received: " + resp.getStatusLine());
                }
            } finally {
                // serialise wrapper state again (token might have been refreshed)
                wrapper.toFile(wrapperFile);
            }
        }
    }


    static String formatJSON(String s) {
        try {
            return new JSONObject(s).toString(4);
        } catch (JSONException e) {
            try {
                return new JSONArray(s).toString(4);
            } catch (JSONException e2) {
                return s;
            }
        }
    }
}
