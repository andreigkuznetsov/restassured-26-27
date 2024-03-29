package tests;

import data.TestData;
import helpers.WithUserDeleting;
import helpers.WithUserRegistration;
import models.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static data.ApiEndpoints.*;
import static helpers.WithUserRegistrationExtension.getGenerateTokenResponse;
import static helpers.WithUserRegistrationExtension.getRegistrationResponse;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.ApiMethodsSpecs.*;

@DisplayName("API тесты demoqa.com WebShop")
public class ApiTests extends TestBase {

        TestData testData = new TestData();

        @Test
        @WithUserRegistration
        @DisplayName("Проверка успешного удаления зарегистрированного пользователя")
        void successfulDeletingUserTest() {
                step("Отправляем запрос на удаление пользователя", () ->
                        given(requestGetDelete)
                                .header("Authorization", "Bearer " +
                                        getGenerateTokenResponse().getToken())
                                .when()
                                .delete(USER_ACCOUNT + getRegistrationResponse().getUserId())
                                .then()
                                .spec(response204));

                UserDataResponse getInvalidUserDataResponse = step("Отправляем запрос" +
                        "на получение данных о пользователе и добавленных ему книгах", () ->
                        given(requestGetDelete)
                                .header("Authorization", "Bearer " +
                                        getGenerateTokenResponse().getToken())
                                .when()
                                .get(USER_ACCOUNT + getRegistrationResponse().getUserId())
                                .then()
                                .spec(response401)
                                .extract().as(UserDataResponse.class));

                step("Проверяем, что значение Code совпадает с ожидаемыми", () -> {
                        assertThat(getInvalidUserDataResponse.getCode()).isEqualTo(testData.userNotFoundCode);
                });
                step("Проверяем, что значение Message - \"User not found!\"", () -> {
                        assertThat(getInvalidUserDataResponse.getMessage()).isEqualTo(testData.userNotFoundMessage);
                });
        }

        @Test
        @WithUserRegistration
        @WithUserDeleting
        @DisplayName("Проверка успешного удаления книги из профиля пользователя")
        void removeBookFromUserProfileTest() {
                AddListOfBooksRequest booksToAdd = new AddListOfBooksRequest();
                ListOfIsbns isbns = new ListOfIsbns();
                isbns.setIsbn(testData.isbn);
                booksToAdd.setCollectionOfIsbns(List.of(isbns));
                booksToAdd.setUserId(getRegistrationResponse().getUserId());

                AddListOfBooksResponse addListOfBooksResponse = step("Отправляем запрос " +
                        "на добавление пользователю случайной книги ", () ->
                     given(request)
                        .header("Authorization", "Bearer " + getGenerateTokenResponse().getToken())
                        .body(booksToAdd)
                        .when()
                        .post(BOOKS)
                        .then()
                        .spec(response201)
                        .extract().as(AddListOfBooksResponse.class));

                step("Проверяем, что ответ содержит isbn добавленной книги", () -> {
                assertThat(addListOfBooksResponse.getBooks())
                        .extracting(ListOfIsbns::getIsbn)
                        .containsExactly(testData.isbn);
                        });

                UserDataResponse getUserDataResponse = step("Отправляем запрос " +
                        "на получение данных о пользователе и добавленных ему книгах", () ->
                     given(request)
                        .header("Authorization", "Bearer " + getGenerateTokenResponse().getToken())
                        .when()
                        .get(USER_ACCOUNT + getRegistrationResponse().getUserId())
                        .then()
                        .spec(response200)
                        .extract().as(UserDataResponse.class));

                step("Проверяем, что значение userName совпадает с ожидаемыми", () -> {
                assertThat(getUserDataResponse.getUserName()).isEqualTo(getRegistrationResponse().getUserName());
                         });
                step("Проверяем, что значение isbn книги совпадает с ожидаемыми", () -> {
                assertThat(getUserDataResponse.getBooks()).extracting(BooksResponse::getIsbn)
                        .containsExactly(testData.isbn);
                         });

                ListOfIsbns booksToDelete = new ListOfIsbns();
                booksToDelete.setUserId(getRegistrationResponse().getUserId());
                booksToDelete.setIsbn(testData.isbn);

                step("Отправляем запрос на удаление книги из профиля пользователя", () ->
                given(request)
                        .header("Authorization", "Bearer " + getGenerateTokenResponse().getToken())
                        .body(booksToDelete)
                        .when()
                        .delete(BOOK)
                        .then()
                        .spec(response204));

                UserDataResponse getUserNewDataResponse = step("Отправляем запрос" +
                        "на получение данных о пользователе и добавленных ему книгах", () ->
                     given(request)
                        .header("Authorization", "Bearer " + getGenerateTokenResponse().getToken())
                        .when()
                        .get(USER_ACCOUNT + getRegistrationResponse().getUserId())
                        .then()
                        .spec(response200)
                        .extract().as(UserDataResponse.class));

                step("Проверяем, что значение userName совпадает с ожидаемыми", () -> {
                assertThat(getUserNewDataResponse.getUserName()).isEqualTo(getRegistrationResponse().getUserName());
                        });
                step("Проверяем, что у пользователя список книг пустой", () -> {
                assertThat(getUserNewDataResponse.getBooks()).isEmpty();
                         });
        }
}
