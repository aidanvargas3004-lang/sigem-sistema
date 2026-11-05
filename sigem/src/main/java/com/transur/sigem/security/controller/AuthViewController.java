package com.transur.sigem.security.controller;

import com.transur.sigem.security.dto.RegisterRequest;
import com.transur.sigem.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;


@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthService authService;

    @GetMapping("/register")
    public String showRegister(Model model) {
        // Inicializa el objeto vacío para el formulario
        model.addAttribute("usuario", new RegisterRequest());
        return "login/register";
    }

    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("usuario") RegisterRequest dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttrs) {

        // Si hay errores de validación (@NotBlank, @Email), regresa al form
        if (result.hasErrors()) {
            return "login/register";
        }

        try {
            authService.registrarUsuario(dto);

            // Mensaje de éxito visible en el login
            redirectAttrs.addFlashAttribute("msgSuccess",
                    "¡Cuenta creada exitosamente! Por favor inicie sesión.");
            return "redirect:/login";

        } catch (Exception e) {
            // Muestra error específico (ej: usuario duplicado)
            model.addAttribute("msgError", e.getMessage());
            return "login/register";
        }

    }
    @GetMapping("/login")
    public String showLogin(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/inicio";
        }
        return "login/login";
    }

    @GetMapping("/inicio")
    public String inicio(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_GERENCIA".equals(role)) return "redirect:/gerencia/dashboard";
            if ("ROLE_VENTAS".equals(role)) return "redirect:/ventas/dashboard";
            if ("ROLE_MARKETING".equals(role)) return "redirect:/marketing/dashboard";
            if ("ROLE_RRHH".equals(role)) return "redirect:/rrhh/dashboard";
            if ("ROLE_LOGISTICA".equals(role)) return "redirect:/logistica/dashboard";
            if ("ROLE_OPERACIONES".equals(role)) return "redirect:/operaciones/dashboard";
            if ("ROLE_ADMIN".equals(role)) return "redirect:/dashboard";
            if ("ROLE_CLIENTE".equals(role)) return "redirect:/cliente/dashboard";
        }
        return "redirect:/acceso-denegado";
    }

    /** Acepta cualquier método: Spring Security reenvía aquí también los POST denegados (rol o CSRF). */
    @RequestMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "login/acceso-denegado";
    }
}