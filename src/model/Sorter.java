package model;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;


public class Sorter {


    public <K, V> HashMap<V, K> reverse(Map<K, V> map) {
        HashMap<V, K> rev = new HashMap<V, K>();

        for (Map.Entry<K, V> entry : map.entrySet()) {
            rev.put(entry.getValue(), entry.getKey());
        }
        return rev;
    }


    public <K, V extends Comparable<? super V>> Map<K, V> sortHashMapByKey(Map<K, V> map) {

        SortedSet<Integer> keys = new TreeSet<Integer>(
                (Collection<? extends Integer>) map.keySet()
        );

        Map<K, V> result = new LinkedHashMap<>();

        for (Integer key : keys) {
            result.put((K) key, map.get(key));
        }
        return result;
    }


    public static <K, V extends Comparable<? super V>> Map<K, V> sortHashMapByValue(Map<K, V> map) {

        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(/*Collections.reverseOrder()*/))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                        )
                );
    }


    public List<Integer> sortMapToListByKey(Map<Integer, Integer> map) {

        List<Integer> unsorted = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            unsorted.add(entry.getKey());
        }
        Collections.sort(unsorted);

        return unsorted;
    }

    public List<BigInteger> sortMapToListByValue(Map<BigInteger, BigInteger> map) {

        List<BigInteger> unsorted = new ArrayList<>();

        for (Map.Entry<BigInteger, BigInteger> entry : map.entrySet()) {
            unsorted.add(entry.getValue());
        }
        Collections.sort(unsorted);

        return unsorted;
    }
}
