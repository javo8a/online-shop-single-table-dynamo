package com.onlineshop.api;

import com.onlineshop.api.dto.ProblemDetailsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.NoSuchElementException;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {


    Logger logger = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        ProblemDetailsDTO detailsDTO = new ProblemDetailsDTO();
        detailsDTO.detail(exception.getClass()
                        .getCanonicalName() + ">" + exception.getMessage())
                .status(500);

        logger.debug("Exception >{}< in code sending error response", exception.getMessage(), exception);

        if (exception instanceof NoSuchElementException) {
            detailsDTO.status(404);
            detailsDTO.detail("Not found");
        }

        if (exception instanceof IllegalArgumentException) {
            detailsDTO.status(400);
            detailsDTO.detail(exception.getMessage());
        }

        if (exception instanceof ConditionalCheckFailedException) {
            detailsDTO.status(409);
            detailsDTO.detail("Entry with same key already exists");
        }

        return Response.status(detailsDTO.getStatus())
                .entity(detailsDTO)
                .build();
    }

}
