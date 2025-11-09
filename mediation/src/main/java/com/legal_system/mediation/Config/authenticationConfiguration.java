//package com.legal_system.mediation.Config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.password.NoOpPasswordEncoder;
//// import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//import org.springframework.security.web.SecurityFilterChain;
//
//import com.legal_system.mediation.Service.MyUserDetailService;
//
////import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;
//
//@Configuration
//@EnableWebSecurity
//// to create our own security configuration (authentication)
//public class authenticationConfiguration {
//
//    @Autowired
//    private MyUserDetailService userDetailService;
//
//   @Bean
//    //to modify the existing filter chain provided by spring 6 dependency
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
//        http.csrf(Customizer->Customizer.disable());
//        http.authorizeHttpRequests(HttpRequest->HttpRequest.anyRequest().authenticated());
//        http.formLogin(Customizer.withDefaults());
//        http.httpBasic(Customizer.withDefaults()); // every time new session id is created once logined
//        return http.build();
//    }
//
//    // @Bean
//    // //user details service is the interface in between the server and the elogin form to get the credentail given in login form and check it with the app properties.
//    // //so we can modify it by creating the bean of UserDetailService
//    // //UserDetailService is an interface so we cant return the object instead we can return the object of the class(InMemoryUserDetailsManager) which implements that interface
//    // public UserDetailsService userDetails(){
//    //     return new InMemoryUserDetailsManager();
//    // }
//
//    @Bean
//    public AuthenticationProvider authenticationProvider(){
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
//        provider.setUserDetailsService(userDetailService);
//        return provider;
//    }
//
//
//
//}
