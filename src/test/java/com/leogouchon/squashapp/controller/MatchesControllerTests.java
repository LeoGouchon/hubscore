package com.leogouchon.squashapp.controller;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MatchController.class)
@ExtendWith(SpringExtension.class)
public class MatchesControllerTests {
    @Autowired
    private MockMvc mockMvc;

}
