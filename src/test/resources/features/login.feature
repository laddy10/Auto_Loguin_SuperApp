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

  @LOGIN_CORREO_01  @LOGIN
  Scenario: Login exitoso con correo electrónico
    Given EL USUARIO ABRE LA SUPER APP
    When  REALIZA EL INGRESO CON CORREO


  @LOGIN_DOCUMENTO_01  @LOGIN
  Scenario: Login exitoso con documento
    Given EL USUARIO ABRE LA SUPER APP
    When  REALIZA EL INGRESO CON DOCUMENTO


  @LOGIN_PIN_01  @LOGIN
  Scenario: Login exitoso con pin
    Given EL USUARIO ABRE LA SUPER APP
    When  REALIZA EL INGRESO CON PIN