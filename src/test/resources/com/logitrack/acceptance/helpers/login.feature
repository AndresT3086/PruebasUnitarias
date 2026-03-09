Feature: Login Helper

Scenario: Get admin token
    Given url 'http://localhost:8080'
    And path 'api','v1','auth','login'
    And header Content-Type = 'application/json'
    And request
    """
    {
      "email": "admin@logitrack.com",
      "password": "admin123"
    }
    """
    When method post
    Then status 200
    * print response
    * def token = response.token