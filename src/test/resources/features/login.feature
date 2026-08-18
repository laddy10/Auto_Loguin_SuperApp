Feature: Ingreso a la Super App

  @SA001
    Scenario: Login exitoso con correo electrónico
    Given EL USUARIO ABRE LA SUPER APP
    When  REALIZA EL INGRESO
    Then  VERIFICA VERSION DE LA SUPER APP

  @SA002
  Scenario: Login exitoso con cédula
    Given EL USUARIO ABRE LA SUPER APP
    When  REALIZA EL INGRESO CON CEDULA
    Then  VERIFICA VERSION DE LA SUPER APP


    ################################################################################################

  #Login

  # Tags individuales: mismo flujo que @LOGIN_COMPLETO pero uno por escenario.
  # Cada uno deja la sesión cerrada al terminar, para que el siguiente arranque limpio.
  # El nombre del escenario coincide con la fila de la tabla de resultados.

  @LOGIN_CAMBIO_CONTRASENA_01
  Scenario: Cambio de Contraseña
    Given EL USUARIO ABRE LA SUPER APP
    When  REALIZA EL CAMBIO DE CONTRASENA


  @LOGIN_PIN_01
  Scenario: Login por PIN
    Given EL USUARIO ABRE LA SUPER APP
    When  REALIZA EL INGRESO CON PIN


  @LOGIN_CORREO_01
  Scenario: Login Por Correo y Contraseña
    Given EL USUARIO ABRE LA SUPER APP
    When  REALIZA EL INGRESO CON CORREO


  @LOGIN_DOCUMENTO_01
  Scenario: Login por Documento
    Given EL USUARIO ABRE LA SUPER APP
    When  REALIZA EL INGRESO CON DOCUMENTO


  @LOGIN_COMPLETO
  Scenario: Monitoreo de inicios de sesión Mi Claro
    Given EL USUARIO ABRE LA SUPER APP
    Then  REALIZA TODOS LOS INGRESOS