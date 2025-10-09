package br.com.vcoroa.ecommerce.platform.application.dto.response;

import br.com.vcoroa.ecommerce.platform.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private String username;
    private String email;
    private Role role;
    private String message;
}
