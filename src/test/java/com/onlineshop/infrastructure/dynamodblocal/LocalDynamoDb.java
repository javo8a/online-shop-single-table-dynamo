package com.onlineshop.infrastructure.dynamodblocal;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.util.Optional;

/**
 * Wrapper for a local DynamoDb server used in testing. Each instance of this
 * class will find a new port to run on, so multiple instances can be safely run
 * simultaneously. Each instance of this service uses memory as a storage medium
 * and is thus completely ephemeral; no data will be persisted between stops and
 * starts.
 * <p>
 * LocalDynamoDb localDynamoDb = new LocalDynamoDb(); localDynamoDb.start(); //
 * Start the service running locally on host DynamoDbClient dynamoDbClient =
 * localDynamoDb.createClient(); ... // Do your testing with the client
 * localDynamoDb.stop(); // Stop the service and free up resources
 * <p>
 * If possible it's recommended to keep a single running instance for all your
 * tests, as it can be slow to teardown and create new servers for every test,
 * but there have been observed problems when dropping tables between tests for
 * this scenario, so it's best to write your tests to be resilient to tables
 * that already have data in them.
 *
 * @see "https://github.com/aws/aws-sdk-java-v2/blob/master/services-custom/dynamodb-enhanced/src/test/java/software/amazon/awssdk/enhanced/dynamodb/functionaltests/LocalDynamoDb.java"
 */
class LocalDynamoDb {

    private DynamoDBProxyServer server;

    /**
     * Start the local DynamoDb service and run in background
     */
    void start(int port) {
        String portString = Integer
                .toString(port);

        try {
            server = createServer(portString);
            server
                    .start();
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    /**
     * Create a standard AWS v2 SDK client pointing to the local DynamoDb instance
     *
     * @return A DynamoDbClient pointing to the local DynamoDb instance
     */
    DynamoDbClient createClient(String endpoint, String region, String accessKeyId, String secretAccessKey) {
        System
                .setProperty("sqlite4java.library.path", "native-libs");

        return DynamoDbClient
                .builder()
                .endpointOverride(URI
                        .create(endpoint))
                // The region is meaningless for local DynamoDb but required for client builder
                // validation
                .region(Region
                        .of(region))
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials
                                .create(accessKeyId, secretAccessKey)))
                .overrideConfiguration(o -> o
                        .addExecutionInterceptor(new VerifyUserAgentInterceptor()))
                .build();
    }

    /**
     * Stops the local DynamoDb service and frees up resources it is using.
     */
    void stop() {
        try {
            server
                    .stop();
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    private DynamoDBProxyServer createServer(String portString) throws Exception {
        return ServerRunner
                .createServerFromCommandLineArgs(new String[]{"-inMemory", "-port", portString});
    }

    private static RuntimeException propagate(Exception e) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        throw new RuntimeException(e);
    }

    private static class VerifyUserAgentInterceptor implements ExecutionInterceptor {

        @Override
        public void beforeTransmission(Context.BeforeTransmission context, ExecutionAttributes executionAttributes) {
            Optional<String> headers = context
                    .httpRequest()
                    .firstMatchingHeader("User-agent");
            assert (headers)
                    .isPresent();
            assert (headers
                    .get())
                    .contains("hll/ddb-enh");
        }
    }

}
