package com.joboffers.controller.error;

import com.joboffers.BaseIntegrationTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OfferDuplicateErrorIntegrationTest extends BaseIntegrationTests {


    @Test
    @DisplayName("should return 409 conflict when added second offer with same offer url")
    public void should_return_409() throws Exception {
        // step 1
        // given && when
        ResultActions perform = mockMvc.perform(post("/offers")
                .content("""
                        {
                        "companyName": "Google",
                        "position": "Mid",
                        "salary": "9 000 - 16 000 PLN",
                        "offerUrl": "https://newoffers.pl/offer/1234"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
        );
        // then
        perform.andExpect(status().isCreated());


        // step 2
        // given && when
        ResultActions perform1 = mockMvc.perform(post("/offers")
                .content("""
                         {
                        "companyName": "Google",
                        "position": "Mid",
                        "salary": "9 000 - 16 000 PLN",
                        "offerUrl": "https://newoffers.pl/offer/1234"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
        );
        // then
        perform1.andExpect(status().isConflict());
    }
}
