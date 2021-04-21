package uz.pdp.demo.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.demo.model.Response;
import uz.pdp.demo.service.TurniketService;

@RestController
@RequestMapping("/api/turniket")
public class TurniketController {

    TurniketService turniketService;

    public TurniketController(TurniketService turniketService) {
        this.turniketService = turniketService;
    }

    @GetMapping
    public HttpEntity<?> enter() {
        final Response response = turniketService.enterWork();
        return ResponseEntity.status(response.isStatus() ? 201 : 409).body(response);
    }

    @PutMapping
    public HttpEntity<?> exit() {
        final Response response = turniketService.exitWork();
        return ResponseEntity.status(response.isStatus() ? 201 : 409).body(response);
    }
}
