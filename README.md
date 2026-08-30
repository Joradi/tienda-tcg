# TCG Premium

Aplicación full stack para la gestión de catálogo, inventario y venta de cartas Pokémon TCG, desarrollada con **Java 21, Spring Boot, PostgreSQL, React y TypeScript**.

El sistema permite sincronizar información de cartas desde **Pokémon TCG API**, crear productos comerciales a partir de cartas existentes, administrar stock, gestionar usuarios y permisos, calcular costos de importación y escenarios de rentabilidad, utilizar un carrito de compras y generar órdenes con control de concurrencia para evitar sobreventa.

---

## Tecnologías

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- JWT
- Maven
- JUnit 5
- Mockito
- MockMvc

### Frontend

- React
- TypeScript
- Vite
- React Router
- CSS

---

## Funcionalidades principales

### Catálogo

El catálogo permite consultar los productos disponibles dentro de la tienda.

Cada producto puede contener información como:

- carta asociada;
- set;
- precio;
- condición;
- idioma;
- variante;
- stock disponible.

Los usuarios pueden consultar el detalle de un producto y agregar unidades disponibles al carrito.

---

### Pokémon TCG API

Las cartas utilizadas por la aplicación se obtienen desde **Pokémon TCG API**.

El backend consume la API externa y sincroniza la información necesaria para mantener actualizado el catálogo base de cartas y sets.

La aplicación separa los datos provenientes de Pokémon TCG API de la información comercial administrada localmente.

#### Card

`Card` representa la información propia de una carta Pokémon obtenida desde la API externa.

#### CardSet

`CardSet` representa el set o expansión a la que pertenece una carta.

#### Product

`Product` representa una carta disponible comercialmente dentro de la tienda.

Esto permite que una misma carta pueda tener múltiples representaciones comerciales según:

- idioma;
- variante;
- condición;
- precio;
- stock.

De esta manera, el modelo externo de Pokémon TCG no queda acoplado directamente al modelo comercial de la aplicación.

---

## Autenticación y autorización

La aplicación implementa autenticación mediante **JWT** utilizando Spring Security.

Entre las funcionalidades disponibles se encuentran:

- registro de usuarios;
- inicio de sesión;
- generación y validación de JWT;
- rutas protegidas;
- identificación del usuario autenticado;
- separación de permisos entre usuarios y administradores.

El backend utiliza una configuración de seguridad stateless, por lo que cada solicitud protegida debe incluir un token válido.

Ejemplo:

```text
Authorization: Bearer <token>
```

---

## Administración de productos

Los administradores pueden utilizar las cartas previamente sincronizadas para crear productos comerciales.

Al crear un producto se pueden definir propiedades como:

- carta asociada;
- idioma;
- variante;
- condición;
- precio;
- stock.

La información comercial permanece separada de los datos originales obtenidos desde Pokémon TCG API.

---

## Stock e inventario

El sistema mantiene el stock disponible de cada producto.

Las operaciones relacionadas con inventario incluyen validaciones para evitar estados inválidos y cantidades inconsistentes.

Durante el checkout el stock vuelve a ser validado antes de confirmar una compra.

---

## Carrito

La aplicación permite:

- crear y mantener un carrito;
- agregar productos;
- modificar cantidades;
- eliminar productos;
- consultar el contenido actual;
- validar disponibilidad de stock.

El carrito representa una intención de compra y **no reserva inventario**.

El stock se modifica únicamente cuando se completa correctamente el proceso de checkout.

---

## Checkout y control de concurrencia

El checkout representa uno de los flujos principales de negocio de la aplicación.

Durante este proceso el backend:

1. obtiene los productos involucrados;
2. valida nuevamente el stock disponible;
3. calcula la información necesaria para la orden;
4. protege la operación mediante una transacción;
5. actualiza el inventario;
6. genera la orden correspondiente.

El sistema implementa **control de concurrencia mediante bloqueo de los productos involucrados durante el checkout**.

Esto evita escenarios de overselling.

