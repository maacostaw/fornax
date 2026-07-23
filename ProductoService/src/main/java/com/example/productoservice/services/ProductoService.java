package com.example.productoservice.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.productoservice.entities.Producto;
import com.example.productoservice.repositories.ProductoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    public Producto obtenerPorId(Long id) throws EntityNotFoundException {
        Optional<Producto> productosEncontrados = productoRepository.findById(id);
        if(productosEncontrados.isEmpty()){
            String errorMsg = String.format("Producto (%d) no encontrado", id);
            throw new EntityNotFoundException(errorMsg);
        }
        return productosEncontrados.get();
    }

    public Producto crear(Producto producto) throws IllegalArgumentException {
        validar(producto);
        //Por si a caso lo mandó el usuario
        producto.setId(null);
        return productoRepository.save(producto);
    }

    public Producto actualizar(Long id, Producto producto) throws EntityNotFoundException, IllegalArgumentException {
        validar(producto);
        Optional<Producto> productosEncontrados = productoRepository.findById(id);
        if(productosEncontrados.isEmpty()){
            String errorMsg = String.format("Producto (%d) no encontrado", id);
            throw new EntityNotFoundException(errorMsg);
        }
        Producto productoEncontrado = productosEncontrados.get();
        producto.setId(productoEncontrado.getId());
        return productoRepository.save(producto);
    }

    private void validar(Producto producto) throws IllegalArgumentException {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (producto.getPrecio() == null) {
            throw new IllegalArgumentException("El precio es obligatorio");
        }
        if (producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (producto.getStock() == null) {
            throw new IllegalArgumentException("El stock es obligatorio");
        }
        if (producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }

    public void eliminar(Long id) {
        Optional<Producto> productosEncontrados = productoRepository.findById(id);
        if(productosEncontrados.isEmpty()){
            String errorMsg = String.format("Producto (%d) no encontrado", id);
            throw new EntityNotFoundException(errorMsg);
        }
        Producto productoEncontrado = productosEncontrados.get();
        productoRepository.delete(productoEncontrado);
    }
}
