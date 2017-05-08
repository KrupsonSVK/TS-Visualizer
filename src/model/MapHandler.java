package model;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;


public abstract class MapHandler {


    public static <K, V> HashMap<V, K> reverse(Map<K, V> map) {
        HashMap<V, K> rev = new HashMap<V, K>();

        for (Map.Entry<K, V> entry : map.entrySet()) {
            rev.put(entry.getValue(), entry.getKey());
        }
        return rev;
    }


    public static <K, V extends Comparable<? super V>> Map<K, V> sortHashMapByKey(Map<K, V> map) {

        SortedSet<V> keys = new TreeSet<V>(
                (Collection<? extends V>) map.keySet()
        );

        Map<K, V> result = new LinkedHashMap<>();

        for (V key : keys) {
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


    public static List<Integer> sortMapToListByKey(Map<Integer, Integer> map) {

        List<Integer> unsorted = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            unsorted.add(entry.getKey());
        }
        Collections.sort(unsorted);

        return unsorted;
    }


    public static List<BigInteger> sortMapToListByValue(Map<BigInteger, BigInteger> map) {

        List<BigInteger> unsorted = new ArrayList<>();

        for (Map.Entry<BigInteger, BigInteger> entry : map.entrySet()) {
            unsorted.add(entry.getValue());
        }
        Collections.sort(unsorted);

        return unsorted;
    }


    public static <K, V> Map.Entry<K, V> getFirstItem(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            return entry;
        }
        return null;
    }


    public static <K, V> Map.Entry<K, V> getLastItem(Map<K, V> map) {
        Map.Entry<K, V> last = null;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            last = entry;
        }
        return last;
    }


    public static<K,V> Map.Entry<K, V> getIndexFromMap(Map<K,V> map, int index) {
        int i = 0;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (i == index) {
                return entry;
            }
            i++;
        }
        return null;
    }


    public static <K, V> K getByValue(Map<K, V> map, V value) {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().equals(value))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public static Map updateMap(Map<Integer, Long> map, Integer key, Long value) {
        Long currentValue = map.get(key);
        if(currentValue != null){
            value += currentValue;
        }
        map.put(key,value);
        return map;
    }

    public static Map updateMap(Map<Integer, Integer> map, Integer key, Integer value) {
        Integer currentValue = map.get(key);
        if(currentValue != null){
            value += currentValue;
        }
        map.put(key,value);
        return map;
    }
}