Por ejemplo, si dos usuarios intentan comprar simultáneamente la última unidad disponible de un producto, solamente una de las operaciones puede completarse correctamente.

```text
Stock inicial: 1

Usuario A ─┐
           ├── Checkout concurrente
Usuario B ─┘
              ↓
       validación + bloqueo
              ↓
        solo una compra
              ↓
          Stock final: 0
```

---

## Órdenes

Las órdenes se generan a partir del contenido válido de un carrito.

El sistema permite:

- crear órdenes;
- asociarlas al usuario correspondiente;
- almacenar los productos comprados;
- conservar información histórica de la compra;
- consultar el historial de órdenes de un usuario.

La información necesaria para representar la compra se conserva de forma independiente de futuros cambios realizados sobre el producto.

> El proyecto no integra un procesador de pagos real. El checkout representa principalmente la lógica comercial, creación de órdenes y actualización de inventario.

---

## Análisis de importaciones y rentabilidad

TCG Premium incluye un módulo destinado a analizar el costo de importar cartas y evaluar su posible rentabilidad comercial.

El sistema permite considerar distintos componentes asociados a una importación, incluyendo:

- valor de la mercancía;
- costos compartidos;
- costos logísticos;
- impuestos;
- costos unitarios;
- precio de referencia local.

Los costos compartidos pueden distribuirse entre los distintos elementos de una importación manteniendo consistencia en los totales calculados.

A partir de estos valores el sistema determina el costo final de los productos y permite analizar su posible venta.

### Escenarios comerciales

El módulo de análisis genera distintos escenarios:

- `QUICK`
- `NORMAL`
- `SLOW`

Cada escenario representa una estrategia comercial diferente y permite comparar elementos como:

- precio de venta;
- costo unitario;
- utilidad;
- margen;
- markup;
- viabilidad de la operación.

Esto permite analizar una importación antes de utilizar sus resultados para la creación de productos comerciales.

---

## Manejo de errores

El backend utiliza excepciones específicas y manejo centralizado de errores para representar adecuadamente distintos problemas de la aplicación.

Entre las respuestas HTTP utilizadas se encuentran:

- `400 Bad Request` — datos o solicitudes inválidas;
- `401 Unauthorized` — autenticación requerida o credenciales inválidas;
- `403 Forbidden` — usuario autenticado sin permisos suficientes;
- `404 Not Found` — recurso inexistente;
- `409 Conflict` — conflictos con reglas o restricciones existentes.

Las validaciones permiten evitar que errores esperados del dominio terminen representándose como errores internos genéricos.

---

## Testing

El backend incluye pruebas automatizadas sobre reglas de negocio y flujos críticos.

Se utilizan:

- JUnit 5;
- Mockito;
- MockMvc;
- pruebas de integración con Spring Boot.

Entre los comportamientos cubiertos se encuentran:

- creación y validación de usuarios;
- detección de correos duplicados;
- autorización y seguridad;
- operaciones de stock;
- carrito;
- checkout;
- órdenes;
- validaciones y escenarios de error;
- cálculos de importación;
- análisis de rentabilidad;
- concurrencia durante el checkout.

Uno de los escenarios de integración comprueba específicamente que dos checkouts concurrentes no puedan vender simultáneamente la última unidad disponible de un producto.

---

## Arquitectura

El backend está organizado por **dominios funcionales**, evitando concentrar todos los controllers, services y repositories de la aplicación en paquetes globales.

Entre los principales dominios se encuentran:

```text
auth
card
cardset
cart
importation
integration
order
product
security
user
```

Cada dominio contiene las clases necesarias según su responsabilidad, por ejemplo:

```text
controller
service
repository
dto
mapper
model
```

cuando corresponde.

Esta organización busca mantener juntas las clases relacionadas con una misma responsabilidad de negocio.

---

## Flujo general del sistema

```text
                  Pokémon TCG API
                         │
                         ▼
                  Card / CardSet
                         │
                         ▼
                     Product
                         │
                         ▼
                 Catálogo / Stock
                         │
                         ▼
                 Cart / CartItem
                         │
                         ▼
                     Checkout
                         │
              ┌──────────┴──────────┐
              ▼                     ▼
           Order              Actualización
                                de stock
```

