# ms-securitytoken-facade

Microservicio Spring WebFlux que replica la capa de **descifrado + reenvío a SSO**
del documento "Diseño de servicio SecurityToken",
usando el mismo esquema AES-256-CBC + PBKDF2/salt de 32 caracteres, con
interoperabilidad hacia el modo legacy sin salt.

## Qué hace (pasos 1-7 del documento)

1. Recibe `POST /auth/realms/{realm}/protocol/openid-connect/token` con `client_id`,
   `client_secret`, `grant_type`, `scope` cifrados y `channel` en claro (mismo shape
   que el SSO real, para no romper el contrato del consumidor).
2. Valida que los 5 parámetros vengan en el request.
3. Resuelve la llave/IV configurados para ese `channel`.
4. Descifra `client_id`, `client_secret`, `grant_type`, `scope`.
5. Llama al SSO real (Keycloak) con las credenciales ya en claro.
6. Recibe la respuesta del SSO (token o error).
7. Devuelve esa respuesta al cliente tal cual (passthrough de status + body).

## Esquema de cifrado implementado

- **Cypher:** AES-256, **Mode:** CBC, **Encoding:** base64 (igual que el documento)
- **Modo "con salt"** (nuevo): `payload = saltHex(32 chars) + base64(cipherText)`,
  la clave real se deriva con `PBKDF2WithHmacSHA1(llave_canal, salt, 10000 iter, 256 bits)`.
- **Modo legacy "sin salt"**: `payload = base64(cipherText)`, la llave del canal
  (32 caracteres) se usa directamente como clave AES-256.
- El servicio **detecta automáticamente** cuál modo aplica: intenta "con salt" primero
  (si los primeros 32 caracteres son hex válido) y cae a legacy si falla.

La lógica fue validada por fuera del proyecto (Python, mismo esquema PBKDF2-HMAC-SHA1
+ AES-256-CBC) antes de entregarla — ver detalle en la conversación. No se pudo
compilar el módulo Spring completo en este entorno por falta de acceso a Maven
Central; **compílalo y corre `./gradlew test` en tu máquina antes de desplegar.**

## Levantar

```bash
./gradlew bootRun
```

Por defecto corre en `:8081` y apunta a `http://localhost:8080/realms/api-ext-dev/protocol/openid-connect/token`
— es decir, al Keycloak del `docker-compose.yml` que armamos antes (`realm api-ext-dev`,
cliente `1165863`). Levanta ese compose primero:

```bash
# En la carpeta del compose de Keycloak
docker compose up -d
```

## Probar (payload real, canal 8, contra el Keycloak anterior)

Estos valores ya están cifrados con la llave/IV por defecto del `application.yml`
(canal 8) y con el `client_secret` real del cliente `1165863` de Keycloak — el
`curl` funciona tal cual, sin que tengas que cifrar nada a mano:

```bash
curl -X 'POST' \
  'http://localhost:8080/auth/realms/api-ext-dev/protocol/openid-connect/token' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=189cc7...&client_secret=189cc7f8143...&grant_type=189cc7f814338...&scope=189cc7f81...&salt=189cc70&channel=8' | jq .
```

Deberías recibir el mismo shape de respuesta que el ejemplo del documento
(`access_token`, `expires_in`, `refresh_token`, `token_type`, etc.) porque el
facade descifra y reenvía tal cual al Keycloak real.

### Generar tus propios payloads de prueba

`AesCbcDecryptor` incluye `encryptWithSaltForTesting(plainText, llave, iv)` — un
helper de solo-pruebas (no se usa en el flujo de producción) para no depender de
un script externo. Puedes invocarlo desde un test o un `main` temporal:

```java
String cifrado = AesCbcDecryptor.encryptWithSaltForTesting("1165863", "CHANGEME-32-CHARS-LLAVE-CANAL8!!", "CHANGEME16CHARIV");
```

## Configuración (Configmap/Secret del documento)

Ver `src/main/resources/application.yml`. En K8s real:
- `security-token.sso.url` / `timeout-ms` → **Configmap** (no sensible)
- `security-token.channels.*.llave` / `.iv` → **Secret** (nunca en texto plano
  como está aquí, que es solo para desarrollo local)

Variables de entorno equivalentes: `SSO_URL`, `SSO_TIMEOUT_MS`, `CHANNEL_8_KEY`,
`CHANNEL_8_IV`, `CHANNEL_9_KEY`, `CHANNEL_9_IV`, `LOG_LEVEL`.

## Códigos de respuesta (misma tabla del documento)

| Código | Caso                                                                                      |
|--------|-------------------------------------------------------------------------------------------|
| 200    | Respuesta correcta (passthrough del SSO)                                                  |
| 400    | Parámetros faltantes, canal no configurado, o fallo de descifrado (`INVALID_CREDENTIALS`) |
| 500    | Error técnico interno                                                                     |

## Estructura (hexagonal, mismo patrón usado en el resto de la conversación)

```
domain/             → modelos, excepciones, servicios de dominio (reglas de negocio puras)
application/        → puertos (in/out) + use case que orquesta el flujo
helpers/            → implementación técnica de descifrado (adapter que implementa puerto)
adapter-in-rest/    → router/handler/DTOs de entrada HTTP
adapter-out-client/ → lectura de config + cliente WebClient hacia SSO
bootstrap/          → composition root (wiring manual de beans Spring)
```
