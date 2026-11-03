package hu.mik.pte.prog5.konyvtar.service;

import org.springframework.stereotype.Service;

// 5-1 es 8-4: interface-bol nem lesz bean, ezert kell ez az ures osztaly.
// A nevre a @PostFilter @securityService hivatkozasa miatt van szukseg.
@Service("securityService")
public class SecurityServiceImpl implements SecurityService {
}
