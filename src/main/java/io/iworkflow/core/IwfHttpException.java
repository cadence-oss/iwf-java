package io.iworkflow.core;

import feign.FeignException;
import io.iworkflow.gen.models.EncodedObject;
import io.iworkflow.gen.models.ErrorResponse;
import io.iworkflow.gen.models.ErrorSubStatus;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public abstract class IwfHttpException extends RuntimeException {

    private final int statusCode;
    private ErrorResponse errorResponse;

    public IwfHttpException(final ObjectEncoder objectEncoder, final FeignException.FeignClientException exception) {
        super(exception);
        statusCode = exception.status();
        String decodeErrorMessage = "";
        final Optional<ByteBuffer> respBody = exception.responseBody();
        if (respBody.isPresent()) {
            String data = StandardCharsets.UTF_8.decode(respBody.get()).toString();
            try {
                errorResponse = objectEncoder.decode(new EncodedObject().data(data), ErrorResponse.class);
                return;
            } catch (Exception e) {
                decodeErrorMessage = e.getMessage();
            }
        }
        errorResponse = new ErrorResponse()
                .detail("empty or unable to decode to ErrorResponse:" + decodeErrorMessage)
                .subStatus(ErrorSubStatus.UNCATEGORIZED_SUB_STATUS);
    }

    public IwfHttpException() {
        statusCode = 500;
    }

    public String getErrorDetails() {
        return errorResponse.getDetail();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ErrorSubStatus getErrorSubStatus() {
        return errorResponse.getSubStatus();
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public static IwfHttpException fromFeignException(final ObjectEncoder objectEncoder, final FeignException.FeignClientException exception) {
        if (exception.status() >= 400 && exception.status() < 500) {
            return new ClientSideException(objectEncoder, exception);
        } else {
            return new ServerSideException(objectEncoder, exception);
        }
    }
}
