Feature: Package Delivery Flow

  Background:
    * url 'http://localhost:8080'
    * def loginResult = call read('classpath:com/logitrack/acceptance/helpers/login.feature')
    * def authHeader = 'Bearer ' + loginResult.token
    * configure headers = { 'Authorization': '#(authHeader)', 'Content-Type': 'application/json' }


  Scenario: Full delivery flow CREATED to DELIVERED
    Given path 'api','v1','packages'
    And request
      """
      {
        "recipientName": "Andrea Lopez",
        "recipientEmail": "andrea@udea.edu.co",
        "recipientPhone": "+573009876543",
        "street": "Calle 1",
        "city": "Medellin",
        "state": "ANT",
        "country": "COL",
        "postalCode": "050001",
        "height": 15.0,
        "width": 20.0,
        "depth": 30.0,
        "weight": 1.5
      }
      """
    When method POST
    Then status 201
    * def pkgId = response.data.id

    Given path 'api','v1','packages', pkgId, 'status'
    And param status = 'IN_TRANSIT'
    When method PUT
    Then status 200
    And match response.data.status == 'IN_TRANSIT'

    Given path 'api','v1','packages', pkgId, 'status'
    And param status = 'OUT_FOR_DELIVERY'
    When method PUT
    Then status 200

    Given path 'api','v1','packages', pkgId, 'status'
    And param status = 'DELIVERED'
    When method PUT
    Then status 200
    And match response.data.status == 'DELIVERED'

  Scenario: Should reject invalid transition CREATED to DELIVERED
    Given path 'api','v1','packages'
    And request
      """
      {
        "recipientName": "Yiyi Test",
        "recipientEmail": "yiyi@udea.edu.co",
        "recipientPhone": "+573001111111",
        "street": "Calle X",
        "city": "Bogota",
        "state": "CUN",
        "country": "COL",
        "postalCode": "110001",
        "height": 10.0,
        "width": 10.0,
        "depth": 10.0,
        "weight": 1.0
      }
      """
    When method POST
    Then status 201
    * def pkgId = response.data.id

    Given path 'api','v1','packages', pkgId, 'status'
    And param status = 'DELIVERED'
    When method PUT
    Then status 409