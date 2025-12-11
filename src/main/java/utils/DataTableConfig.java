package utils;

import models.User;

import java.util.Map;

public class DataTableConfig {

    //   @DataTableType
    public User userEntry(Map<String, String> entry) {
        User user = new User();

        user.setEmail(entry.getOrDefault("email", ""));
        user.setCedula(entry.getOrDefault("cedula", ""));
        user.setPassword(entry.getOrDefault("password", ""));
        user.setNombreUsuario(entry.getOrDefault("nombreUsuario", ""));

        return user;
    }
}