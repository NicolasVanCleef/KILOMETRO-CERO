package com.uruguaymnatural.backend.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.BusinessDetailDto;
import com.uruguaymnatural.backend.dto.BusinessRegistrationDto;
import com.uruguaymnatural.backend.dto.BusinessSummaryDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.InvalidDataException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.model.BusinessStatus;
import com.uruguaymnatural.backend.model.Departments;
import com.uruguaymnatural.backend.model.PersonType;
import com.uruguaymnatural.backend.model.Role;
import com.uruguaymnatural.backend.model.User;
import com.uruguaymnatural.backend.repository.BusinessRepository;
import com.uruguaymnatural.backend.repository.UserRepository;

/**
 * Provider registration (CU-01) and administrator review (CU-02).
 */
@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public BusinessService(BusinessRepository businessRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder, EmailService emailService) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Registers a business and its provider account. The business stays PENDING and the
     * account disabled until an administrator approves it (RN-01).
     */
    @Transactional
    public BusinessDetailDto register(BusinessRegistrationDto data) {
        String email = data.email().trim().toLowerCase();
        String document = normalizeDocument(data.personType(), data.document());
        String representativeName = checkRepresentative(data.personType(), data.representativeName());

        if (!Departments.isValid(data.department())) {
            throw new InvalidDataException("department", "Elegí un departamento de la lista.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("Ya existe una cuenta con el correo " + email + ".");
        }
        // RN-03: an individual can have only one business.
        if (data.personType() == PersonType.INDIVIDUAL
                && businessRepository.existsByPersonTypeAndDocument(PersonType.INDIVIDUAL, document)) {
            throw new BusinessRuleException(
                    "Ya hay un emprendimiento registrado con esa CI. Una persona física puede tener uno solo.");
        }

        User user = new User(email, passwordEncoder.encode(data.password()), Role.PROVIDER, false);
        userRepository.save(user);

        Business business = new Business(user, data.personType(), document, data.legalName().trim(),
                data.phone().trim(), email, representativeName, data.description().trim(),
                data.department(), data.locality().trim(), blankToNull(data.address()));
        businessRepository.save(business);

        return BusinessDetailDto.from(business);
    }

    @Transactional(readOnly = true)
    public List<BusinessSummaryDto> listByStatus(BusinessStatus status) {
        return businessRepository.findByStatusOrderByCreatedAtAsc(status).stream()
                .map(BusinessSummaryDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BusinessDetailDto getDetail(Long id) {
        return BusinessDetailDto.from(findBusiness(id));
    }

    /**
     * Approves a pending business: the provider can now log in with the password they chose,
     * and is told by email (RF-45, RN-04).
     */
    @Transactional
    public BusinessDetailDto approve(Long id) {
        Business business = findPendingBusiness(id);
        business.approve();
        emailService.send(business.getEmail(),
                "Tu emprendimiento fue aprobado",
                "¡Hola! Uruguay Mnatural aprobó el emprendimiento \"" + business.getLegalName() + "\".\n"
                        + "Ya podés iniciar sesión con tu correo y la contraseña que elegiste al registrarte.");
        return BusinessDetailDto.from(business);
    }

    /**
     * Rejects a pending business: the provider cannot log in and is told by email (RF-45, RN-04).
     */
    @Transactional
    public BusinessDetailDto reject(Long id) {
        Business business = findPendingBusiness(id);
        business.reject();
        emailService.send(business.getEmail(),
                "Tu emprendimiento no fue aprobado",
                "Hola. Uruguay Mnatural revisó el emprendimiento \"" + business.getLegalName() + "\" "
                        + "y no fue aprobado.\nSi tenés dudas, respondé a este correo.");
        return BusinessDetailDto.from(business);
    }

    private Business findBusiness(Long id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el emprendimiento " + id + "."));
    }

    private Business findPendingBusiness(Long id) {
        Business business = findBusiness(id);
        if (business.getStatus() != BusinessStatus.PENDING) {
            throw new BusinessRuleException("Este emprendimiento ya fue revisado.");
        }
        return business;
    }

    /**
     * Keeps only the digits. A CI has 7 or 8 digits and a RUT 12 (RN-02).
     */
    private static String normalizeDocument(PersonType personType, String document) {
        String digits = document.replaceAll("[^0-9]", "");
        if (personType == PersonType.INDIVIDUAL && (digits.length() < 7 || digits.length() > 8)) {
            throw new InvalidDataException("document", "La CI tiene que tener 7 u 8 dígitos.");
        }
        if (personType == PersonType.COMPANY && digits.length() != 12) {
            throw new InvalidDataException("document", "El RUT tiene que tener 12 dígitos.");
        }
        return digits;
    }

    /**
     * A company must name its representative; an individual has none (RN-02).
     */
    private static String checkRepresentative(PersonType personType, String representativeName) {
        if (personType == PersonType.INDIVIDUAL) {
            return null;
        }
        if (representativeName == null || representativeName.isBlank()) {
            throw new InvalidDataException("representativeName",
                    "Una persona jurídica tiene que indicar su representante.");
        }
        return representativeName.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
