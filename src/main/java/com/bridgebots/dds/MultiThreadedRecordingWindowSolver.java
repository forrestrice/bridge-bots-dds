package com.bridgebots.dds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Stopwatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MultiThreadedRecordingWindowSolver implements Solver {
    private static final Logger LOG = LogManager.getLogger();
    private final ThreadPoolExecutor threadPoolExecutor;
    private AtomicLong nodesUsed = new AtomicLong();
    private AtomicLong interruptedTasks = new AtomicLong();
    private AtomicLong hitsWithoutTermination = new AtomicLong();
    private final Function<Board, List<Card>> cardSelectionFunction;
    private final TranspositionTable<AlphaBetaResult> transpositionTable = new TranspositionTable<>();

    public MultiThreadedRecordingWindowSolver() {
        this(Board::restrictedNextPlays);
    }

    public MultiThreadedRecordingWindowSolver(Function<Board, List<Card>> cardSelectionFunction) {
        this.cardSelectionFunction = cardSelectionFunction;
        this.threadPoolExecutor = new ThreadPoolExecutor(13, 13, 10_000, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(200),
                new ThreadPoolExecutor.AbortPolicy());
        threadPoolExecutor.prestartAllCoreThreads();
    }

    @Override
    public int solve(Deal deal, TrumpSuit trumpSuit, Direction declarer) {
        nodesUsed.set(0);
        Board board = Board.forDeal(deal, trumpSuit, declarer.next());
        Stopwatch timer = Stopwatch.createStarted();

        int upperBound = board.getTricksAvailable();
        int lowerBound = 0;
        while (lowerBound != upperBound) {
            int target = chooseTarget(lowerBound, upperBound);
            LOG.info("Window solving for lowerBound {}, upperBound {}, target {}", lowerBound, upperBound, target);
            try {
                int windowResult = alphaBetaSolve(board, 0, target - 1, target);
                if (windowResult >= target) {
                    lowerBound = target;
                } else {
                    upperBound = target - 1;
                }
            } catch (InterruptedException e){
                //Should never propagate this far up
                throw new RuntimeException(e);
            }
        }
        LOG.info("Computed window result {} with {} nodes in {}", lowerBound, nodesUsed, timer.stop());
        LOG.info("Interrupted tasks: {}", interruptedTasks);
        LOG.info("TranspositionTable stats: {} entries, {} queries, {} hits, {} hitrate, {} hitsWithoutTermination",
                transpositionTable.keyCount(),
                transpositionTable.getQueryCount(),
                transpositionTable.getHitCount(),
                (float) transpositionTable.getHitCount() / transpositionTable.getQueryCount(),
                hitsWithoutTermination
        );
        return lowerBound;
    }

    private int chooseTarget(int lowerBound, int upperBound) {
        return lowerBound + Math.max(1, (upperBound - lowerBound) / 2);
    }

    private int alphaBetaSolve(Board board, int depth, int alpha, int beta) throws InterruptedException {
        nodesUsed.incrementAndGet();
        if (depth < 3) {
            LOG.debug("depth={}, nodesUsed={}", depth, nodesUsed.get());
        }
        if (board.getCardsRemaining() == 0) {
            return 0;
        }
        if (Thread.interrupted()) {
            LOG.debug("thread interrupted at depth={}",depth);
            interruptedTasks.incrementAndGet();
            throw new InterruptedException();
        }


        //The maximizing player can always achieve at least 0 and can never achieve more than the tricks remaining
        if (beta <= 0 || board.getTricksAvailable() < beta) {
            return 0;
        }

        AlphaBetaResult cachedResult = null;
        if (board.getCurrentTrick().isEmpty()) {
            cachedResult = transpositionTable.get(board);
            if (cachedResult != null) {
                // Upper bound on declarer tricks remaining is alpha
                if (cachedResult.result <= cachedResult.alpha) {
                    if (cachedResult.alpha <= alpha) {
                        return cachedResult.alpha;
                    }

                    //Lower bound on declarer tricks remaining is beta
                } else if (cachedResult.result >= cachedResult.beta) {
                    if (cachedResult.beta >= beta) {
                        return cachedResult.beta;
                    }
                }
            }
        }

        if (cachedResult != null) {
            hitsWithoutTermination.incrementAndGet();
        }

        boolean useThreadpool = depth == 0;
        //threadPoolExecutor.getQueue().isEmpty() && board.getCardsRemaining() > 12;

        int initialAlpha = alpha;
        int initialBeta = beta;
        Map<Card, Integer> cardValues = new HashMap<>();
        int value;
        if (board.offenseOnLead()) {
            if (useThreadpool) {
                List<AlphaBetaCallable> abCallables = cardSelectionFunction.apply(board).stream().map(
                        c -> new AlphaBetaCallable(board, c, initialAlpha, initialBeta, depth)).collect(
                        Collectors.toList());
                List<Future<Integer>> abFutures = abCallables.stream().map(
                        threadPoolExecutor::submit).collect(Collectors.toList());

                value = -1;
                boolean keepRunning = true;
                while(keepRunning){
                    boolean allDone = true;
                    for(Future<Integer> abFuture : abFutures){
                        if(abFuture.isDone()){
                            try {
                                Integer cardResult = abFuture.get();
                                value = Math.max(value, cardResult);
                                alpha = Math.max(alpha, value);
                                if (alpha >= beta) {
                                    keepRunning = false;
                                    break;
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            allDone = false;
                        }
                    }
                    keepRunning = keepRunning && !allDone;
                }
                //Alpha beta cutoff has been hit - can cancel the other jobs to reclaim threads
                for(Future<Integer> abFuture : abFutures){
                    abFuture.cancel(true);
                }
            } else {
                value = -1;
                for (Card card : cardSelectionFunction.apply(board)) {
                    board.makePlay(card);
                    int currentTrickValue = 0;
                    if (board.getCurrentTrick().isEmpty() && board.offenseOnLead()) {
                        currentTrickValue = 1;
                    }
                    int cardResult = currentTrickValue + alphaBetaSolve(board, depth + 1, alpha - currentTrickValue,
                            beta - currentTrickValue);
                    board.undoPlay();
                    cardValues.put(card, cardResult);
                    value = Math.max(value, cardResult);
                    alpha = Math.max(alpha, value);
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
        } else {
            if (useThreadpool) {
                List<AlphaBetaCallable> abCallables = cardSelectionFunction.apply(board).stream().map(
                        c -> new AlphaBetaCallable(board, c, initialAlpha, initialBeta, depth)).collect(
                        Collectors.toList());
                List<Future<Integer>> abFutures = abCallables.stream().map(
                        threadPoolExecutor::submit).collect(Collectors.toList());

                value = 14;
                boolean keepRunning = true;
                while (keepRunning) {
                    boolean allDone = true;
                    for (Future<Integer> abFuture : abFutures) {
                        if (abFuture.isDone()) {
                            try {
                                Integer cardResult = abFuture.get();
                                value = Math.min(value, cardResult);
                                beta = Math.min(beta, value);
                                if (alpha >= beta) {
                                    keepRunning = false;
                                    break;
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            allDone = false;
                        }
                    }
                    keepRunning = keepRunning && !allDone;
                }
                //Alpha beta cutoff has been hit - can cancel the other jobs to reclaim threads
                for (Future<Integer> abFuture : abFutures) {
                    abFuture.cancel(true);
                }
            } else{
                value = 14;
                for (Card card : cardSelectionFunction.apply(board)) {
                    board.makePlay(card);
                    int currentTrickValue = 0;
                    if (board.getCurrentTrick().isEmpty() && board.offenseOnLead()) {
                        currentTrickValue = 1;
                    }
                    int cardResult = currentTrickValue + alphaBetaSolve(board, depth + 1, alpha - currentTrickValue,
                            beta - currentTrickValue);
                    board.undoPlay();
                    value = Math.min(value, cardResult);
                    cardValues.put(card, cardResult);
                    beta = Math.min(beta, value);
                    if (alpha >= beta) {
                        break;
                    }
                }
            }

            //LOG.debug("defense returns value={}, alpha={}, beta={}", value, alpha, beta);
        }
        if (board.getCurrentTrick().isEmpty()) {
            AlphaBetaResult alphaBetaResult = new AlphaBetaResult(cardValues, initialAlpha, initialBeta, value, null);
            transpositionTable.put(board, alphaBetaResult);
        }
        if (depth <= 2){
            LOG.debug("returning value={} from depth={} nodesUsed={}", value, depth, nodesUsed.get());
        }

        return value;
    }

    /*
    private int supplyAlphaBeta(Board board, Card cardToPlay, int alpha, int beta, int depth) {
        Board boardCopy = new Board(board);
        boardCopy.makePlay(cardToPlay);
        int currentTrickValue = 0;
        if (board.getCurrentTrick().isEmpty() && board.offenseOnLead()) {
            currentTrickValue = 1;
        }
        return currentTrickValue + alphaBetaSolve(boardCopy, depth, alpha, beta);
    }*/

    private class AlphaBetaCallable implements Callable<Integer> {

        private final Board board;
        private final Card cardToPlay;
        private final int alpha;
        private final int beta;
        private final int depth;

        public AlphaBetaCallable(Board board, Card cardToPlay, int alpha, int beta, int depth) {
            this.board = new Board(board);
            this.cardToPlay = cardToPlay;
            this.alpha = alpha;
            this.beta = beta;
            this.depth = depth;
        }

        @Override
        public Integer call() throws Exception {
            board.makePlay(cardToPlay);
            int currentTrickValue = 0;
            if (board.getCurrentTrick().isEmpty() && board.offenseOnLead()) {
                currentTrickValue = 1;
            }
            return currentTrickValue + alphaBetaSolve(board, depth + 1, alpha - currentTrickValue, beta - currentTrickValue);
        }
    }


    private static class AlphaBetaResult {
        final Map<Card, Integer> cardValues;
        final int alpha;
        final int beta;
        final int result;
        final Board board;

        AlphaBetaResult(Map<Card, Integer> cardValues, int alpha, int beta, int result, Board board) {
            this.cardValues = cardValues;
            this.alpha = alpha;
            this.beta = beta;
            this.result = result;
            this.board = board;
        }
    }
}
