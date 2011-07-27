package com.soundcloud.api.examples;

import com.soundcloud.api.ApiWrapper;

import java.io.File;

public final class DumpToken {

    public static void main(String[] args) throws Exception {
        final File wrapperFile = CreateWrapper.WRAPPER_SER;
        if (!wrapperFile.exists()) {
            System.err.println("\nThe serialised wrapper (" + wrapperFile + ") does not exist.\n" +
                    "Run CreateWrapper first to create it.");
            System.exit(1);
        } else {
            System.err.println(ApiWrapper.fromFile(wrapperFile).getToken());
        }
    }
}
