package com.onlineshop.infrastructure.dynamodblocal;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates an instance of LocalDynamoDb that can be used for unit tests
 */
public class DynamoDbResource implements QuarkusTestResourceLifecycleManager {

    Logger logger = LoggerFactory
            .getLogger(DynamoDbResource.class);

    private LocalDynamoDb localDynamoDb;
    protected DynamoDbClient localDynamoDbClient;

    /**
     * Finds a port available to use locally
     *
     * @return the port as integer, or Integer.MIN_VALUE in case of failure
     */
    private int getFreePort() {
        int port = Integer.MIN_VALUE;
        try {
            ServerSocket socket = new ServerSocket(0);
            port = socket
                    .getLocalPort();
            socket
                    .close();
            return port;
        } catch (IOException ioe) {
            logger
                    .error("Error getting port: {}", ioe
                            .getMessage(), ioe);
        }
        return port;
    }

    /**
     * Starts in memory localDynamoDb
     *
     * @return returns quarkus dynamodb properties needed to connect to local
     * instance
     */
    @Override
    public Map<String, String> start() {
        localDynamoDb = new LocalDynamoDb();
        int port = getFreePort();
        localDynamoDb
                .start(port);

        String endpointOverride = String
                .format("http://localhost:%d", port);
        String region = "us-east-1";
        String accessKey = "accessKey";
        String secretKey = "secretKey";
        localDynamoDbClient = localDynamoDb
                .createClient(endpointOverride, region, accessKey, secretKey);

        Map<String, String> properties = new HashMap<>();
        properties
                .put("quarkus.dynamodb.endpoint-override", endpointOverride);
        properties
                .put("quarkus.dynamodb.aws.region", region);
        properties
                .put("quarkus.dynamodb.aws.credentials.type", "static");
        properties
                .put("quarkus.dynamodb.aws.credentials.static-provider.access-key-id", accessKey);
        properties
                .put("quarkus.dynamodb.aws.credentials.static-provider.secret-access-key", secretKey);

        return properties;
    }

    @Override
    public void stop() {
        if (localDynamoDb != null) {
            localDynamoDb
                    .stop();
        }
    }

}
