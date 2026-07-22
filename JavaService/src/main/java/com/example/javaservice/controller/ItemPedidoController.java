package com.example.javaservice.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.javaservice.dto.ItemPedidoRequest;
import com.example.javaservice.model.ItemPedido;
import com.example.javaservice.service.ItemPedidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/items-pedido")
public class ItemPedidoController {

    private final ItemPedidoService itemPedidoService;

    public ItemPedidoController(ItemPedidoService itemPedidoService) {
        this.itemPedidoService = itemPedidoService;
    }

    @GetMapping
    public List<ItemPedido> listar(@RequestParam(required = false) Long pedidoId) {
        return pedidoId != null
                ? itemPedidoService.listarPorPedido(pedidoId)
                : itemPedidoService.listar();
    }

    @GetMapping("/{id}")
    public ItemPedido obtener(@PathVariable Long id) {
        return itemPedidoService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<ItemPedido> crear(@RequestParam Long pedidoId,
                                            @Valid @RequestBody ItemPedidoRequest request) {
        ItemPedido creado = itemPedidoService.crear(pedidoId, request);
        return ResponseEntity.created(URI.create("/api/items-pedido/" + creado.getId())).body(creado);
    }

    @PutMapping("/{id}")
    public ItemPedido actualizar(@PathVariable Long id, @Valid @RequestBody ItemPedidoRequest request) {
        return itemPedidoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        itemPedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
