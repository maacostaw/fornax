package com.example.usuarioservice.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.usuarioservice.entities.Usuario;
import com.example.usuarioservice.repositories.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) throws EntityNotFoundException {
        Optional<Usuario> usuariosEncontrados = usuarioRepository.findById(id);
        if(usuariosEncontrados.isEmpty()){
            String errorMsg = String.format("Usuario (%d) no encontrado", id);
            throw new EntityNotFoundException(errorMsg);
        }
        return usuariosEncontrados.get();
    }

    public Usuario crear(Usuario usuario) throws IllegalArgumentException {
        validar(usuario);
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            String errorMsg = String.format("Ya existe un usuario con el email %s", usuario.getEmail());
            throw new IllegalArgumentException(errorMsg);
        }
        //Por si a caso lo mandó el usuario
        usuario.setId(null);
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario usuario) throws EntityNotFoundException, IllegalArgumentException {
        validar(usuario);
        Optional<Usuario> usuariosEncontrados = usuarioRepository.findById(id);
        if(usuariosEncontrados.isEmpty()){
            String errorMsg = String.format("Usuario (%d) no encontrado", id);
            throw new EntityNotFoundException(errorMsg);
        }
        Usuario usuarioEncontrado = usuariosEncontrados.get();
        // Si no es el mismo email y el nuevo email ya existe
        if (!usuarioEncontrado.getEmail().equals(usuario.getEmail()) && usuarioRepository.existsByEmail(usuario.getEmail())) {
            String errorMsg = String.format("Ya existe un usuario con el email %s", usuario.getEmail());
            throw new IllegalArgumentException(errorMsg);
        }
        usuario.setId(usuarioEncontrado.getId());
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        Optional<Usuario> usuariosEncontrados = usuarioRepository.findById(id);
        if(usuariosEncontrados.isEmpty()){
            String errorMsg = String.format("Usuario (%d) no encontrado", id);
            throw new EntityNotFoundException(errorMsg);
        }
        Usuario usuarioEncontrado = usuariosEncontrados.get();
        usuarioRepository.delete(usuarioEncontrado);
    }

    private void validar(Usuario usuario) throws IllegalArgumentException {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (!usuario.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("El email debe tener un formato válido");
        }
        if (usuario.getContrasena() == null || usuario.getContrasena().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
    }
}
