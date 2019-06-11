package no.saua.groovydsl

import java.time.LocalDate

public class BsuBuilderAndTester {

    Map<Integer, BigDecimal> interestsForYear = new HashMap<>()
    List<Bsu.BsuTransaction> transactions = new ArrayList<>()

    TransactionBuilder currentTransBuilder
    int currentYear = 1

    /**
     * Sett året som blir i neste transaksjon/rente
     */
    public void år(int new_year) {
        buildAndAddCurrentTransaction()
        currentYear = new_year;
    }

    public void rente(int number) {
        rente(number as BigDecimal)
    }

    public void rente(BigDecimal bd) {
        interestsForYear.put(currentYear, bd.divide(BigDecimal.valueOf(100)))
    }

    public TransactionBuilder dag(int day) {
        buildAndAddCurrentTransaction()
        currentTransBuilder = new TransactionBuilder(currentYear, day)
        return currentTransBuilder
    }

    public void buildAndAddCurrentTransaction() {
        if (currentTransBuilder != null) {
            transactions.add(currentTransBuilder.build())
            currentTransBuilder = null
        }
    }

    public void verifiser(@DelegatesTo(BsuTester) Closure verifyClosure) {
        buildAndAddCurrentTransaction()

        // Lag domeneobjekt
        def bsu = new Bsu(transactions, interestsForYear)

        // Kjør verifiserings-closuren
        verifyClosure.delegate = new BsuTester(bsu)
        verifyClosure.resolveStrategy = Closure.DELEGATE_FIRST
        verifyClosure()
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

