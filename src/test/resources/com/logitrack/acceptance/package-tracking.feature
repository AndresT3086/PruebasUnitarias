Feature: Package Tracking

  Background:
    * url 'http://localhost:8080'
    * def loginResult = call read('classpath:com/logitrack/acceptance/helpers/login.feature')
    * def authHeader = 'Bearer ' + loginResult.token
    * configure headers = { 'Authorization': '#(authHeader)', 'Content-Type': 'application/json' }


  Scenario: Should get package by id
    Given path 'api','v1','packages'
    And request
      """
      {
        "recipientName": "Carlos Test",
        "recipientEmail": "carlos@udea.edu.co",
        "recipientPhone": "+573002222222",
        "street": "Carrera 50",
        "city": "Cali",
        "state": "VAL",
        "country": "COL",
        "postalCode": "760001",
        "height": 10.0,
        "width": 10.0,
        "depth": 10.0,
        "weight": 1.0
      }
      """
    When method POST
    Then status 201
    * def pkgId = response.data.id

    Given path 'api','v1','packages', pkgId
    When method GET
    Then status 200
    And match response.data.id == pkgId
    And match response.data.status == 'CREATED'

  Scenario: Should return 404 for non-existent package
    Given path 'api','v1','packages','LT-INEXISTENTE-999'
    When method GET
    Then status 404