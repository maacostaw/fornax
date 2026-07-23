package com.example.pedidoservice.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.pedidoservice.entities.Pedido;
import com.example.pedidoservice.enums.EstadoPedido;
import com.example.pedidoservice.repositories.PedidoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
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

        Pedido pedido = new Pedido();
        pedido.setUsuarioId(request.getUsuarioId());
        pedido.setProductoId(request.getProductoId());
        pedido.setEstado(request.getEstado() != null ? request.getEstado() : EstadoPedido.PENDIENTE);
        pedido.setFecha(LocalDateTime.now());

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido actualizar(Long id, Pedido request) throws EntityNotFoundException, IllegalArgumentException {
        validar(request);
        Pedido pedido = obtenerPorId(id);

        pedido.setUsuarioId(request.getUsuarioId());
        pedido.setProductoId(request.getProductoId());
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
        if (request.getUsuarioId() == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        if (request.getProductoId() == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
    }
}
