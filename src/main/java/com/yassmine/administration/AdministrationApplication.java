package com.yassmine.administration;

import com.yassmine.administration.model.ConfigItem;
import com.yassmine.administration.model.Country;
import com.yassmine.administration.model.Role;
import com.yassmine.administration.repository.ConfigItemRepository;
import com.yassmine.administration.repository.CountryRepository;
import com.yassmine.administration.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class AdministrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdministrationApplication.class, args);
    }
}
