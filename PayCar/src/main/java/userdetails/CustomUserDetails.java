package userdetails;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.entity.Usuario;

public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(new SimpleGrantedAuthority(usuario.getRol()));
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
    	if (usuario != null) {
			return usuario.getUsername(); // o getEmail() si es el correo electrónico lo que almacenas aquí
		}
		return null;
	
    }

    // Implementa los métodos restantes de UserDetails según sea necesario

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Implementa la lógica según tus requisitos
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Implementa la lógica según tus requisitos
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Implementa la lógica según tus requisitos
    }

    @Override
    public boolean isEnabled() {
        return true;  // Implementa la lógica según tus requisitos
    }
}
