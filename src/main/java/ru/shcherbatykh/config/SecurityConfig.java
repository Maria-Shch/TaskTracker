package ru.shcherbatykh.config;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.security.LogoutSuccessHandler;
import ru.shcherbatykh.services.UserService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final LogoutSuccessHandler logoutSuccessHandler;
    private final UserService userService;
    private static final Logger logger = Logger.getLogger(SecurityConfig.class);

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, LogoutSuccessHandler logoutSuccessHandler, UserService userService) {
        this.userDetailsService = userDetailsService;
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.userService = userService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        logger.debug("Method 'configure' started working.");

        http
            .csrf().disable()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/auth/registration").permitAll()
                .antMatchers("/auth/failSignIn").permitAll()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() //this line makes css styles work
                .anyRequest()
                .authenticated()
            .and()
                .formLogin()
                .loginPage("/auth/login").permitAll()
                .successHandler(
                        (request, response, authentication) -> {
                            User user = userService.findByUsername(authentication.getName());
                            response.sendRedirect("/task/allTasks/all");
                            logger.info("User with id=" + user.getId() + " signed in");
                        }
                )
                .failureUrl("/auth/failureLogin")
            .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout", "POST"))
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler(logoutSuccessHandler);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth){
        logger.debug("Method 'configure' started working.");
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    protected PasswordEncoder passwordEncoder() {
        logger.debug("Bean 'passwordEncoder' was created.");
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    protected DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        logger.debug("Bean 'daoAuthenticationProvider' was created.");
        return daoAuthenticationProvider;
    }

    @Bean @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        logger.debug("Bean 'authenticationManagerBean' was created.");
        return super.authenticationManagerBean();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        logger.debug("Bean 'bCryptPasswordEncoder' was created.");
        return new BCryptPasswordEncoder();
    }
}
