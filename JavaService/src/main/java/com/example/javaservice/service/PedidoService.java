package com.example.javaservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.javaservice.dto.ItemPedidoRequest;
import com.example.javaservice.dto.PedidoRequest;
import com.example.javaservice.exception.ResourceNotFoundException;
import com.example.javaservice.model.EstadoPedido;
import com.example.javaservice.model.ItemPedido;
import com.example.javaservice.model.Pedido;
import com.example.javaservice.model.Producto;
import com.example.javaservice.model.Usuario;
import com.example.javaservice.repository.PedidoRepository;

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

    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
    }

    @Transactional
    public Pedido crear(PedidoRequest request) {
        Usuario usuario = usuarioService.obtenerPorId(request.getUsuarioId());

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setEstado(request.getEstado() != null ? request.getEstado() : EstadoPedido.PENDIENTE);
        pedido.setFecha(LocalDateTime.now());

        for (ItemPedidoRequest itemReq : request.getItems()) {
            pedido.getItems().add(construirItem(pedido, itemReq));
        }

        pedido.setTotal(calcularTotal(pedido));
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido actualizar(Long id, PedidoRequest request) {
        Pedido pedido = obtenerPorId(id);

        Usuario usuario = usuarioService.obtenerPorId(request.getUsuarioId());
        pedido.setUsuario(usuario);
        if (request.getEstado() != null) {
            pedido.setEstado(request.getEstado());
        }

        pedido.getItems().clear();
        for (ItemPedidoRequest itemReq : request.getItems()) {
            pedido.getItems().add(construirItem(pedido, itemReq));
        }

        pedido.setTotal(calcularTotal(pedido));
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cambiarEstado(Long id, EstadoPedido estado) {
        Pedido pedido = obtenerPorId(id);
        pedido.setEstado(estado);
        return pedidoRepository.save(pedido);
    }

    public void eliminar(Long id) {
        Pedido pedido = obtenerPorId(id);
        pedidoRepository.delete(pedido);
    }

    private ItemPedido construirItem(Pedido pedido, ItemPedidoRequest itemReq) {
        Producto producto = productoService.obtenerPorId(itemReq.getProductoId());
        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setProducto(producto);
        item.setCantidad(itemReq.getCantidad());
        item.setPrecioUnitario(producto.getPrecio());
        return item;
    }

    private BigDecimal calcularTotal(Pedido pedido) {
        return pedido.getItems().stream()
                .map(item -> item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