El análisis comercial funciona como un flujo complementario:

```text
Importation
     │
     ▼
Costos e impuestos
     │
     ▼
Costo unitario
     │
     ▼
QUICK / NORMAL / SLOW
     │
     ▼
Margen / Markup / Viabilidad
```

---

## Separación entre datos externos y datos propios

Una de las decisiones principales del proyecto es mantener separados los datos obtenidos desde servicios externos y los datos pertenecientes al dominio comercial.

```text
Pokémon TCG API
      │
      ▼
Card / CardSet
datos externos
      │
      ▼
Product
datos comerciales
      │
      ├── precio
      ├── stock
      ├── idioma
      ├── condición
      └── variante
```

De esta manera, actualizar información proveniente de Pokémon TCG API no implica reemplazar información comercial administrada por la tienda.

---

## Requisitos

### Backend

- Java 21
- Maven
- PostgreSQL

### Frontend

- Node.js
- npm

También es necesario disponer de acceso a Pokémon TCG API para las funcionalidades de sincronización correspondientes.

---

## Configuración

Antes de iniciar el backend se deben configurar los valores correspondientes a:

- conexión PostgreSQL;
- usuario y contraseña de base de datos;
- configuración JWT;
- credenciales/API key de Pokémon TCG API cuando corresponda.

La configuración debe realizarse mediante las propiedades o variables de entorno utilizadas por el proyecto.

> No se recomienda almacenar contraseñas, API keys o secretos JWT directamente en el repositorio.

---

## Ejecución del backend

Clonar el repositorio:

```bash
git clone https://github.com/Joradi/tienda-tcg.git
cd tienda-tcg
```

Configurar PostgreSQL y las variables necesarias.

Luego ejecutar:

```bash
./mvnw spring-boot:run
```

En Windows:

```bash
mvnw.cmd spring-boot:run
```

También puede ejecutarse:

```bash
./mvnw clean package
java -jar target/*.jar
```

---

## Ejecución de tests

Para ejecutar las pruebas del backend:

```bash
./mvnw clean test
```

En Windows:

```bash
mvnw.cmd clean test
```

---

## Ejecución del frontend

Desde la carpeta correspondiente al frontend:

```bash
npm install
npm run dev
```

Vite iniciará el servidor de desarrollo y mostrará la dirección local disponible en consola.

---

## Interfaz

El frontend fue desarrollado con React y TypeScript para permitir utilizar visualmente las principales funcionalidades expuestas por el backend.

Entre los flujos disponibles se encuentran:

- autenticación;
- catálogo;
- detalle de productos;
- carrito;
- checkout;
- historial de órdenes;
- funcionalidades administrativas.

### Capturas

> Agregar aquí capturas finales del proyecto.

Ejemplo:

```md
![Catálogo](docs/images/catalogo.png)

![Detalle de producto](docs/images/producto.png)

![Carrito](docs/images/carrito.png)

![Administración](docs/images/admin.png)
```

---

## Objetivos técnicos del proyecto

TCG Premium fue desarrollado como un proyecto de práctica backend/full stack orientado a trabajar con problemas más cercanos a una aplicación real.

Entre los conceptos aplicados se encuentran:

- diseño de APIs REST;
- modelado de dominio;
- persistencia relacional;
- integración con APIs externas;
- sincronización de datos;
- DTOs y mapeo;
- autenticación y autorización;
- JWT;
- manejo de errores;
- validaciones;
- reglas de negocio;
- inventario;
- transacciones;
- concurrencia;
- testing unitario;
- testing de controllers;
- testing de integración;
- cálculos comerciales;
- frontend conectado a una API REST.

---

## Estado del proyecto

La aplicación cuenta con backend y frontend funcionales para los flujos definidos dentro del alcance del proyecto.

No se implementó procesamiento real de pagos.

Posibles extensiones futuras pueden desarrollarse fuera del alcance actual sin ser necesarias para el funcionamiento principal de la aplicación.
