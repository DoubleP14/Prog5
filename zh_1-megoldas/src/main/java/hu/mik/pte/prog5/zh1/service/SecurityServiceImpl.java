package hu.mik.pte.prog5.zh1.service;

import org.springframework.stereotype.Service;

// A SecurityService interface-nek csak default metodusa van, de ahhoz, hogy injektalni
// (es a @PostFilter-ben @securityService-kent hivni) tudjuk, kell egy Spring bean belole.
@Service("securityService")
public class SecurityServiceImpl implements SecurityService {
}
