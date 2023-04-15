package com.joboffers.features;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.joboffers.BaseIntegrationTests;
import com.joboffers.SampleJobOffersResponse;
import com.joboffers.domain.loginandregister.dto.RegistrationResultDto;
import com.joboffers.domain.offer.dto.OfferResponseDto;
import com.joboffers.infrastructure.loginandregister.controller.dto.JwtResponseDto;
import com.joboffers.infrastructure.offer.scheduler.HttpScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class TypicalScenarioIntegrationTests extends BaseIntegrationTests implements SampleJobOffersResponse {
    @Autowired
    HttpScheduler httpScheduler;

    @Container
    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));

    @DynamicPropertySource
    public static void propertyOverride(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("offer.http.client.config.uri", () -> WIRE_MOCK_HOST);
        registry.add("offer.http.client.config.port", () -> wireMockServer.getPort());
    }

    @Test
    @DisplayName("user want to see offers but have to be logged in and external server should have some offers")
    public void typical_scenario() throws Exception {

        // step 1: zero offers in external HTTP server
        // given && when && then
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithZeroOffersJson())));


        // step 2: scheduler start first time and made  GET to external server and system added 0 offers to database
        // given && when
        List<OfferResponseDto> newOffers = httpScheduler.fetchAllOffersAndSaveAllIfNotExists();
        // then
        assertThat(newOffers).isEmpty();


        //step 3: user tried to get JWT token by requesting POST /token with username=someUser, password=somePassword and system returned UNAUTHORIZED(401)
        //given && when
        ResultActions failedLoginRequest = mockMvc.perform(post("/token")
                .content("""
                        {
                        "username": "User",
                        "password": "Password"
                        }
                        """.trim())
                .contentType(MediaType.APPLICATION_JSON_VALUE));
        //then
        failedLoginRequest
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(
                        """
                                {
                                "message": "Bad Credentials",
                                "status": "UNAUTHORIZED"
                                }
                                        """.trim()));


        //step 4: user made GET /offers with no jwt token and system returned UNAUTHORIZED(401)

        ResultActions failedGetOffersRequest = mockMvc.perform(get("/offers")
                .contentType(MediaType.APPLICATION_JSON_VALUE));
        //then
        failedGetOffersRequest.andExpect(status().isForbidden());

        //step 5: user made POST /register with username=User, password=Password and system registered user with status CREATED(200)

        ResultActions failedRegisterAction = mockMvc.perform(post("/register")
                .content(
                        """
                                {"username": "User",
                                "password": "Password"
                                }
                                """
                )
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        MvcResult registerActionResult = failedRegisterAction.andExpect(status().isCreated()).andReturn();
        String registerActionResultJson = registerActionResult.getResponse().getContentAsString();
        RegistrationResultDto registrationResultDto = objectMapper.readValue(registerActionResultJson, RegistrationResultDto.class);
        assertAll(
                () -> assertThat(registrationResultDto.username()).isEqualTo("User"),
                () -> assertThat(registrationResultDto.created()).isTrue(),
                () -> assertThat(registrationResultDto.id()).isNotNull()
        );

        //step 6: user tried to get JWT token by requesting POST /token with username=someUser, password=somePassword and system returned OK(200) and jwttoken=AAAA.BBBB.CCC
        ResultActions successLoginRequest = mockMvc.perform(post("/token")
                .content(
                        """
                                {"username": "User",
                                "password": "Password"
                                }
                                """
                )
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        MvcResult mvcResult = successLoginRequest.andExpect(status().isOk()).andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        JwtResponseDto jwtResponse = objectMapper.readValue(json, JwtResponseDto.class);
        String token = jwtResponse.token();
        assertAll(
                () -> assertThat(jwtResponse.username()).isEqualTo("User"),
                () -> assertThat(token).matches(Pattern.compile("^([A-Za-z0-9-_=]+\\.)+([A-Za-z0-9-_=])+\\.?$"))
        );



        //step 7: user made GET /offers with header and system returned OK(200) with 0 offers
        // given
        String offersUrl = "/offers";
        // when
        ResultActions perform = mockMvc.perform(get(offersUrl)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        MvcResult mvcResult2 = perform.andExpect(status().isOk()).andReturn();
        String jsonWithOffers = mvcResult2.getResponse().getContentAsString();
        List<OfferResponseDto> offers = objectMapper.readValue(jsonWithOffers, new TypeReference<>() {
        });
        assertThat(offers).isEmpty();


        //step 8: two new offers in external HTTP server

        //given && when && then
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithTwoOffersJson())));


        //step 9: scheduler start second  time and made GET to external server and system added 2 new offers with ids: 1000 and 2000 to database
        // given && when
        List<OfferResponseDto> twoNewOffers = httpScheduler.fetchAllOffersAndSaveAllIfNotExists();
        // then
        assertThat(twoNewOffers).hasSize(2);

        //step 10: user made GET /offers  and system returned OK(200) with 2 offers

        // given&& when
        ResultActions performGetForTwoOffers = mockMvc.perform(get(offersUrl)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then

        MvcResult performGetForTwoOffersMvcResult = performGetForTwoOffers.andExpect(status().isOk()).andReturn();
        String jsonWithTwoOffers = performGetForTwoOffersMvcResult.getResponse().getContentAsString();
        List<OfferResponseDto> twoOffers = objectMapper.readValue(jsonWithTwoOffers, new TypeReference<>() {
        });
        assertThat(twoOffers).hasSize(2);
        OfferResponseDto expectedFirstOffer = twoNewOffers.get(0);
        OfferResponseDto expectedSecondOffer = twoNewOffers.get(1);
        assertThat(twoOffers).containsExactlyInAnyOrder(
                new OfferResponseDto(expectedFirstOffer.id(), expectedFirstOffer.companyName(), expectedFirstOffer.position(), expectedFirstOffer.salary(), expectedFirstOffer.offerUrl()),
                new OfferResponseDto(expectedSecondOffer.id(), expectedSecondOffer.companyName(), expectedSecondOffer.position(), expectedSecondOffer.salary(), expectedSecondOffer.offerUrl())
        );


        //step 11: user made GET /offers/1000 and system returned NOT_FOUND(404) with message “Offer with id 1000 not found”

        // given
        // when
        ResultActions performGetOffersNotExisitingId = mockMvc.perform(get("/offers/1000"));
        // then
        performGetOffersNotExisitingId.andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {
                        "message":  "Offer with id 1000 not found",
                        "status": "NOT_FOUND"
                        }
                        """.trim()));

        //step 12: user made GET /offers/100 and system returned OK(200) with offer
        // given
        String offerIdAddedToDatabase = expectedFirstOffer.id();
        // when
        ResultActions getOfferById = mockMvc.perform(get("/offers/" + offerIdAddedToDatabase)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        String singleOfferByOfferUrlJson = getOfferById.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        OfferResponseDto singleOfferByOfferUrl = objectMapper.readValue(singleOfferByOfferUrlJson, OfferResponseDto.class);
        assertThat(singleOfferByOfferUrl).isEqualTo(expectedFirstOffer);

        //step 13: two new offers in external HTTP server

        // given && when && then
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithFourOffersJson())));

        //step 14: scheduler start third time and made GET to external server and system added 5 new offers to database

        List<OfferResponseDto> nextTwoNewOffers = httpScheduler.fetchAllOffersAndSaveAllIfNotExists();
        // then
        assertThat(nextTwoNewOffers).hasSize(2);


        //step 15: user made GET /offers  and system returned OK(200) with 4 offers
        ResultActions performGetForFourOffers = mockMvc.perform(get(offersUrl)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        MvcResult performGetForFourOffersMvcResult = performGetForFourOffers.andExpect(status().isOk()).andReturn();
        String jsonWithFourOffers = performGetForFourOffersMvcResult.getResponse().getContentAsString();
        List<OfferResponseDto> fourOffers = objectMapper.readValue(jsonWithFourOffers, new TypeReference<>() {
        });
        assertThat(fourOffers).hasSize(4);
        OfferResponseDto expectedThirdOffer = fourOffers.get(0);
        OfferResponseDto expectedFourthOffer = fourOffers.get(1);
        assertThat(fourOffers).contains(
                new OfferResponseDto(expectedThirdOffer.id(), expectedThirdOffer.companyName(), expectedThirdOffer.position(), expectedThirdOffer.salary(), expectedThirdOffer.offerUrl()),
                new OfferResponseDto(expectedFourthOffer.id(), expectedFourthOffer.companyName(), expectedFourthOffer.position(), expectedFourthOffer.salary(), expectedFourthOffer.offerUrl()
                ));

        //step 16: user made POST /offers and offer as body and system returned CREATED(201) with saved offer
        // given
        // when
        ResultActions performPostOffersWithOneOffer = mockMvc.perform(post("/offers")
                .content("""
                        {
                        "companyName": "BWR",
                        "position": "junior",
                        "salary": "5000 - 6000 PLN",
                        "offerUrl": "https://jobOffers.pl/offer/111"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
        );
        // then
        String createdOfferJson = performPostOffersWithOneOffer.andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OfferResponseDto parsedCreatedOfferJson = objectMapper.readValue(createdOfferJson, OfferResponseDto.class);
        String id = parsedCreatedOfferJson.id();
        assertAll(
                () -> assertThat(parsedCreatedOfferJson.companyName()).isEqualTo("BWR"),
                () -> assertThat(parsedCreatedOfferJson.position()).isEqualTo("junior"),
                () -> assertThat(parsedCreatedOfferJson.salary()).isEqualTo("5000 - 6000 PLN"),
                () -> assertThat(parsedCreatedOfferJson.offerUrl()).isEqualTo("https://jobOffers.pl/offer/111"),
                () -> assertThat(id).isNotNull()
        );


        //step 17: user made GET /offers and system returned OK(200) with 1 offer

        // given & when
        ResultActions performGetOffers = mockMvc.perform(get("/offers")
                .contentType(MediaType.APPLICATION_JSON_VALUE));
        // then
        String oneOfferJson = performGetOffers.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<OfferResponseDto> parsedJsonWithOneOffer = objectMapper.readValue(oneOfferJson, new TypeReference<>() {
        });
        assertThat(parsedJsonWithOneOffer).hasSize(5);
        assertThat(parsedJsonWithOneOffer.stream().map(OfferResponseDto::id)).contains(id);
    }
}
