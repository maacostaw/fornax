# JavaService

API REST (Spring Boot + Maven) con CRUD completo para **Usuario**, **Producto** y **Pedido**.

## Stack

- Java 21 / Spring Boot 3.3
- Spring Web, Spring Data JPA, Bean Validation
- Base de datos PostgreSQL

## Base de datos

Crea el usuario y la base de datos en PostgreSQL (nota: usa comillas simples rectas
`'...'` para la contraseña y `TO` en los `GRANT`):

```sql
CREATE USER fronax_user WITH PASSWORD '2gKZ7x1TiN0>';
CREATE DATABASE fronax;
GRANT ALL PRIVILEGES ON DATABASE fronax TO fronax_user;
-- Ejecutar conectado a la base 'fronax':
GRANT ALL ON SCHEMA public TO fronax_user;
```

La conexión se configura en `src/main/resources/application.properties`. Puedes
sobreescribir `DB_URL` y `DB_USER` por variables de entorno; la contraseña está
fijada por sus caracteres especiales.

## Ejecutar

```bash
mvn spring-boot:run
```

La aplicación arranca en `http://localhost:8080`. Hibernate crea/actualiza las
tablas automáticamente (`ddl-auto=update`).

## Entidades

| Entidad     | Campos                                                        |
|-------------|--------------------------------------------------------------|
| Usuario     | id, nombre, email, contrasena                                |
| Producto    | id, nombre, precio, stock                                    |
| Pedido      | id, usuario, producto, estado, fecha                         |

Un pedido representa **un único producto** encargado por un usuario.

## Endpoints

### Usuarios — `/api/usuarios`
| Método | Ruta                  | Descripción            |
|--------|-----------------------|------------------------|
| GET    | `/api/usuarios`       | Listar todos           |
| GET    | `/api/usuarios/{id}`  | Obtener por id         |
| POST   | `/api/usuarios`       | Crear                  |
| PUT    | `/api/usuarios/{id}`  | Actualizar             |
| DELETE | `/api/usuarios/{id}`  | Eliminar               |

### Productos — `/api/productos`
Mismas operaciones CRUD que usuarios.

### Pedidos — `/api/pedidos`
| Método | Ruta                              | Descripción                          |
|--------|-----------------------------------|--------------------------------------|
| GET    | `/api/pedidos`                    | Listar (filtro opcional `?usuarioId=`) |
| GET    | `/api/pedidos/{id}`               | Obtener por id                       |
| POST   | `/api/pedidos`                    | Crear                                |
| PUT    | `/api/pedidos/{id}`               | Actualizar                           |
| PATCH  | `/api/pedidos/{id}/estado?estado=`| Cambiar estado                       |
| DELETE | `/api/pedidos/{id}`               | Eliminar                             |

Estados posibles: `PENDIENTE`, `PAGADO`, `ENVIADO`, `ENTREGADO`, `CANCELADO`.

## Ejemplos (curl)

```bash
# Crear usuario
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana","email":"ana@mail.com","contrasena":"secreto"}'

# Crear producto
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Teclado","precio":29.99,"stock":100}'

# Crear pedido (un usuario encarga un producto)
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId":1,"productoId":1}'

# Cambiar estado del pedido
curl -X PATCH "http://localhost:8080/api/pedidos/1/estado?estado=PAGADO"
```
