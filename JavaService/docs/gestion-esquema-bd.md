# Gestión del esquema de base de datos

Guía de cómo trabajar con los cambios de esquema en JavaService (Spring Boot + JPA/Hibernate + PostgreSQL).

## El parámetro clave: `ddl-auto`

Se configura en `src/main/resources/application.properties`:

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

| Valor         | Qué hace                                                                 | Cuándo usarlo                                                        |
|---------------|--------------------------------------------------------------------------|---------------------------------------------------------------------|
| `create-drop` | **Borra y recrea todo** el esquema en cada arranque (y lo borra al cerrar). Se pierden los datos. | Desarrollo temprano: cada arranque te da un esquema limpio sin SQL manual. **Actual.** |
| `create`      | Borra y recrea al arrancar, pero no borra al cerrar.                      | Similar a create-drop.                                              |
| `update`      | Añade tablas/columnas nuevas, pero **nunca borra** columnas ni tablas viejas. | Conserva datos, pero acumula "basura" del esquema antiguo.          |
| `validate`    | Solo comprueba que el esquema coincide con las entidades; no toca nada.   | Producción con migraciones gestionadas.                            |
| `none`        | No hace nada.                                                            | Producción.                                                        |

## Paso a paso cuando cambias el esquema

Modificar el esquema = cambiar una clase `@Entity` (añadir/quitar campos, relaciones, etc.).

### Camino A — desarrollo rápido (`create-drop`, el actual)

1. Cambias la clase Java.
2. `mvn compile` para verificar que compila.
3. Reinicias la app (`mvn spring-boot:run`).
4. Hibernate tira el esquema y lo recrea con la forma nueva. **Sin SQL manual.**
5. (Opcional) recargas datos de prueba.

> Coste: se pierden todos los datos en cada reinicio. Irrelevante mientras prototipas.

### Camino B — conservando datos (`update` o `validate`)

1. Cambias la clase Java.
2. `mvn compile`.
3. **Escribes tú el SQL de migración** (`ALTER TABLE …`), porque `update` solo *añade* (no quita ni renombra) y `validate` no toca nada.
4. Ejecutas ese SQL contra la base.
5. Reinicias la app. Con `validate`, avisa si el esquema y las entidades no cuadran.

## Ejemplo real: migración a "un pedido = un producto"

Cuando se eliminó la entidad `ItemPedido` y `Pedido` pasó a tener un solo `producto_id`
(quitando `total` y las líneas de pedido), con `ddl-auto=update` había que aplicar esto a mano.
Con `create-drop` NO hace falta: basta reiniciar. Se deja como referencia del Camino B:

```sql
-- 1) Eliminar la tabla de líneas de pedido
DROP TABLE IF EXISTS items_pedido;

-- 2) Añadir la nueva columna producto_id (de momento nullable)
ALTER TABLE pedidos ADD COLUMN producto_id BIGINT;

-- 3) Si ya hay pedidos con datos, asígnales un producto válido antes del paso 4.
--    (si la tabla pedidos está vacía, sáltate esta línea)
-- UPDATE pedidos SET producto_id = 1 WHERE producto_id IS NULL;

-- 4) Hacer la columna obligatoria y crear la clave foránea hacia productos
ALTER TABLE pedidos ALTER COLUMN producto_id SET NOT NULL;
ALTER TABLE pedidos ADD CONSTRAINT fk_pedido_producto
    FOREIGN KEY (producto_id) REFERENCES productos(id);

-- 5) Eliminar la columna total
ALTER TABLE pedidos DROP COLUMN total;
```

> El paso 4 (`SET NOT NULL`) falla si hay filas sin `producto_id`; por eso el paso 3.
> Alternativa limpia en desarrollo: `DROP TABLE items_pedido; DROP TABLE pedidos;` y dejar
> que Hibernate recree `pedidos`.

## Recomendación de evolución

- **Ahora (modelo en flujo):** `create-drop`. Cero scripts manuales.
- **Cuando el esquema se estabilice y haya datos que conservar:** `validate` + un gestor de
  migraciones como **Flyway** o **Liquibase**. Versiona cada cambio en archivos
  (`V1__init.sql`, `V2__pedido_producto.sql`, …) que se aplican de forma automática y ordenada.
  Es el estándar en producción, en lugar de escribir `ALTER TABLE` a mano.
