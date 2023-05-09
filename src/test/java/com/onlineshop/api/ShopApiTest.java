package com.onlineshop.api;

import static io.restassured.RestAssured.given;

import com.onlineshop.infrastructure.ShopRepositoryImpl;
import com.onlineshop.infrastructure.dynamo.DynamoDBHandler;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTest
public class ShopApiTest {

  @InjectMock
  ShopRepositoryImpl shopRepoMock;


  @InjectMock
  DynamoDBHandler dynamoDBHandler;

  @BeforeEach
  public void setup() {

//    Order order = Order.builder()
//            .customerId(UUID.randomUUID())
//            .items()
//                                         .fleetId("A330")
//                                         .revisionNumber("22.22")
//                                         .description("Sample revision")
//                                         .createdBy("JohnSmith")
//                                         .build();
//                Mockito.when(melRepoMock.searchMelRevisions(anyString()))
//                                .thenReturn(Arrays.asList(melRevision));
  }

  //  @Test
  public void whenGetRevisions_thenRevisionsShouldReturn() {

    given().contentType(ContentType.JSON)
           .queryParam("fleetId", "A330")
           .when()
           .get("/mel-revisions")
           .then()
           .statusCode(200)
           .body("size()", Matchers.is(1));
  }
}
