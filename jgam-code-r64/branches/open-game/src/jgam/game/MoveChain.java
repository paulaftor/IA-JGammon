package jgam.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class MoveChain implements Iterable<SingleMove> {
    
    public static final MoveChain EMPTY = new MoveChain();
    static {
        EMPTY.singleMoves = Collections.emptyList();
    }
    
    private List<SingleMove> singleMoves = new ArrayList<SingleMove>();

    public MoveChain() {
    }

    public MoveChain(SingleMove m) {
        singleMoves.add(m);
    }

    public MoveChain(MoveChain chain, SingleMove singleMove) {
        singleMoves.addAll(chain.singleMoves);
        singleMoves.add(singleMove);
        sort();
    }

    public MoveChain(MoveChain moveChain) {
        singleMoves.addAll(moveChain.singleMoves);
    }

    private void sort() {
        Collections.sort(singleMoves);
    }

    @Override
    public Iterator<SingleMove> iterator() {
        return singleMoves.iterator();
    }

    public int size() {
        return singleMoves.size();
    }

    public boolean isEmpty() {
        return singleMoves.isEmpty();
    }

    public boolean contains(Object o) {
        return singleMoves.contains(o);
    }

    public Object[] toArray() {
        return singleMoves.toArray();
    }

    public boolean add(SingleMove e) {
        boolean result = singleMoves.add(e);
        sort();
        return result;
    }

    public void clear() {
        singleMoves.clear();
    }

    public boolean equals(Object o) {
        if (o instanceof MoveChain) {
            MoveChain chain = (MoveChain) o;
            return singleMoves.equals(chain.singleMoves);
        }
        return singleMoves.equals(o);
    }

    public int hashCode() {
        return singleMoves.hashCode();
    }

    public SingleMove get(int index) {
        return singleMoves.get(index);
    }

    public int indexOf(Object o) {
        return singleMoves.indexOf(o);
    }

    public int lastTo() {
        if(isEmpty())
            throw new IllegalStateException();
        return get(size()-1).to();
    }
    
    public Set<MoveChain> findConsecutiveChainsFrom(int from) {
        Set<MoveChain> res = new HashSet<MoveChain>();
        MoveChain chain = new MoveChain();
        for (SingleMove sm : singleMoves) {
            if(sm.from() == from) {
                chain.add(sm);
                res.add(new MoveChain(chain));
                from = sm.to();
            }
        }
        return res;
    }

    public boolean isConsecutive() {
        int lastTo = -1;
        for (SingleMove sm : singleMoves) {
            if(lastTo != -1 && lastTo == sm.from()) {
                return false;
            }
            lastTo = sm.to();
        }
        return true;
    }

    public int player() {
        return get(0).player();
    }

    public String toString() {
        return singleMoves.toString();
    }
    
}
