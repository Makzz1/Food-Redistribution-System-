package com.foodredistribution.foodredistribution.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.jwt.JwtService;
import com.foodredistribution.foodredistribution.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    // Dynamic frontend URL from config (defaults to localhost for dev)
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // Google guarantees email
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            // New user from Google
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setEmailVerified(true);
            // Notice: We don't set a password, role, or location here.
            userRepository.save(user);
        }

        // Determine if the profile is complete (needs Role and Location)
        boolean profileComplete = (user.getRole() != null && user.getLatitude() != null && user.getLongitude() != null);
        String roleStr = user.getRole() != null ? user.getRole().name() : null;

        // Generate JWT token with custom claims
        String token = jwtService.generateToken(user.getEmail(), roleStr, profileComplete);

        // Redirect back to frontend with the token (URL from config)
        String targetUrl = frontendUrl + "/oauth2/redirect?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
