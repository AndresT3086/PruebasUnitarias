package com.logitrack.acceptance;

import com.intuit.karate.junit5.Karate;

class KarateRunner {

    @Karate.Test
    Karate runTests() {
        return Karate.run("classpath:com/logitrack/acceptance");
    }

}
