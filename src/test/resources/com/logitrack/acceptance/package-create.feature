Feature: Package Creation API

  Background:
    * url 'http://localhost:8080'
    * header Content-Type = 'application/json'
    * def loginResult = call read('classpath:com/logitrack/acceptance/helpers/login.feature')
    * header Authorization = 'Bearer ' + loginResult.token
    * configure logPrettyRequest = true
    * configure logPrettyResponse = true


  Scenario: Should create a package with valid data
    Given path 'api','v1','packages'
    And request
      """
      {
        "recipientName": "Darwin T",
        "recipientEmail": "darwin.tangarife@udea.edu.co",
        "recipientPhone": "+1234567890",
        "street": "Av. Vegas",
        "city": "Sabaneta",
        "state": "Ant",
        "country": "Col",
        "postalCode": "10001",
        "height": 20.0,
        "width": 15.0,
        "depth": 10.0,
        "weight": 2.5
      }
      """
    When method POST
    Then status 201
    And match response.id == '#string'
    And match response.status == 'CREATED'
    And match response.recipient.name == 'Darwin T'

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

  Scenario: Should reject package with negative weight
    Given path 'api','v1','packages'
    And request
      """
      {
        "recipientName": "Darwin T",
        "recipientEmail": "darwin@udea.edu.co",
        "recipientPhone": "+573001234567",
        "street": "Av. Vegas",
        "city": "Sabaneta",
        "country": "COL",
        "postalCode": "1046520",
        "height": 20.0,
        "width": 15.0,
        "depth": 10.0,
        "weight": -1.0
      }
      """
    When method POST
    Then status 400
    And match response.error.description contains 'Weight must be positive'