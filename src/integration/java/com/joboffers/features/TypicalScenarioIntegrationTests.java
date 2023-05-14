package com.joboffers.features;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.joboffers.BaseIntegrationTests;
import com.joboffers.TemplateJobOffersResponse;
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


public class TypicalScenarioIntegrationTests extends BaseIntegrationTests implements TemplateJobOffersResponse {
    @Autowired
    HttpScheduler httpScheduler;

    @Container
    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:5.0.17"));


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
        List<OfferResponseDto> offers = httpScheduler.fetchAllOffersAndSaveAllIfNotExists();
        // then
        assertThat(offers).isEmpty();


        //step 3: user tried to get JWT token by requesting POST /token with username=User, password=Password and system returned UNAUTHORIZED(401)
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

        ResultActions notAuthorizedRequest = mockMvc.perform(get("/offers")
                .contentType(MediaType.APPLICATION_JSON_VALUE));
        //then
        notAuthorizedRequest.andExpect(status().isForbidden());

        //step 5: user made POST /register with username=User, password=Password and system registered user with status CREATED(200)

        ResultActions failedRegister = mockMvc.perform(post("/register")
                .content(
                        """
                                {
                                "username": "User",
                                 "password": "Password"
                                }
                                """
                )
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        MvcResult registerResult = failedRegister.andExpect(status().isCreated()).andReturn();
        String registerResultJson = registerResult.getResponse().getContentAsString();
        RegistrationResultDto registrationDto = objectMapper.readValue(registerResultJson, RegistrationResultDto.class);
        assertAll(
                () -> assertThat(registrationDto.username()).isEqualTo("User"),
                () -> assertThat(registrationDto.created()).isTrue(),
                () -> assertThat(registrationDto.id()).isNotNull()
        );

        //step 6: user tried to get JWT token by requesting POST /token with username=User, password=Password and system returned OK(200)
        ResultActions successLoginRequest = mockMvc.perform(post("/token")
                .content(
                        """
                                 {
                                "username": "User",
                                 "password": "Password"
                                }
                                """
                )
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        MvcResult loginMvcResult = successLoginRequest.andExpect(status().isOk()).andReturn();
        String json = loginMvcResult.getResponse().getContentAsString();
        JwtResponseDto jwtResponse = objectMapper.readValue(json, JwtResponseDto.class);
        String token = jwtResponse.token();
        assertAll(
                () -> assertThat(jwtResponse.username()).isEqualTo("User"),
                () -> assertThat(token).matches(Pattern.compile("^([A-Za-z0-9-_=]+\\.)+([A-Za-z0-9-_=])+\\.?$"))
        );


        //step 7: user made GET /offers with authorization and system returned OK(200) with 0 offers
        // given && when
        String offersUrl = "/offers";
        ResultActions perform = mockMvc.perform(get(offersUrl)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        MvcResult mvcResult = perform.andExpect(status().isOk()).andReturn();
        String jsonWithOffers = mvcResult.getResponse().getContentAsString();
        List<OfferResponseDto> listOffers = objectMapper.readValue(jsonWithOffers, new TypeReference<>() {
        });
        assertThat(listOffers).isEmpty();

        //step 8 one new offer in external HTTP server

        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithOneOfferJson())));
        //step 9 scheduler start second  time and made GET with authorization  to external server and system added 1 new offer
        // given && when
        List<OfferResponseDto> newOffer = httpScheduler.fetchAllOffersAndSaveAllIfNotExists();

        assertThat(newOffer).hasSize(1);

        //step 10: user made GET /offers with authorization and system returned OK(200) with 1 offer

