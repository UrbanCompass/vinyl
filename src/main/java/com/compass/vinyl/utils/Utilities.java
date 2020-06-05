// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utilities {

    private static final Logger LOG = LoggerFactory.getLogger(Utilities.class);

    public static String md5(String message) {
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("UTF-8"));

            StringBuilder sb = new StringBuilder(2*hash.length);
            for (byte b: hash)
                sb.append(String.format("%02x", b&0xff));
            digest = sb.toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            LOG.error("Exception occurred while hashing.", ex);
        }
        return digest;
    }
}

