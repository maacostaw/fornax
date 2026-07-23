# fornax

Microservices App. Tres servicios independientes (Spring Boot + Maven), cada uno con su
propia base de datos PostgreSQL. **No hay claves foráneas entre servicios**: `PedidoService`
guarda `usuarioId` y `productoId` como datos sueltos; la consistencia se gestiona fuera.

## Servicios

| Servicio          | Puerto | Base de datos     | Usuario BD             | API              |
|-------------------|--------|-------------------|------------------------|------------------|
| `UsuarioService`  | 8081   | `fornax_usuario`  | `usr_fornax_user`      | `/api/usuarios`  |
| `ProductoService` | 8082   | `fornax_producto` | `usr_fornax_producto`  | `/api/productos` |
| `PedidoService`   | 8083   | `fornax_pedido`   | `usr_fornax_pedido`    | `/api/pedidos`   |

Cada uno tiene su `application.properties`, donde `DB_URL`, `DB_USER` y `DB_PASSWORD` se
pueden sobreescribir por variables de entorno.

## Stack

- Java 21 / Spring Boot 3.3
- Spring Web, Spring Data JPA, Bean Validation
- PostgreSQL, ModelMapper

## Ejecutar

Cada servicio se arranca por separado, desde su propia carpeta:

```bash
cd UsuarioService  && mvn spring-boot:run   # http://localhost:8081
cd ProductoService && mvn spring-boot:run   # http://localhost:8082
cd PedidoService   && mvn spring-boot:run   # http://localhost:8083
```

Hibernate crea el esquema al arrancar (`ddl-auto=create-drop`); ver
[docs/gestion-esquema-bd.md](docs/gestion-esquema-bd.md).

Para comprobar que todo funciona: [docs/pruebas-manuales.md](docs/pruebas-manuales.md).

## Entidades

| Servicio          | Entidad  | Campos                                          |
|-------------------|----------|-------------------------------------------------|
| `UsuarioService`  | Usuario  | id, nombre, email, contrasena                   |
| `ProductoService` | Producto | id, nombre, precio, stock                       |
| `PedidoService`   | Pedido   | id, usuarioId, productoId, estado, fecha        |

Un pedido representa **un único producto** encargado por un usuario.

## Endpoints

### Usuarios — `http://localhost:8081/api/usuarios`
| Método | Ruta                  | Descripción            |
|--------|-----------------------|------------------------|
| GET    | `/api/usuarios`       | Listar todos           |
| GET    | `/api/usuarios/{id}`  | Obtener por id         |
| POST   | `/api/usuarios`       | Crear                  |
| PUT    | `/api/usuarios/{id}`  | Actualizar             |
| DELETE | `/api/usuarios/{id}`  | Eliminar               |

### Productos — `http://localhost:8082/api/productos`
Mismas operaciones CRUD que usuarios.

### Pedidos — `http://localhost:8083/api/pedidos`
| Método | Ruta                              | Descripción                            |
|--------|-----------------------------------|----------------------------------------|
| GET    | `/api/pedidos`                    | Listar (filtro opcional `?usuarioId=`) |
| GET    | `/api/pedidos/{id}`               | Obtener por id                         |
| POST   | `/api/pedidos`                    | Crear                                  |
| PUT    | `/api/pedidos/{id}`               | Actualizar                             |
| PATCH  | `/api/pedidos/{id}/estado?estado=`| Cambiar estado                         |
| DELETE | `/api/pedidos/{id}`               | Eliminar                               |

Estados posibles: `PENDIENTE`, `PAGADO`, `ENVIADO`, `ENTREGADO`, `CANCELADO`.

`PedidoService` **no valida** que el `usuarioId` o el `productoId` existan: solo comprueba
que vengan informados.

## Ejemplos (curl)

```bash
# Crear usuario
curl -X POST http://localhost:8081/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana","email":"ana@mail.com","contrasena":"secreto"}'

# Crear producto
curl -X POST http://localhost:8082/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Teclado","precio":29.99,"stock":100}'

# Crear pedido (referencia por id, sin FK)
curl -X POST http://localhost:8083/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId":1,"productoId":1}'

# Cambiar estado del pedido
curl -X PATCH "http://localhost:8083/api/pedidos/1/estado?estado=PAGADO"
```
