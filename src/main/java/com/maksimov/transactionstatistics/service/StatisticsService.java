package com.maksimov.transactionstatistics.service;

import com.maksimov.transactionstatistics.exception.NoContentException;
import com.maksimov.transactionstatistics.model.Statistics;
import com.maksimov.transactionstatistics.model.Transaction;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class StatisticsService {
    private final ReentrantLock lock = new ReentrantLock();
    private Queue<Transaction> transactionQueue = new PriorityBlockingQueue<>(100, Comparator.comparing(Transaction::getTimestamp));
    private volatile Statistics currentStatistics = new Statistics();
    private boolean keepWatcherRunning = true;
    private long MINUTE_IN_MILLISECS = 60 * 1000;

    public StatisticsService() {
        initStatisticsWatcher();
    }

    private void initStatisticsWatcher() {
        Runnable runnable = () -> {
            try {
                while (keepWatcherRunning) {
                    Thread.sleep(1000);
                    Transaction oldestTransaction = transactionQueue.peek();
                    BigDecimal timestamp = null;

                    if (oldestTransaction != null) {
                        timestamp = oldestTransaction.getTimestamp();

                        if (timestamp.compareTo(BigDecimal.valueOf(System.currentTimeMillis() - MINUTE_IN_MILLISECS)) < 0) {
                            changeTransactionList(MethodName.SUBTRACTION, null);
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread t = new Thread(runnable);
        t.start();
    }

//    public void turnOffStatisticsWatcher(){
//        keepWatcherRunning = false;
//    }

    public Statistics getCurrentStatistics() {
        return currentStatistics.clone();
    }

    public HttpStatus saveTransaction(@NotNull Transaction transaction) throws NoContentException {
        long currentTimeMillis = System.currentTimeMillis();
        long minuteAgoTimeMillis = currentTimeMillis - MINUTE_IN_MILLISECS;
        double transactionTimeMillis = transaction.getTimestamp().longValueExact();

        if (transactionTimeMillis - minuteAgoTimeMillis >= 0 && transactionTimeMillis - currentTimeMillis <= 0) {
            changeTransactionList(MethodName.ADDITION, transaction);
            return HttpStatus.CREATED;
        } else {
//            System.out.println("why NO_CONTENT ???");
//            System.out.println(transaction);
//            System.out.println(currentTimeMillis);
//            System.out.println(minuteAgoTimeMillis);

            throw new NoContentException();
        }
    }

    private void changeTransactionList(MethodName methodName, @Nullable Transaction transaction) {
        final ReentrantLock lock = this.lock;
        lock.lock();

        try {
            switch (methodName) {
                case ADDITION:
                    if (transaction != null) {
                        addNewTransaction(transaction);
                    }

                    break;

                case SUBTRACTION:
                    removeOldTransactions();

                    break;
            }
        } finally {
            lock.unlock();
        }
    }

    private void addNewTransaction(Transaction transaction) {
        Statistics newStatistics = new Statistics();

        BigDecimal amount = transaction.getAmount();

        newStatistics.setSum(currentStatistics.getSum().add(amount));
        newStatistics.setCount(currentStatistics.getCount().add(BigDecimal.ONE));
        newStatistics.setAvg(newStatistics.getSum().divide(newStatistics.getCount(), 2, RoundingMode.HALF_UP));
        if (transactionQueue.isEmpty()) {
            newStatistics.setMin(amount);
            newStatistics.setMax(amount);
        } else {
            newStatistics.setMin(currentStatistics.getMin().min(amount));
            newStatistics.setMax(currentStatistics.getMax().max(amount));
        }

        transactionQueue.add(transaction);
        currentStatistics = null;
        currentStatistics = newStatistics;

//        System.out.println(Arrays.toString(transactionQueue.toArray()));
//        System.out.println(currentStatistics);
    }

    private void removeOldTransactions() {
//        System.out.println("SUBTRACTION");

        long minuteAgoTimeMillis = System.currentTimeMillis() - MINUTE_IN_MILLISECS;

        boolean isRemoved = transactionQueue.removeIf((Transaction transaction) -> {
            return transaction.getTimestamp().compareTo(BigDecimal.valueOf(minuteAgoTimeMillis)) < 0;
        });

        if (isRemoved) {
            currentStatistics = new Statistics();
            Transaction oldestTransaction = transactionQueue.peek();
            if (oldestTransaction != null && oldestTransaction.getAmount() != null) {
                BigDecimal amount = transactionQueue.peek().getAmount();
                currentStatistics.setMin(amount);
                currentStatistics.setMin(amount);
            }

            transactionQueue.forEach(transaction -> {
                BigDecimal amount = transaction.getAmount();

                currentStatistics.setSum(currentStatistics.getSum().add(amount));
                currentStatistics.setCount(currentStatistics.getCount().add(BigDecimal.ONE));
                currentStatistics.setMin(currentStatistics.getMin().min(amount));
                currentStatistics.setMax(currentStatistics.getMax().max(amount));
                currentStatistics.setAvg(currentStatistics.getSum().divide(currentStatistics.getCount(), 2, RoundingMode.HALF_UP));
            });
        }

//        System.out.println(currentStatistics);
    }

//    public HttpStatus save2Transaction(Transaction transaction) {
//        changeTransactionList(MethodName.SUBTRACTION, transaction);
//        return HttpStatus.CREATED;
//    }
//
//    public HttpStatus save3Transaction(Transaction transaction) {
//        System.out.println(Arrays.toString(transactionQueue.toArray()));
//        System.out.println(currentStatistics);
//        return HttpStatus.CREATED;
//    }

    private enum MethodName {
        ADDITION,
        SUBTRACTION
    }
}
