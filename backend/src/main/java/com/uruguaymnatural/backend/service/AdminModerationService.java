package com.uruguaymnatural.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.ActivityDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.repository.ActivityRepository;
import com.uruguaymnatural.backend.repository.BusinessRepository;

/**
 * The administrator controls providers and activities (CU-23, CU-24). The administrator does
 * not manage the providers' activities: they can only take down what is inappropriate (RN-11).
 */
@Service
public class AdminModerationService {

    private final BusinessRepository businessRepository;
    private final ActivityRepository activityRepository;
    private final ActivityService activityService;
    private final EmailService emailService;

    public AdminModerationService(BusinessRepository businessRepository, ActivityRepository activityRepository,
            ActivityService activityService, EmailService emailService) {
        this.businessRepository = businessRepository;
        this.activityRepository = activityRepository;
        this.activityService = activityService;
        this.emailService = emailService;
    }

    /** Every published activity, for the administrator to look through. */
    @Transactional(readOnly = true)
    public List<ActivityDto> listActivities() {
        return activityRepository.findPublished().stream().map(ActivityDto::from).toList();
    }

    /**
     * CU-24: removes an inappropriate activity from the portal. Its coming dates are cancelled
     * and the tourists who paid get 100 % back (RN-28). The provider is told by email.
     */
    @Transactional
    public void removeActivity(Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .filter(Activity::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la actividad " + activityId + "."));
        activityService.remove(activity, "Uruguay Mnatural dio de baja la actividad");
        emailService.send(activity.getBusiness().getEmail(),
                "Dimos de baja una actividad",
                "Hola. Uruguay Mnatural dio de baja la actividad \"" + activity.getName() + "\" por "
                        + "considerarla inapropiada. Sus fechas se cancelaron y a los turistas que habían pagado "
                        + "se les devolvió el 100 %.\nSi tenés dudas, respondé a este correo.");
    }

    /**
     * CU-23: removes a provider. They can no longer log in, their activities stop being
     * published and the tourists who paid get 100 % back (RN-30).
     */
    @Transactional
    public void removeBusiness(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el emprendimiento " + businessId + "."));
        if (!business.isApproved()) {
            throw new BusinessRuleException("Solo se puede eliminar a un prestador aprobado.");
        }
        activityRepository.findByBusinessIdAndActiveTrueOrderByNameAsc(businessId)
                .forEach(activity -> activityService.remove(activity, "Uruguay Mnatural dio de baja al prestador"));
        business.remove();
        emailService.send(business.getEmail(),
                "Dimos de baja tu emprendimiento",
                "Hola. Uruguay Mnatural dio de baja el emprendimiento \"" + business.getLegalName() + "\". "
                        + "Ya no vas a poder iniciar sesión y tus actividades dejaron de estar publicadas.\n"
                        + "Si tenés dudas, respondé a este correo.");
    }
}
