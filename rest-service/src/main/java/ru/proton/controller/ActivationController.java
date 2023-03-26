package ru.proton.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.proton.service.UserActivationService;

@RequestMapping("/user")
@RestController
public class ActivationController {
    private final UserActivationService userActivationService;

    public ActivationController(UserActivationService userActivationService) {
        this.userActivationService = userActivationService;
    }

    @GetMapping("/activation")
    public ResponseEntity<?> activation(@RequestParam("id") String id) {
        boolean answer = userActivationService.activation(id);
        if (answer) {
            return ResponseEntity.ok().body("Регистрация успешно завершена!");
        }
        return ResponseEntity.internalServerError().build();
    }
}
