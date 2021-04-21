package uz.pdp.demo.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.pdp.demo.entity.Turniket;
import uz.pdp.demo.entity.User;
import uz.pdp.demo.model.Response;
import uz.pdp.demo.repository.TurniketRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TurniketService {

    TurniketRepository turniketRepository;

    public TurniketService(TurniketRepository turniketRepository) {
        this.turniketRepository = turniketRepository;
    }

    public Response enterWork() {
        Turniket turniket = new Turniket();
        turniket.setStatus(true);
        turniket.setEnterWork(LocalDateTime.now());
        turniketRepository.save(turniket);
        return new Response("You entered", true);
    }

    public Response exitWork() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !authentication.getPrincipal().equals("anonymousUser")) {

            final User user = (User) authentication.getPrincipal();

            final Optional<Turniket> byCreatedByAndStatus =
                    turniketRepository.findByCreatedByAndStatus(user.getId(), true);

            byCreatedByAndStatus.get().setStatus(false);
            byCreatedByAndStatus.get().setExitWork(LocalDateTime.now());
            turniketRepository.save(byCreatedByAndStatus.get());
            return new Response("You exited", true);
        }
        return new Response("Authentication empty", false);
    }
}
