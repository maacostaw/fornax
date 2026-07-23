# Pruebas manuales (smoke test)

Comprobación rápida de que los tres microservicios arrancan, hablan cada uno con su base
de datos y que **ya no existe integridad referencial entre ellos**.

> Con `ddl-auto=create-drop`, cada arranque borra y recrea el esquema. Los ids que se
> muestran abajo (1, 2, …) asumen que empiezas con las bases vacías. Si reutilizas una
> sesión anterior, ajusta los ids.

## 1. Arrancar los tres servicios

Cada uno en su propia terminal, desde la carpeta del servicio:

```bash
cd UsuarioService  && mvn spring-boot:run   # :8081
cd ProductoService && mvn spring-boot:run   # :8082
cd PedidoService   && mvn spring-boot:run   # :8083
```

Espera a ver en cada log:

```
Tomcat started on port 80XX (http) with context path '/'
Started App in N seconds
```

El aviso `table "usuarios" does not exist, skipping` en el primer arranque es normal:
Hibernate intenta borrar tablas que todavía no existen.

Para verificar que los tres puertos están escuchando:

```bash
ss -ltn | grep -E ':(8081|8082|8083) '
```

## 2. Crear un usuario (8081)

```bash
curl -s -X POST http://localhost:8081/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana","email":"ana@mail.com","contrasena":"secreto"}'
```

Esperado:

```json
{"id":1,"nombre":"Ana","email":"ana@mail.com","contrasena":"secreto"}
```

## 3. Crear un producto (8082)

```bash
curl -s -X POST http://localhost:8082/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Teclado","precio":29.99,"stock":100}'
```

Esperado:

```json
{"id":1,"nombre":"Teclado","precio":29.99,"stock":100}
```

## 4. Crear un pedido (8083)

```bash
curl -s -X POST http://localhost:8083/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId":1,"productoId":1}'
```

Esperado:

```json
{"id":1,"usuarioId":1,"productoId":1,"estado":"PENDIENTE","fecha":"..."}
```

## 5. Pedido con ids inexistentes — **la prueba clave**

Confirma que ya no hay claves foráneas ni validación cruzada entre servicios:

```bash
curl -s -X POST http://localhost:8083/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId":9999,"productoId":8888}'
```

Esperado: **se crea sin error** (antes esto daba 404).

```json
{"id":2,"usuarioId":9999,"productoId":8888,"estado":"PENDIENTE","fecha":"..."}
```

## 6. Filtrar pedidos por usuario

```bash
curl -s "http://localhost:8083/api/pedidos?usuarioId=1"
```

Esperado: solo el pedido 1, no el 2.

```json
[{"id":1,"usuarioId":1,"productoId":1,"estado":"PENDIENTE","fecha":"..."}]
```

## 7. Cambiar el estado de un pedido

```bash
curl -s -X PATCH "http://localhost:8083/api/pedidos/1/estado?estado=PAGADO"
```

Esperado: `"estado":"PAGADO"`.

Estados válidos: `PENDIENTE`, `PAGADO`, `ENVIADO`, `ENTREGADO`, `CANCELADO`.

## 8. Validación que sí sigue viva (400)

`PedidoService` ya no comprueba que el usuario exista, pero sí que venga informado:

```bash
curl -s -w " [HTTP %{http_code}]" -X POST http://localhost:8083/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"productoId":1}'
```

Esperado:

```
El usuario es obligatorio [HTTP 400]
```

## 9. Aislamiento de las bases de datos

Cada servicio ve solo su tabla. Conectado a `fornax_pedido` no debe existir `usuarios`
ni `productos`, y la tabla `pedidos` no debe tener ninguna FK:

```bash
psql -U usr_fornax_pedido -d fornax_pedido -c '\d pedidos'
```

En la salida, la sección `Foreign-key constraints` **no debe aparecer**, y las columnas
`usuario_id` / `producto_id` son `bigint not null` sueltas.

## 10. Parar los servicios

`Ctrl+C` en cada terminal, o:

```bash
pkill -f "spring-boot:run"
```

## Resto del CRUD

Los mismos endpoints existen en los tres servicios (listar, obtener por id, actualizar,
eliminar). Ver la tabla completa en el [README](../README.md).

```bash
curl -s http://localhost:8081/api/usuarios          # listar
curl -s http://localhost:8081/api/usuarios/1        # obtener
curl -s -X DELETE http://localhost:8081/api/usuarios/1   # eliminar -> 204
curl -s http://localhost:8081/api/usuarios/99       # inexistente -> 404
```
