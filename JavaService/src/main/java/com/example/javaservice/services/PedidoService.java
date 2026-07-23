package com.example.javaservice.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.javaservice.entities.Pedido;
import com.example.javaservice.entities.Producto;
import com.example.javaservice.entities.Usuario;
import com.example.javaservice.enums.EstadoPedido;
import com.example.javaservice.repositories.PedidoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioService usuarioService;
    private final ProductoService productoService;

    public PedidoService(PedidoRepository pedidoRepository,
                         UsuarioService usuarioService,
                         ProductoService productoService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioService = usuarioService;
        this.productoService = productoService;
    }

    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> listarPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    public Pedido obtenerPorId(Long id) throws EntityNotFoundException {
        Optional<Pedido> pedidosEncontrados = pedidoRepository.findById(id);
        if(pedidosEncontrados.isEmpty()){
            String errorMsg = String.format("Pedido (%d) no encontrado", id);
            throw new EntityNotFoundException(errorMsg);
        }
        return pedidosEncontrados.get();
    }

    @Transactional
    public Pedido crear(Pedido request) throws EntityNotFoundException, IllegalArgumentException {
        validar(request);
        Usuario usuario = usuarioService.obtenerPorId(request.getUsuario().getId());
        Producto producto = productoService.obtenerPorId(request.getProducto().getId());

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setProducto(producto);
        pedido.setEstado(request.getEstado() != null ? request.getEstado() : EstadoPedido.PENDIENTE);
        pedido.setFecha(LocalDateTime.now());

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido actualizar(Long id, Pedido request) throws EntityNotFoundException, IllegalArgumentException {
        validar(request);
        Pedido pedido = obtenerPorId(id);

        Usuario usuario = usuarioService.obtenerPorId(request.getUsuario().getId());
        Producto producto = productoService.obtenerPorId(request.getProducto().getId());
        pedido.setUsuario(usuario);
        pedido.setProducto(producto);
        if (request.getEstado() != null) {
            pedido.setEstado(request.getEstado());
        }

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cambiarEstado(Long id, EstadoPedido estado) throws EntityNotFoundException {
        Pedido pedido = obtenerPorId(id);
        pedido.setEstado(estado);
        return pedidoRepository.save(pedido);
    }

    public void eliminar(Long id) {
        Pedido pedido = obtenerPorId(id);
        pedidoRepository.delete(pedido);
    }

    private void validar(Pedido request) throws IllegalArgumentException {
        if (request.getUsuario() == null || request.getUsuario().getId() == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        if (request.getProducto() == null || request.getProducto().getId() == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
    }
}
