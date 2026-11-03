package hu.mik.pte.prog5.filmtar.model;

import java.util.Set;

public record AppUser(String username, String passwordHash, Set<String> roles) {
}
