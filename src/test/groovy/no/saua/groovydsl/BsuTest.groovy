package no.saua.groovydsl


import org.junit.Test

import java.time.LocalDate

class BsuTest {

    @SuppressWarnings("GrEqualsBetweenInconvertibleTypes")
    @Test
    void "Verifiser tilgjengelig innskudd"() {
        testBsu {
            år 1
            dag 1 innskudd 200
            dag 3 innskudd 50
            rente 3

            år 2
            dag 1 innskudd 30
            rente 3.5

            år 3
            dag 1 innskudd 35
            dag 2 uttak 15

            verifiser {
                innskudd(år: 1) == 250
                innskudd(år: 2) == 287.50
                innskudd(år: 3) == 317.56

                tilgjengeligInnskudd(år: 1) == 250
                tilgjengeligInnskudd(år: 2) == 30
                tilgjengeligInnskudd(år: 3) == 20
            }
        }
    }























    @SuppressWarnings("GrEqualsBetweenInconvertibleTypes")
    @Test
    void "Verifiser tilgjengelig innskudd 2"() {
        testBsu({
            år(1)
            dag(1).innskudd(200)
            dag(3).innskudd(50)
            rente(3)

            år(2)
            dag(1).innskudd(30)
            rente(3.5)

            år(3)
            dag(1).innskudd(35)
            dag(2).uttak(15)

            verifiser({
                innskudd(år: 1) == 250
                innskudd(år: 2) == 287.50
                innskudd(år: 3) == 317.56

                tilgjengeligInnskudd(år: 1) == 250
                tilgjengeligInnskudd(år: 2) == 30
                tilgjengeligInnskudd(år: 3) == 20
            })
        })
    }

    private static void testBsu(@DelegatesTo(BsuBuilderAndTester) Closure closure) {
        def builder = new BsuBuilderAndTester()
        closure.setDelegate(builder)
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure()
    }

}
