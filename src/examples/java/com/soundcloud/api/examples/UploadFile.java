package com.soundcloud.api.examples;

import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Endpoints;
import com.soundcloud.api.Http;
import com.soundcloud.api.Params;
import com.soundcloud.api.Request;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;

/**
 * Uploads a file to SoundCloud.
 */
public final class UploadFile {

    public static void main(String[] args) throws Exception {
        final File wrapperFile = CreateWrapper.WRAPPER_SER;

        if (args.length == 0) {
            System.err.println(UploadFile.class.getSimpleName() + " file");
            System.exit(1);
        } else if (!wrapperFile.exists()) {
            System.err.println("\nThe serialised wrapper (" + wrapperFile + ") does not exist.\n" +
                    "Run CreateWrapper first to create it.");
            System.exit(1);
        } else {
            final File file = new File(args[0]);
            if (!file.exists()) throw new IOException("The file `"+file+"` does not exist");

            final ApiWrapper wrapper = ApiWrapper.fromFile(wrapperFile);
            System.out.println("Uploading " + file);
            try {
                HttpResponse resp = wrapper.post(Request.to(Endpoints.TRACKS)
                        .add(Params.Track.TITLE,     file.getName())
                        .add(Params.Track.TAG_LIST, "demo upload")
                        .withFile(Params.Track.ASSET_DATA, file)
                        // you can add more parameters here, e.g.
                        // .withFile(Params.Track.ARTWORK_DATA, file)) /* to add artwork */

                        // set a progress listener (optional)
                        .setProgressListener(new Request.TransferProgressListener() {
                            @Override public void transferred(long amount) {
                                System.err.print(".");
                            }
                        }));

                if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                    System.out.println("\n201 Created "+resp.getFirstHeader("Location").getValue());

                    // dump the representation of the new track
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
