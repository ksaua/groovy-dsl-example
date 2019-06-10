package no.saua.groovydsl


import org.junit.Test

import java.time.LocalDate

class BsuTest {

    @SuppressWarnings("GrEqualsBetweenInconvertibleTypes")
    @Test
    void "test"() {
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
    void "test2"() {
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

    public static class BsuBuilderAndTester {

        Map<Integer, BigDecimal> interests = new HashMap<>()
        List<Bsu.BsuTransaction> transactions = new ArrayList<>()

        TransactionBuilder currentTransBuilder
        int currentYear = 1

        private void år(int new_year) {
            buildAndAddCurrentTrans()
            currentYear = new_year;
        }

        private void rente(int number) {
            rente(number as BigDecimal)
        }

        private void rente(BigDecimal bd) {
            interests.put(currentYear, bd.divide(BigDecimal.valueOf(100)))
        }

        private TransactionBuilder dag(int day) {
            buildAndAddCurrentTrans()
            currentTransBuilder = new TransactionBuilder(currentYear, day)
            return currentTransBuilder
        }

        public void buildAndAddCurrentTrans() {
            if (currentTransBuilder != null) {
                transactions.add(currentTransBuilder.build())
                currentTransBuilder = null
            }
        }

        public void verifiser(@DelegatesTo(BsuTester) Closure closure) {
            buildAndAddCurrentTrans()
            def bsu = new Bsu(transactions, interests)
            closure.delegate = new BsuTester(bsu)
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure()
        }

        public class TransactionBuilder {
            private int year
            private int day
            private int amount

            TransactionBuilder(int year, int day) {
                this.day = day
                this.year = year
            }

            TransactionBuilder innskudd(int amount) {
                this.amount = amount
                return this
            }

            TransactionBuilder uttak(int amount) {
                this.amount = -amount
                return this
            }

            public Bsu.BsuTransaction build() {
                return new Bsu.BsuTransaction(LocalDate.of(year, 1, 1).withDayOfYear(day), new BigDecimal(amount))
            }
        }
    }

    static class BsuTester {
        private Bsu bsu

        BsuTester(Bsu bsu) {
            this.bsu = bsu
        }

        public BigDecimalWrapper innskudd(Map parameters) {
            return new BigDecimalWrapper(bsu.getInnskudd(parameters["år"] as Integer))
        }


        BigDecimalWrapper tilgjengeligInnskudd(Map parameters) {
            return new BigDecimalWrapper(bsu.getTilgjengeligInnskudd(parameters["år"] as Integer))
        }
    }

    static class BigDecimalWrapper {
        private BigDecimal value

        BigDecimalWrapper(BigDecimal i) {
            this.value = i
        }

        public boolean equals(Object other) {
            if (other instanceof Integer) {
                assert value == new BigDecimal(other)
            }
            assert value == other
        }
    }
}
