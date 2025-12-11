package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import models.User;

public class TestDataProvider {

  public static User getRealUser() {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      File file = new File("src/test/resources/config/real-user.json");
      return objectMapper.readValue(file, User.class);
    } catch (IOException e) {
      throw new RuntimeException("Error leyendo el archivo real-user.json", e);
    }
  }
}
