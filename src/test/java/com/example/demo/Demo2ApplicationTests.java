package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class Demo2ApplicationTests {
  @MockitoBean
  private SentryConf sentryConf;

  @Test
  void contextLoads() {}
}
