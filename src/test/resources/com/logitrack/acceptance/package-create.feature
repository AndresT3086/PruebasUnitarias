Feature: Package Creation API

  Background:
    * url 'http://localhost:8080'
    * def loginResult = call read('classpath:com/logitrack/acceptance/helpers/login.feature')
    * def authHeader = 'Bearer ' + loginResult.token
    * configure headers = { 'Authorization': '#(authHeader)', 'Content-Type': 'application/json' }

  Scenario: Should reject package without recipient
    Given path 'api','v1','packages'
    And request
      """
      {
        "street": "Av. Vegas",
        "city": "Sabaneta",
        "country": "COL",
        "postalCode": "1046520",
        "height": 20.0,
        "width": 15.0,
        "depth": 10.0,
        "weight": 2.5
      }
      """
    When method POST
    Then status 400