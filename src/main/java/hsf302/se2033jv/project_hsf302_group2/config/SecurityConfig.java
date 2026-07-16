package hsf302.se2033jv.project_hsf302_group2.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import hsf302.se2033jv.project_hsf302_group2.auth.security.CustomLoginSuccessHandler;
import hsf302.se2033jv.project_hsf302_group2.auth.security.CustomOidcUserService;
import hsf302.se2033jv.project_hsf302_group2.auth.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final CustomUserDetailsService customerUserDetailsService;

    public SecurityConfig(CustomLoginSuccessHandler customLoginSuccessHandler,
                          CustomUserDetailsService customerUserDetailsService) {
        this.customLoginSuccessHandler = customLoginSuccessHandler;
        this.customerUserDetailsService = customerUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // Filter chain cho Customer
    @Bean
    @Order(2)
    public SecurityFilterChain customerFilterChain(HttpSecurity http, ApplicationContext applicationContext, ProfileCompletionFilter profileCompletionFilter) throws Exception {
        CustomOidcUserService oidcUserService = applicationContext.getBean(CustomOidcUserService.class);

        http
                .securityMatcher("/**")
                .authenticationProvider(customerAuthenticationProvider())
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/login**", "/assets/**", "/css/**", "/images/**",
                                "/js/**", "/forgot-password", "/products/**", "/set-password**", "/resend-otp",
                                "/verify-email", "/register", "/home", "/login", "/uploads/**", "/api/banners",
                                "/favicon.ico", "/webjars/**", "/search", "/loyalty-policy",
                                "/customer/payment/vnpay/return", "/ai-chat/**")
                        .permitAll()
                        .requestMatchers("/admin/**").authenticated()
                        .requestMatchers("/customer/profile/**").authenticated()
                        .requestMatchers("/customer/points/**").authenticated()
                        .requestMatchers("/customer/reservations/**").authenticated()
                        .requestMatchers("/manager/**").authenticated()
                        .requestMatchers("/cashier/**").authenticated()
                        .anyRequest().authenticated())

                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customLoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            String errorMessage = "true";
                            if (exception instanceof org.springframework.security.core.userdetails.UsernameNotFoundException) {
                                errorMessage = "username";
                            } else if (exception instanceof org.springframework.security.authentication.BadCredentialsException) {
                                errorMessage = "password";
                            }
                            response.sendRedirect("/login?error=" + errorMessage);
                        })
                        .permitAll())

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService))
                        .successHandler(customLoginSuccessHandler)
                        .failureUrl("/login?error=true")
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(new SecurityContextLogoutHandler())
                        .logoutSuccessUrl("/login?logout=success")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())

                .exceptionHandling(exc -> exc
                        .accessDeniedHandler(accessDeniedHandler()));
        http.addFilterAfter(profileCompletionFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider customerAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customerUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false); // Enable specific UsernameNotFoundException
        return provider;
    }



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }


    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendRedirect(request.getContextPath() + "/403");
        };
    }


}
