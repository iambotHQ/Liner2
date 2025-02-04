package g419.liner2.core.tools;

import java.util.*;

/**
 * A thread-safe element frequency counter.
 *
 * @param <T> Counted object class.
 * @author Michał Marcińczuk
 */
public class FrequencyCounter<T> {

  private Map<T, Integer> frequency = new HashMap<T, Integer>();

  public synchronized void add(T object) {
    Integer c = this.frequency.get(object);
    if (c == null) {
      c = 0;
    }
    c += 1;
    this.frequency.put(object, c);
  }

  public void addAll(Collection<T> objects) {
    for (T o : objects) {
      this.add(o);
    }
  }

  /**
   * Return a set of most frequent elements.
   *
   * @return
   */
  public Set<T> getMostFrequent() {
    Set<T> itemsWithMaxFrequency = new HashSet<T>();
    if (this.frequency.size() == 0) {
      return itemsWithMaxFrequency;
    } else {
      int max = Collections.max(this.frequency.values());
      for (T o : this.frequency.keySet()) {
        if (this.frequency.get(o) == max) {
          itemsWithMaxFrequency.add(o);
        }
      }
      return itemsWithMaxFrequency;
    }
  }

}
