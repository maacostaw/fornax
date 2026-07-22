package com.example.javaservice.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.javaservice.dto.ItemPedidoRequest;
import com.example.javaservice.exception.ResourceNotFoundException;
import com.example.javaservice.model.ItemPedido;
import com.example.javaservice.model.Pedido;
import com.example.javaservice.model.Producto;
import com.example.javaservice.repository.ItemPedidoRepository;
import com.example.javaservice.repository.PedidoRepository;

@Service
public class ItemPedidoService {

    private final ItemPedidoRepository itemPedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoService productoService;

    public ItemPedidoService(ItemPedidoRepository itemPedidoRepository,
                             PedidoRepository pedidoRepository,
                             ProductoService productoService) {
        this.itemPedidoRepository = itemPedidoRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoService = productoService;
    }

    public List<ItemPedido> listar() {
        return itemPedidoRepository.findAll();
    }

    public List<ItemPedido> listarPorPedido(Long pedidoId) {
        return itemPedidoRepository.findByPedidoId(pedidoId);
    }

    public ItemPedido obtenerPorId(Long id) {
        return itemPedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ItemPedido", id));
    }

    @Transactional
    public ItemPedido crear(Long pedidoId, ItemPedidoRequest request) {
        Pedido pedido = obtenerPedido(pedidoId);
        Producto producto = productoService.obtenerPorId(request.getProductoId());

        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setProducto(producto);
        item.setCantidad(request.getCantidad());
        item.setPrecioUnitario(producto.getPrecio());

        ItemPedido guardado = itemPedidoRepository.save(item);
        recalcularTotal(pedido);
        return guardado;
    }

    @Transactional
    public ItemPedido actualizar(Long id, ItemPedidoRequest request) {
        ItemPedido item = obtenerPorId(id);
        Producto producto = productoService.obtenerPorId(request.getProductoId());

        item.setProducto(producto);
        item.setCantidad(request.getCantidad());
        item.setPrecioUnitario(producto.getPrecio());

        ItemPedido guardado = itemPedidoRepository.save(item);
        recalcularTotal(item.getPedido());
        return guardado;
    }

    @Transactional
    public void eliminar(Long id) {
        ItemPedido item = obtenerPorId(id);
        Pedido pedido = item.getPedido();
        itemPedidoRepository.delete(item);
        recalcularTotal(pedido);
    }

    private Pedido obtenerPedido(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", pedidoId));
    }

    private void recalcularTotal(Pedido pedido) {
        List<ItemPedido> items = itemPedidoRepository.findByPedidoId(pedido.getId());
        BigDecimal total = items.stream()
                .map(i -> i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setTotal(total);
        pedidoRepository.save(pedido);
    }
}
