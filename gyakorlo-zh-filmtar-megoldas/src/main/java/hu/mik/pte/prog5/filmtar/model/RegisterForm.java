package hu.mik.pte.prog5.filmtar.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterForm {

    // 9-1
    @NotBlank(message = "{validation.notBlank}")
    private String username;

    // 9-1
    @Size(min = 4, message = "{validation.password.size}")
    private String password;

    private String confirmPassword;
}
