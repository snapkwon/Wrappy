package net.wrappy.im.GethService.exception;

/**
 * Created by sonntht on 21/10/2017.
 */

/**
 * EthereumJava exception that acts as a RuntimeException
 */
public class GethLightServiceException extends RuntimeException {

    public GethLightServiceException(Exception e) {
        super(e.getMessage(), e.getCause());
    }

    public GethLightServiceException(String message) {
        super(message);
    }
}
