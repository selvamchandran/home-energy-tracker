package com.selvam.userservice.integration;

import com.selvam.userservice.dto.ApiResponseDto;
import com.selvam.userservice.dto.request.UserRequest;
import com.selvam.userservice.dto.response.UserResponse;
import com.selvam.userservice.entity.User;
import com.selvam.userservice.repository.UserRepository;
import com.selvam.userservice.testsupport.MySqlTestContainersBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
public class UserServiceIntegrationTest extends MySqlTestContainersBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser_viaRestApi_persistsAndReturnsUser() {
        UserRequest request = UserRequest.builder()
                .name("John John")
                .surname("Doe")
                .email("demouser@gmail.com")
                .address("123 Coding St")
                .alerting(true)
                .energyAlertingThreshold(2000.0)
                .build();


        ResponseEntity<ApiResponseDto<UserResponse>> response = restTemplate.exchange(
                "/api/v1/user",
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<ApiResponseDto<UserResponse>>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        UserResponse created = response.getBody().getData(); // adjust getter if your wrapper uses another field name
        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        assertThat(created.email()).isEqualTo("demouser@gmail.com");
        assertThat(created.name()).isEqualTo("John John");
        assertThat(created.surname()).isEqualTo("Doe");
        assertThat(created.address()).isEqualTo("123 Coding St");
        assertThat(created.alerting()).isTrue();
        assertThat(created.energyAlertingThreshold()).isEqualTo(2000.0);

        // Verify database persistence
        assertThat(userRepository.findById(created.id())).isPresent();

        // Verify GET endpoint
        ResponseEntity<ApiResponseDto<UserResponse>> loaded = restTemplate.exchange(
                "/api/v1/user/" + created.id(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponseDto<UserResponse>>() {
                }
        );

        assertThat(loaded.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loaded.getBody()).isNotNull();
        assertThat(loaded.getBody().getData().email()).isEqualTo("demouser@gmail.com");
    }

    @Test
    void saveUser_viaRepository_roundTripsThroughMysql() {
        User saved = userRepository.save(User.builder()
                .name("Grace")
                .surname("Hopper")
                .email("grace.it@example.com")
                .address("2 Compiler Way")
                .alerting(false)
                .energyAlertingThreshold(900.0)
                .build());

        assertThat(saved.getId()).isNotNull();

        User fromDb = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(fromDb.getEmail()).isEqualTo("grace.it@example.com");
        assertThat(fromDb.getName()).isEqualTo("Grace");
        assertThat(fromDb.isAlerting()).isFalse();
        assertThat(fromDb.getEnergyAlertingThreshold()).isEqualTo(900.0);
    }


    @Test
    void updateUser_viaRestApi_persistsChanges() {
        UserRequest createRequest = UserRequest.builder()
                .name("Alan")
                .surname("Turing")
                .email("alan.update.it@example.com")
                .address("10 Bletchley Park")
                .alerting(true)
                .energyAlertingThreshold(500.0)
                .build();

        ResponseEntity<ApiResponseDto<UserResponse>> createdUser = restTemplate.exchange(
                "/api/v1/user",
                HttpMethod.POST,
                new HttpEntity<>(createRequest),
                new ParameterizedTypeReference<ApiResponseDto<UserResponse>>() {
                }
        );

        assertThat(createdUser.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createdUser.getBody()).isNotNull();

        UserResponse created = createdUser.getBody().getData();
        Long id = created.id();

        UserRequest updateRequest = UserRequest.builder()
//                .id(id)
                .name("Alan Mathison")
                .surname("Turing")
                .email("alan.update.it@example.com")
                .address("12 Wilmslow Rd")
                .alerting(false)
                .energyAlertingThreshold(750.0)
                .build();

        ResponseEntity<ApiResponseDto<UserResponse>> updateResponse = restTemplate.exchange(
                "/api/v1/user/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                new ParameterizedTypeReference<ApiResponseDto<UserResponse>>() {
                }
        );
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody() != null ? updateResponse.getBody().getMessage() : "User Update Failed").isEqualTo("User updated successfully");


        // Verify GET endpoint
        ResponseEntity<ApiResponseDto<UserResponse>> loaded = restTemplate.exchange(
                "/api/v1/user/" + created.id(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponseDto<UserResponse>>() {
                }
        );

        assertThat(loaded.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loaded.getBody()).isNotNull();
        assertThat(loaded.getBody().getData().email()).isEqualTo("alan.update.it@example.com");
        assertThat(loaded.getBody().getData().name()).isEqualTo("Alan Mathison");
        assertThat(loaded.getBody().getData().address()).isEqualTo("12 Wilmslow Rd");
        assertThat(loaded.getBody().getData().alerting()).isFalse();
        assertThat(loaded.getBody().getData().energyAlertingThreshold()).isEqualTo(750.0);

    }

    @Test
    void deleteUser_viaRestApi_removesUser() {
        UserRequest createRequest = UserRequest.builder()
                .name("Edsger")
                .surname("Dijkstra")
                .email("edsger.delete.it@example.com")
                .address("3 Structured Programming Ln")
                .alerting(false)
                .energyAlertingThreshold(300.0)
                .build();

        ResponseEntity<ApiResponseDto<UserResponse>> response = restTemplate.exchange(
                "/api/v1/user",
                HttpMethod.POST,
                new HttpEntity<>(createRequest),
                new ParameterizedTypeReference<ApiResponseDto<UserResponse>>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        UserResponse created = response.getBody().getData(); // adjust getter if your wrapper uses another field name
        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        Long id = created.id();


        ResponseEntity<ApiResponseDto<Void>> deleteResponse = restTemplate.exchange(
                "/api/v1/user/" + id,
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<ApiResponseDto<Void>>() {
                });

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify GET endpoint
        ResponseEntity<ApiResponseDto<UserResponse>> afterDelete = restTemplate.exchange(
                "/api/v1/user/" + id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponseDto<UserResponse>>() {
                }
        );
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);


    }

}
