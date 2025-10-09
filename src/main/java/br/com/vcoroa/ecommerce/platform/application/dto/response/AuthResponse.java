package br.com.vcoroa.ecommerce.platform.application.dto.response;

import br.com.vcoroa.ecommerce.platform.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private Role role;
}
