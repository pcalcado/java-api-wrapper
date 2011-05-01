package com.soundcloud.api.examples;

import static com.soundcloud.api.examples.CreateWrapper.WRAPPER_SER;

import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Endpoints;
import com.soundcloud.api.Http;
import com.soundcloud.api.Params;
import com.soundcloud.api.Request;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;

/**
 * Uploads a file to SoundCloud.
 */
public final class UploadFile {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println(UploadFile.class.getSimpleName() + " file");
            System.exit(1);
        } else if (!WRAPPER_SER.exists()) {
            System.err.println("\nThe serialised wrapper (" + WRAPPER_SER + ") does not exist.\n" +
                    "Run CreateWrapper first to create it.");
            System.exit(1);
        } else {
            final File file = new File(args[0]);
            if (!file.exists()) throw new IOException("The file `"+file+"` does not exist");

            final ApiWrapper wrapper = ApiWrapper.fromFile(WRAPPER_SER);
            System.out.println("Uploading " + file);
            try {
                HttpResponse resp = wrapper.post(Request.to(Endpoints.TRACKS)
                        .add(Params.Track.TITLE, file.getName())
                        .withFile(Params.Track.ASSET_DATA, file));

                if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                    System.out.println("201 Created");
                    Header ct = resp.getFirstHeader("Content-Type");
                    if (ct != null && ct.getValue().contains("application/json")) {
                        System.out.println("\n" + Http.getJSON(resp).toString(4));
                    } else {
                        System.out.println("\n" + Http.getString(resp));
                    }
                } else {
                    System.err.println("Invalid status received: " + resp.getStatusLine());
                }
            } finally {
                // serialise wrapper state again (token might have changed)
                wrapper.toFile(WRAPPER_SER);
            }
        }
    }
}
