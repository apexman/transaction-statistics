package com.maksimov.transactionstatistics.service;

import com.maksimov.transactionstatistics.model.Statistics;
import com.maksimov.transactionstatistics.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@Slf4j
public class TransactionStatisticsService {
    private final static long MILLISECONDS_IN_MINUTE = 60 * 1000;

    private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Statistics[] allStatistics = new Statistics[60];
    private int currentIndex = 0;

    public TransactionStatisticsService() {
        for (int i = 0; i < 60; i++) {
            allStatistics[i] = new Statistics();
        }
    }

    /**
     * Проверить, что можем сохранить транзакцию
     */
    public boolean canSave(Transaction transaction) {
        long now = Instant.now().toEpochMilli();
        long transactionTimestamp = transaction.getTimestamp();
        return now >= transactionTimestamp && now - transactionTimestamp <= MILLISECONDS_IN_MINUTE;
    }

    /**
     * Сохранить транзакцию
     */
    public void saveTransaction(Transaction transaction) {
        readWriteLock.writeLock().lock();
        try {
            int index = getTransactionIndex(transaction);
            addTransactionToStatistics(index, transaction);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Вернуть статистику за 60 секунд (с николосом кейджом в главной роли)
     */
    public Statistics getStatistics() {
        readWriteLock.readLock().lock();
        try {
            return calculateStatistics();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Каждую секунду обновляем данные по статистике
     */
    @Scheduled(fixedRate = 1000)
    private void scheduledUpdateStatisticsListIndex() {
        readWriteLock.writeLock().lock();
        try {
            currentIndex = Math.floorMod(currentIndex + 1, 60);
            allStatistics[currentIndex] = new Statistics();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private Statistics calculateStatistics() {
        Statistics result = new Statistics();
        for (Statistics statistics : allStatistics) {
//            TODO: если нет значений, откуда брать min and max
            if (!statistics.isNew()) {
                if (result.isNew()) {
                    result = new Statistics(statistics.getSum(),
                            statistics.getAvg(),
                            statistics.getMax(),
                            statistics.getMin(),
                            statistics.getCount(),
                            false);
                } else {
                    result.setMax(Math.max(result.getMax(), statistics.getMax()));
                    result.setMin(Math.min(result.getMin(), statistics.getMin()));
                    result.setCount(result.getCount() + statistics.getCount());
                    result.setSum(result.getSum() + statistics.getSum());
                    result.setAvg(result.getSum() / result.getCount());
                }

            }
        }
        return result;
    }

    /**
     * time = current - trans.time
     * time -> 0 - 60_000
     * <p>
     * curr_index - 0 = 0 - 999
     * curr_index - 1 = 1000 - 1999
     * ...
     * curr_index - 59 = 59_000 - 60_000
     * _____
     * <p>
     * int(time / 1000) = index_minus
     * index_minus = 59 if index_minus >= 60 else index_minus
     * <p>
     * tran_index = abs(curr_index - index_minus)
     */
    private int getTransactionIndex(Transaction transaction) {
        long time = Instant.now().toEpochMilli() - transaction.getTimestamp();
        int indexSpace = (int) (time / 1000);
        indexSpace = indexSpace >= 60 ? 59 : indexSpace;
        return Math.abs(currentIndex - indexSpace);
    }

    private void addTransactionToStatistics(int index, Transaction transaction) {
        Statistics statisticsForLastSecond = allStatistics[index];
        Statistics newStatistics = new Statistics();
        if (statisticsForLastSecond.isNew()) {
            newStatistics.setMax(transaction.getAmount());
            newStatistics.setMin(transaction.getAmount());
        } else {
            newStatistics.setMax(Math.max(statisticsForLastSecond.getMax(), transaction.getAmount()));
            newStatistics.setMin(Math.min(statisticsForLastSecond.getMin(), transaction.getAmount()));
        }
        newStatistics.setCount(statisticsForLastSecond.getCount() + 1);
        newStatistics.setSum(statisticsForLastSecond.getSum() + transaction.getAmount());
        newStatistics.setAvg(newStatistics.getSum() / newStatistics.getCount());
        newStatistics.setNew(false);
        allStatistics[index] = newStatistics;
    }
}
