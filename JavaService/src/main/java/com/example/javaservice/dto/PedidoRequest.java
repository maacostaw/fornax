package com.example.javaservice.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.javaservice.model.EstadoPedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class PedidoRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;

    private EstadoPedido estado;

    @Valid
    @NotEmpty(message = "El pedido debe tener al menos un item")
    private List<ItemPedidoRequest> items = new ArrayList<>();

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public List<ItemPedidoRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemPedidoRequest> items) {
        this.items = items;
    }
}
