package com.transur.sigem.cliente.service;

import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.cliente.repository.ClienteRepository;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cliente asociado a la cuenta con sesión iniciada; base del aislamiento de datos del portal. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteSesionService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    public Cliente clienteActual(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
        if (cliente == null && usuario.getCliente() != null) cliente = usuario.getCliente();
        if (cliente == null) throw new IllegalStateException("La cuenta no tiene un cliente asociado. Contacte a Administración.");
        return cliente;
    }
}
