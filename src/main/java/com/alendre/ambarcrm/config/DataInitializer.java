package com.alendre.ambarcrm.config;

import com.alendre.ambarcrm.model.Usuario;
import com.alendre.ambarcrm.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner criarUsuarioAdmin(UsuarioRepository repository, PasswordEncoder encoder) {
        return args -> {
            // Se não tiver nenhum usuário no banco, cria o admin padrão
            if (repository.count() == 0) {
                Usuario admin = new Usuario();
                admin.setLogin("admin");
                // A senha "123" será criptografada agora!
                admin.setSenha(encoder.encode("123")); 
                
                repository.save(admin);
                System.out.println("✅ USUÁRIO ADMIN CRIADO COM SUCESSO!");
                System.out.println("🔑 Login: admin | Senha: 123");
            }
        };
    }
}