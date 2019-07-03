package io.defter.core.app.core;

import graphql.ErrorType;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GraphQLErrorAdapter implements GraphQLError {
    private final GraphQLError error;

    @Override
    public String getMessage() {
        return (error instanceof ExceptionWhileDataFetching)
                ? ((ExceptionWhileDataFetching) error).getException().getMessage()
                : error.getMessage();
    }

    @Override
    public ErrorType getErrorType() {
        return error.getErrorType();
    }

    @Override
    public List<Object> getPath() {
        return error.getPath();
    }

    @Override
    public Map<String, Object> getExtensions() {
        return null;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public Map<String, Object> toSpecification() {
        return error.toSpecification();
    }
}

