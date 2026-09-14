# ms-credit

Microservicio de gestión de créditos del sistema bancario. Aplica las reglas de cardinalidad por tipo de cliente, expone CRUD completo, consume el evento `bank.customer.created` para validar clientes sin REST entre microservicios, y publica `bank.credit.granted` al otorgar un crédito.

## Reglas de negocio (Fase I)

| Tipo de crédito | Regla |
|-----------------|-------|
| PERSONAL | Solo clientes personales, máx. 1 por cliente |
| BUSINESS | Solo clientes empresariales, N por cliente |

Un crédito es independiente de que el cliente tenga cuentas bancarias. El otorgamiento valida el tipo de cliente contra la vista local `customer_view` (alimentada por Kafka), no contra ms-customer.

## Requisitos

- Java 17
- Maven 3.9+
- MongoDB (puerto 27017)
- Kafka (puerto 9092)
- Config Server corriendo en puerto 8888

## Variables de entorno

El servicio obtiene la configuración desde Config Server. Las variables críticas en `application.yml` local:

| Variable | Valor por defecto | Descripción |
|----------|-------------------|-------------|
| `server.port` | `8083` | Puerto del servicio |
| `spring.config.import` | `optional:configserver:http://localhost:8888` | URL del Config Server |
| `spring.data.mongodb.uri` | `mongodb://...credit_db` | URI de MongoDB (fallback si Config Server no está disponible) |
| `spring.kafka.consumer.group-id` | `ms-credit` | Grupo consumidor de Kafka |

## Levantar

```bash
mvn spring-boot:run
```

O generar el JAR:

```bash
mvn clean package -DskipTests
java -jar target/ms-credit-1.0.0.jar
```

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/credits` | Otorgar crédito |
| GET | `/credits/{id}` | Buscar por ID |
| GET | `/credits` | Listar todos |
| PUT | `/credits/{id}` | Actualizar tasa/plazo |
| DELETE | `/credits/{id}` | Eliminar |

## Base de datos

- **Database:** `credit_db`
- **Colecciones:** `credit`, `customer_view` (vista de lectura local)

## Eventos

| Topic | Dirección | Trigger | Payload |
|-------|-----------|---------|---------|
| `bank.customer.created` | Consume | ms-customer crea un cliente | `customerId`, `customerType`, `profile`, `documentNumber`, `occurredAt` |
| `bank.credit.granted` | Produce | Al otorgar un crédito | `creditId`, `customerId`, `creditType`, `amount`, `occurredAt` |

## Verificar

```bash
mvn verify
```

Ejecuta tests unitarios, Checkstyle y genera reporte JaCoCo.
