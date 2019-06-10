package no.saua.groovydsl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class Bsu {
    private List<BsuTransaction> list;
    private Map<Integer, BigDecimal> interests;

    public Bsu(List<BsuTransaction> list, Map<Integer, BigDecimal> interests) {
        this.list = list;
        this.interests = interests;
    }

    BigDecimal getInnskudd(int year) {
        List<BsuTransaction> transactions = list.stream()
                .filter(trans -> trans.getDate().getYear() <= year)
                .sorted(Comparator.comparing(BsuTransaction::getDate))
                .collect(Collectors.toList());

        int lastYear = transactions.get(0).getDate().getYear();
        BigDecimal sum = BigDecimal.ZERO;
        for (BsuTransaction transaction : transactions) {
            if (transaction.getDate().getYear() != lastYear) {
                sum = sum.add(sum.multiply(getInterest(lastYear)).setScale(2, RoundingMode.HALF_EVEN));
                lastYear = transaction.getDate().getYear();
            }
            sum = sum.add(transaction.getAmount());
        }
        if (lastYear != year) {
            sum = sum.add(sum.multiply(getInterest(lastYear)).setScale(2, RoundingMode.HALF_EVEN));
        }
        return sum;
    }

    private BigDecimal getInterest(int year) {
        return interests.getOrDefault(year, BigDecimal.ZERO);
    }

    public BigDecimal getTilgjengeligInnskudd(int year) {
        return list.stream()
                .filter(trans -> trans.getDate().getYear() == year)
                .map(BsuTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static class BsuTransaction {
        private BigDecimal amount;
        private LocalDate date;

        BsuTransaction(LocalDate date, BigDecimal amount) {
            this.date = date;
            this.amount = amount;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public LocalDate getDate() {
            return date;
        }
    }
}