        ResultActions performGetForOneOffer = mockMvc.perform(get(offersUrl)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE));
        // then

        MvcResult performGetForOneOfferMvc = performGetForOneOffer.andExpect(status().isOk()).andReturn();
        String jsonWithOneOffer = performGetForOneOfferMvc.getResponse().getContentAsString();
        List<OfferResponseDto> oneOffer = objectMapper.readValue(jsonWithOneOffer, new TypeReference<>() {
        });
        assertThat(oneOffer).hasSize(1);
        OfferResponseDto expectedOneOffer = oneOffer.get(0);

        assertThat(oneOffer).containsExactlyInAnyOrder(
                new OfferResponseDto(expectedOneOffer.id(), expectedOneOffer.companyName()
                        , expectedOneOffer.position(), expectedOneOffer.salary(), expectedOneOffer.offerUrl()));

        //step 11: two new offers in external HTTP server
        //given && when && then
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithTwoOffersJson())));


        //step 12: scheduler start third  time and made GET with authorization  to external server and system added 2 new offers
        // given && when
        List<OfferResponseDto> twoNewOffers = httpScheduler.fetchAllOffersAndSaveAllIfNotExists();

        // then
        assertThat(twoNewOffers).hasSize(2);

        //step 13: user made GET /offers with authorization and system returned OK(200) with 2 offers
        // given&& when
        ResultActions performGetForTwoOffers = mockMvc.perform(get(offersUrl)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then

        MvcResult performGetForTwoOffersMvcResult = performGetForTwoOffers.andExpect(status().isOk()).andReturn();
        String jsonWithTwoOffers = performGetForTwoOffersMvcResult.getResponse().getContentAsString();
        List<OfferResponseDto> twoOffers = objectMapper.readValue(jsonWithTwoOffers, new TypeReference<>() {
        });
        assertThat(twoOffers).hasSize(3);
        OfferResponseDto expectedFirstOffer = twoNewOffers.get(0);
        OfferResponseDto expectedSecondOffer = twoNewOffers.get(1);
        assertThat(twoOffers).contains(
                new OfferResponseDto(expectedFirstOffer.id(), expectedFirstOffer.companyName(), expectedFirstOffer.position(), expectedFirstOffer.salary(), expectedFirstOffer.offerUrl()),
                new OfferResponseDto(expectedSecondOffer.id(), expectedSecondOffer.companyName(), expectedSecondOffer.position(), expectedSecondOffer.salary(), expectedSecondOffer.offerUrl())
        );


        //step 14: user made GET /offers/1000 with authorization  and system returned NOT_FOUND(404) with message “Offer with id 1000 not found”

        // given && when
        ResultActions performGetOffersNotExisitingId = mockMvc.perform(get("/offers/1000")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE));
        // then
        performGetOffersNotExisitingId.andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {
                        "message":  "Offer with id 1000 not found",
                        "status": "NOT_FOUND"
                        }
                        """.trim()));

        //step 15: user made GET /offers/1000 with authorization  and system returned OK(200) with offer
        // given
        String offerIdAddedToDatabase = expectedFirstOffer.id();
        // when
        ResultActions getOfferById = mockMvc.perform(get("/offers/" + offerIdAddedToDatabase)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        String singleOfferByOfferUrlJson = getOfferById.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        OfferResponseDto singleOfferByOfferUrl = objectMapper.readValue(singleOfferByOfferUrlJson, OfferResponseDto.class);
        assertThat(singleOfferByOfferUrl).isEqualTo(expectedFirstOffer);

        //step 16: two new offers in external HTTP server

        // given && when && then
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithFourOffersJson())));

        //step 17: scheduler start  fourth time and made GET to external server and system added 1 new offer to database

        List<OfferResponseDto> nextOneOffer = httpScheduler.fetchAllOffersAndSaveAllIfNotExists();
        // then
        assertThat(nextOneOffer).hasSize(1);


        //step 18: user made GET /offers with authorization and system returned OK(200) with 4 offers
        ResultActions performGetForFourOffers = mockMvc.perform(get(offersUrl)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        MvcResult performGetForFourOffersMvcResult = performGetForFourOffers.andExpect(status().isOk()).andReturn();
        String jsonWithFourOffers = performGetForFourOffersMvcResult.getResponse().getContentAsString();
        List<OfferResponseDto> fourOffers = objectMapper.readValue(jsonWithFourOffers, new TypeReference<>() {
        });
        assertThat(fourOffers).hasSize(4);

        OfferResponseDto expectedFourthOffer = fourOffers.get(1);
        assertThat(fourOffers).contains(
                new OfferResponseDto(expectedFourthOffer.id(), expectedFourthOffer.companyName(), expectedFourthOffer.position(), expectedFourthOffer.salary(), expectedFourthOffer.offerUrl()
                ));

        //step 19: user made POST /offers with authorization  and offer as body and system returned CREATED(201) with saved offer
        // given && when

        ResultActions performPostOffersWithOneOffer = mockMvc.perform(post("/offers")
                .header("Authorization", "Bearer " + token)
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


        //step 20: user made GET /offers with authorization  and system returned OK(200) with 1 offer

        // given & when
        ResultActions performGetOffers = mockMvc.perform(get("/offers")
                .header("Authorization", "Bearer " + token)
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
