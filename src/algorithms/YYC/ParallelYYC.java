package algorithms.YYC;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import algorithms.DataStructure.BasicMatrix;

class YYCRunner implements Runnable {

    BasicMatrix BM;
    ArrayList<BitSet> tt_set;

    public int candidates;
    public int num_tt;

    public YYCRunner(BasicMatrix someBM, BitSet candidate) {
        BM = someBM;
        tt_set = new ArrayList<BitSet>();
        tt_set.add(candidate);
        candidates = 0;
    }

    public ArrayList<BitSet> getTT() {
        return tt_set;
    }

    private boolean find_compatible_sets(BitSet candidate, int feat_pos, int row_index, BasicMatrix MB) {
        BitSet possible_tt = (BitSet) candidate.clone();
        possible_tt.set(feat_pos);
        int unitary_count = 0;
        BitSet condition_validator = new BitSet(MB.getNumFeat());
        for (int row_pos = 0; row_pos < row_index; row_pos++) {
            BitSet evaluator = (BitSet) possible_tt.clone();
            evaluator.and(MB.getRowBits(row_pos));

            if (1 == evaluator.cardinality()) {
                unitary_count += 1;
                condition_validator.or(evaluator);
            }
        }
        // condition 1
        if (unitary_count < possible_tt.cardinality())
            return false;
        // condition 2
        if (!condition_validator.equals(possible_tt))
            return false;

        return true;
    }

    @Override
    public void run() {
        int row_pos = 1;
        while (row_pos < BM.getNumRows()) {
            BitSet current_row_bits = BM.getRowBits(row_pos);
            ArrayList<BitSet> tt_aux = new ArrayList<BitSet>();

            for (int tt_pos = 0; tt_pos < tt_set.size(); tt_pos++) {
                BitSet candidate = tt_set.get(tt_pos);
                if (candidate.intersects(current_row_bits)) {
                    tt_aux.add(candidate);
                } else {
                    for (int feat_pos = 0; feat_pos < BM.getNumFeat(); feat_pos++) {
                        if (current_row_bits.get(feat_pos) && !candidate.get(feat_pos)) {
                            candidates++;
                            if (find_compatible_sets(candidate, feat_pos, row_pos + 1, BM)) {
                                BitSet new_tt = (BitSet) candidate.clone();
                                new_tt.set(feat_pos);
                                tt_aux.add(new_tt);
                            }
                        }
                    }
                }
            }
            tt_set = tt_aux;
            row_pos += 1;
        }
        num_tt = tt_set.size();
    }
}

public class ParallelYYC {
    public static String FindTT(ArrayList<ArrayList<Integer>> matrixByRows) {
        BasicMatrix mb_simple = new BasicMatrix();
        mb_simple.load(matrixByRows);
        mb_simple.hamming_sort();

        ParallelYYC yycAlgorithm = new ParallelYYC(mb_simple);

        long startTime = System.currentTimeMillis();
        ArrayList<BitSet> tt_list = yycAlgorithm.search_tt();
        long endTime = System.currentTimeMillis();

        int num_tt = tt_list.size();
        String result = "{'numTT':" + Long.toString(num_tt)
                + ", 'ms_time':" + Long.toString(endTime - startTime)
                + ", 'hits':" + Long.toString(yycAlgorithm.candidates) + "}";
        return result;
    }

    public int candidates;
    public int num_tt;
    BasicMatrix BM;

    public ParallelYYC(BasicMatrix someBM) {
        BM = someBM;
        num_tt = 0;
        candidates=0;
    }

    private ArrayList<BitSet> search_tt() {
        candidates = 0;
        ArrayList<BitSet> tt_set = new ArrayList<BitSet>();
        if (BM.getNumRows() == 0)
            return tt_set;

        HashMap<Thread, YYCRunner> searchers = new HashMap<Thread, YYCRunner>();

        // process the first row
        BitSet first_row = BM.getRowBits(0);
        for (int feat_pos = 0; feat_pos < BM.getNumFeat(); feat_pos++) {
            if (first_row.get(feat_pos)) {
                BitSet new_testor = new BitSet(BM.getNumFeat());
                new_testor.set(feat_pos);
                YYCRunner subset = new YYCRunner(BM, new_testor);
                Thread newT = new Thread(subset);
                newT.start();
                searchers.put(newT, subset);
            }
        }

        for (HashMap.Entry<Thread, YYCRunner> entry : searchers.entrySet()) {
            Thread thread = entry.getKey();
            YYCRunner subset = entry.getValue();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tt_set.addAll(subset.getTT());
            candidates += subset.candidates;
        }
        return tt_set;
    }
}
