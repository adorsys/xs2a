package de.adorsys.psd2.validator.signature;

interface Verify {
    boolean verify(byte[] signingStringBytes);
}