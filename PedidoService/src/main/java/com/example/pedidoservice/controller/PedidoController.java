package com.example.pedidoservice.controller;

import java.net.URI;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.pedidoservice.dtos.PedidoDTO;
import com.example.pedidoservice.entities.Pedido;
import com.example.pedidoservice.enums.EstadoPedido;
import com.example.pedidoservice.services.PedidoService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    private final ModelMapper modelMapper;

    public PedidoController(PedidoService pedidoService, ModelMapper modelMapper) {
        this.pedidoService = pedidoService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public List<PedidoDTO> listar(@RequestParam(required = false) Long usuarioId) {
        List<Pedido> pedidos = usuarioId != null
                ? pedidoService.listarPorUsuario(usuarioId)
                : pedidoService.listar();
        return modelMapper.map(pedidos, new TypeToken<List<PedidoDTO>>() {}.getType());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            Pedido pedido = pedidoService.obtenerPorId(id);
            return ResponseEntity.status(HttpStatus.OK).body(modelMapper.map(pedido, PedidoDTO.class));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody PedidoDTO pedidoDTO) {
        try {
            Pedido pedido = pedidoService.crear(modelMapper.map(pedidoDTO, Pedido.class));
            PedidoDTO creado = modelMapper.map(pedido, PedidoDTO.class);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .location(URI.create("/api/pedidos/" + creado.getId()))
                    .body(creado);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody PedidoDTO pedidoDTO) {
        try {
            Pedido pedido = pedidoService.actualizar(id, modelMapper.map(pedidoDTO, Pedido.class));
            return ResponseEntity.status(HttpStatus.OK).body(modelMapper.map(pedido, PedidoDTO.class));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam EstadoPedido estado) {
        try {
            Pedido pedido = pedidoService.cambiarEstado(id, estado);
            return ResponseEntity.status(HttpStatus.OK).body(modelMapper.map(pedido, PedidoDTO.class));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            pedidoService.eliminar(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
