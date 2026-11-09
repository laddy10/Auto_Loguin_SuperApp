# language: es
Característica: Ingreso a la Super App

  # Tags individuales: mismo flujo que @LOGIN_COMPLETO pero uno por escenario.
  # Cada uno deja la sesión cerrada al terminar, para que el siguiente arranque limpio.
  # El nombre del escenario coincide con la fila de la tabla de resultados.

  @LOGIN_CAMBIO_CONTRASENA_01
  Escenario: Cambio de Contraseña
    Dado que el usuario abre la app Mi Claro
    Cuando valida el cambio de contraseña


  @LOGIN_PIN_01
  Escenario: Login por PIN
    Dado que el usuario abre la app Mi Claro
    Cuando inicia sesión con PIN


  @LOGIN_CORREO_01
  Escenario: Login Por Correo y Contraseña
    Dado que el usuario abre la app Mi Claro
    Cuando inicia sesión con correo y contraseña


  @LOGIN_DOCUMENTO_01
  Escenario: Login por Documento
    Dado que el usuario abre la app Mi Claro
    Cuando inicia sesión con documento


  # Monitoreo: prueba los 4 inicios de sesión aunque alguno falle y al final arma la
  # tabla de resultados. Si alguno falló, el escenario termina fallido.
  @LOGIN_COMPLETO
  Escenario: Monitoreo de inicios de sesión Mi Claro
    Dado que el usuario abre la app Mi Claro
    Cuando valida el cambio de contraseña
    Y inicia sesión con PIN
    Y inicia sesión con correo y contraseña
    Y inicia sesión con documento
    Entonces todos los inicios de sesión son exitosos
